package com.autoadda.apis.other.repository;

import com.autoadda.apis.other.entity.ServiceSchemaVersion;
import com.autoadda.apis.other.util.SchemaVersionStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ServiceSchemaVersionRepository extends JpaRepository<ServiceSchemaVersion, Integer> {

    List<ServiceSchemaVersion> findByCatalogServiceIdOrderByVersionNoDesc(Integer catalogServiceId);

    Optional<ServiceSchemaVersion> findFirstByCatalogServiceIdAndStatusOrderByVersionNoDesc(Integer catalogServiceId,
                                                                                            SchemaVersionStatus status);

    Optional<ServiceSchemaVersion> findFirstByCatalogServiceIdOrderByVersionNoDesc(Integer catalogServiceId);
}
