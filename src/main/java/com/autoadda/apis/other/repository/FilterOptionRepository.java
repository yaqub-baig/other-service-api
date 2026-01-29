package com.autoadda.apis.other.repository;

import com.autoadda.apis.other.entity.FilterOption;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FilterOptionRepository extends JpaRepository<FilterOption, Long> {

    /**
     * Find all active options for a filter definition.
     */
    List<FilterOption> findByFilterDefinitionIdAndIsActiveTrueOrderByDisplayOrderAsc(Long filterDefinitionId);

    /**
     * Find all options for a filter definition (including inactive).
     */
    List<FilterOption> findByFilterDefinitionIdOrderByDisplayOrderAsc(Long filterDefinitionId);

    /**
     * Find default options for a filter definition.
     */
    List<FilterOption> findByFilterDefinitionIdAndIsDefaultTrueAndIsActiveTrueOrderByDisplayOrderAsc(Long filterDefinitionId);

    /**
     * Find option by value within a filter definition.
     */
    Optional<FilterOption> findByFilterDefinitionIdAndOptionValue(Long filterDefinitionId, String optionValue);

    /**
     * Check if option value exists within a filter definition.
     */
    boolean existsByFilterDefinitionIdAndOptionValue(Long filterDefinitionId, String optionValue);

    /**
     * Find options by parent value (for dependent filters).
     */
    List<FilterOption> findByFilterDefinitionIdAndParentValueAndIsActiveTrueOrderByDisplayOrderAsc(
            Long filterDefinitionId, String parentValue);

    /**
     * Delete all options for a filter definition.
     */
    void deleteByFilterDefinitionId(Long filterDefinitionId);

    /**
     * Count options for a filter definition.
     */
    long countByFilterDefinitionIdAndIsActiveTrue(Long filterDefinitionId);
}
