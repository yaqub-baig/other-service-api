package com.autoadda.apis.other.service;

import com.autoadda.apis.other.entity.CatalogService;
import com.autoadda.apis.other.entity.ServiceAttributeDefinition;
import com.autoadda.apis.other.entity.ServiceSchemaVersion;
import com.autoadda.apis.other.exception.BusinessException;
import com.autoadda.apis.other.exception.ResourceNotFoundException;
import com.autoadda.apis.other.repository.ServiceAttributeDefinitionRepository;
import com.autoadda.apis.other.repository.ServiceSchemaVersionRepository;
import com.autoadda.apis.other.response.SchemaVersionResponse;
import com.autoadda.apis.other.util.SchemaVersionStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Versioned attribute schemas per catalog service: DRAFT → PUBLISHED → RETIRED.
 * Only the latest PUBLISHED version is live; published versions are never edited.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SchemaVersionService {

    private final ServiceSchemaVersionRepository versionRepository;
    private final ServiceAttributeDefinitionRepository attributeRepository;
    private final ServiceCatalogService serviceCatalogService;

    @Transactional(readOnly = true)
    public List<SchemaVersionResponse> list(Integer serviceId) {
        serviceCatalogService.getEntity(serviceId);
        return versionRepository.findByCatalogServiceIdOrderByVersionNoDesc(serviceId).stream()
                .map(this::toResponse)
                .toList();
    }

    /**
     * The live version of a service's schema, if one has been published.
     */
    @Transactional(readOnly = true)
    public Optional<ServiceSchemaVersion> currentPublished(Integer serviceId) {
        return versionRepository.findFirstByCatalogServiceIdAndStatusOrderByVersionNoDesc(serviceId, SchemaVersionStatus.PUBLISHED);
    }

    /**
     * The newest version (a draft if one is open, otherwise the live one); what admins edit or view.
     */
    @Transactional(readOnly = true)
    public Optional<ServiceSchemaVersion> latest(Integer serviceId) {
        return versionRepository.findFirstByCatalogServiceIdOrderByVersionNoDesc(serviceId)
                .filter(v -> v.getStatus() != SchemaVersionStatus.RETIRED)
                .or(() -> currentPublished(serviceId));
    }

    /**
     * Returns the open draft, or opens one: the next version number, starting with a copy of the
     * live version's attributes so admins edit a full schema rather than an empty one.
     */
    @Transactional
    public ServiceSchemaVersion getOrCreateDraft(Integer serviceId, String notes) {
        Optional<ServiceSchemaVersion> openDraft =
                versionRepository.findFirstByCatalogServiceIdAndStatusOrderByVersionNoDesc(serviceId, SchemaVersionStatus.DRAFT);
        if (openDraft.isPresent()) {
            return openDraft.get();
        }
        CatalogService service = serviceCatalogService.getEntity(serviceId);
        int nextNo = versionRepository.findFirstByCatalogServiceIdOrderByVersionNoDesc(serviceId)
                .map(v -> v.getVersionNo() + 1)
                .orElse(1);
        ServiceSchemaVersion draft = versionRepository.save(ServiceSchemaVersion.builder()
                .catalogService(service)
                .versionNo(nextNo)
                .status(SchemaVersionStatus.DRAFT)
                .notes(notes)
                .build());
        currentPublished(serviceId).ifPresent(live ->
                attributeRepository.findBySchemaVersionIdOrderByDisplayOrderAsc(live.getId())
                        .forEach(attr -> attributeRepository.save(copyInto(attr, draft))));
        log.info("Opened schema draft v{} for service {}", nextNo, service.getCode());
        return draft;
    }

    @Transactional
    public SchemaVersionResponse openDraft(Integer serviceId, String notes) {
        return toResponse(getOrCreateDraft(serviceId, notes));
    }

    /**
     * Publishes a draft. The previously published version is retired; records created under it
     * keep pointing at it.
     */
    @Transactional
    public SchemaVersionResponse publish(Integer serviceId, Integer versionId) {
        ServiceSchemaVersion draft = getForService(serviceId, versionId);
        if (draft.getStatus() != SchemaVersionStatus.DRAFT) {
            throw new BusinessException("Only a DRAFT version can be published; v" + draft.getVersionNo()
                    + " is " + draft.getStatus());
        }
        currentPublished(serviceId).ifPresent(live -> {
            live.setStatus(SchemaVersionStatus.RETIRED);
            versionRepository.save(live);
        });
        draft.setStatus(SchemaVersionStatus.PUBLISHED);
        draft.setPublishedAt(LocalDateTime.now());
        ServiceSchemaVersion saved = versionRepository.save(draft);
        log.info("Published schema v{} for service {}", saved.getVersionNo(), saved.getCatalogService().getCode());
        return toResponse(saved);
    }

    @Transactional(readOnly = true)
    public ServiceSchemaVersion getForService(Integer serviceId, Integer versionId) {
        ServiceSchemaVersion version = versionRepository.findById(versionId)
                .orElseThrow(() -> new ResourceNotFoundException("Schema version not found with id: " + versionId));
        if (!version.getCatalogService().getId().equals(serviceId)) {
            throw new BusinessException("Schema version " + versionId + " does not belong to service " + serviceId);
        }
        return version;
    }

    /**
     * Published and retired versions are immutable; edits must go to a draft.
     */
    public void requireEditable(ServiceAttributeDefinition attribute) {
        ServiceSchemaVersion version = attribute.getSchemaVersion();
        if (version != null && version.getStatus() != SchemaVersionStatus.DRAFT) {
            throw new BusinessException("Attribute '" + attribute.getAttributeCode() + "' belongs to "
                    + version.getStatus() + " schema v" + version.getVersionNo()
                    + "; open a draft for the service and edit it there");
        }
    }

    private static ServiceAttributeDefinition copyInto(ServiceAttributeDefinition source, ServiceSchemaVersion draft) {
        return ServiceAttributeDefinition.builder()
                .serviceCategory(source.getServiceCategory())
                .catalogService(source.getCatalogService())
                .schemaVersion(draft)
                .attributeCode(source.getAttributeCode())
                .attributeName(source.getAttributeName())
                .attributeNameAr(source.getAttributeNameAr())
                .dataType(source.getDataType())
                .unit(source.getUnit())
                .isRequired(source.getIsRequired())
                .isFilterable(source.getIsFilterable())
                .isSearchable(source.getIsSearchable())
                .displayOrder(source.getDisplayOrder())
                .description(source.getDescription())
                .descriptionAr(source.getDescriptionAr())
                .defaultValue(source.getDefaultValue())
                .validationRules(source.getValidationRules())
                .options(source.getOptions())
                .inputContext(source.getInputContext())
                .isActive(source.getIsActive())
                .build();
    }

    private SchemaVersionResponse toResponse(ServiceSchemaVersion v) {
        return SchemaVersionResponse.builder()
                .id(v.getId())
                .serviceId(v.getCatalogService().getId())
                .serviceCode(v.getCatalogService().getCode())
                .versionNo(v.getVersionNo())
                .status(v.getStatus())
                .notes(v.getNotes())
                .attributeCount(attributeRepository.findBySchemaVersionIdAndIsActiveTrueOrderByDisplayOrderAsc(v.getId()).size())
                .publishedAt(v.getPublishedAt())
                .createdAt(v.getCreatedAt())
                .build();
    }
}
