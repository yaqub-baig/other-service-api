package com.autoadda.apis.other.search;

import com.autoadda.apis.other.util.AttributeCodes;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Collections;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Keeps a generated, indexed column on service_offerings for each attribute marked isIndexed,
 * e.g. attr_rim_size = JSON_UNQUOTE(JSON_EXTRACT(filterable_attributes, '$.rim_size')).
 * Search filters on those codes use the column, so MySQL can use the index.
 *
 * Hibernate's ddl-auto cannot express generated columns, so this runs the DDL itself, on MySQL only,
 * on its own auto-commit connection (MySQL DDL would otherwise commit the caller's transaction).
 * Columns are only ever added, never dropped.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class OfferingAttributeIndexer {

    static final String COLUMN_PREFIX = "attr_";

    private final DataSource dataSource;

    @Value("${catalog.max-indexed-attributes:20}")
    private int maxIndexedAttributes;

    private final Set<String> indexedCodes = ConcurrentHashMap.newKeySet();

    /** Published after an attribute's isIndexed flag is set or changed. */
    public record IndexedAttributesChanged() {
    }

    @EventListener(ApplicationReadyEvent.class)
    public void onStartup() {
        sync();
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT, fallbackExecution = true)
    public void onIndexedAttributesChanged(IndexedAttributesChanged event) {
        sync();
    }

    /**
     * The generated column for an attribute code, or null when the code has none.
     */
    public String columnFor(String attributeCode) {
        return indexedCodes.contains(attributeCode) ? COLUMN_PREFIX + attributeCode : null;
    }

    public Set<String> indexedCodes() {
        return Collections.unmodifiableSet(indexedCodes);
    }

    synchronized void sync() {
        try (Connection connection = dataSource.getConnection()) {
            if (!connection.getMetaData().getDatabaseProductName().toLowerCase().contains("mysql")) {
                return;
            }
            connection.setAutoCommit(true);
            Set<String> existing = existingColumns(connection);
            Set<String> wanted = wantedCodes(connection);
            for (String code : wanted) {
                if (existing.contains(code)) {
                    continue;
                }
                if (existing.size() >= maxIndexedAttributes) {
                    log.warn("Not indexing attribute '{}': catalog.max-indexed-attributes ({}) reached", code, maxIndexedAttributes);
                    continue;
                }
                addColumn(connection, code);
                existing.add(code);
            }
            indexedCodes.clear();
            indexedCodes.addAll(existing);
        } catch (SQLException e) {
            // Search still works without the columns, just without the index
            log.error("Could not sync indexed offering attributes", e);
        }
    }

    private Set<String> existingColumns(Connection connection) throws SQLException {
        Set<String> codes = new TreeSet<>();
        String sql = "SELECT column_name FROM information_schema.columns "
                + "WHERE table_schema = DATABASE() AND table_name = 'service_offerings' AND column_name LIKE 'attr\\_%'";
        try (PreparedStatement ps = connection.prepareStatement(sql); ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                codes.add(rs.getString(1).substring(COLUMN_PREFIX.length()));
            }
        }
        return codes;
    }

    private Set<String> wantedCodes(Connection connection) throws SQLException {
        Set<String> codes = new TreeSet<>();
        String sql = "SELECT DISTINCT attribute_code FROM service_attribute_definitions WHERE is_indexed = true AND is_active = true";
        try (PreparedStatement ps = connection.prepareStatement(sql); ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                String code = rs.getString(1);
                if (AttributeCodes.isValid(code)) {
                    codes.add(code);
                } else {
                    log.warn("Skipping index for attribute with invalid code '{}'", code);
                }
            }
        }
        return codes;
    }

    private void addColumn(Connection connection, String code) throws SQLException {
        // code is checked against [a-z][a-z0-9_]{0,49}, so it is safe to place in identifiers and the path
        String column = COLUMN_PREFIX + code;
        String ddl = "ALTER TABLE service_offerings "
                + "ADD COLUMN `" + column + "` VARCHAR(191) "
                + "GENERATED ALWAYS AS (JSON_UNQUOTE(JSON_EXTRACT(filterable_attributes, '$." + code + "'))) VIRTUAL, "
                + "ADD INDEX `idx_" + column + "` (`" + column + "`)";
        try (Statement statement = connection.createStatement()) {
            statement.execute(ddl);
        }
        log.info("Added indexed column {} on service_offerings", column);
    }
}
