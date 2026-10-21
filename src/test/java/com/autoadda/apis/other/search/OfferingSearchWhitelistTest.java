package com.autoadda.apis.other.search;

import com.autoadda.apis.other.exception.BusinessException;
import com.autoadda.apis.other.repository.ServiceAttributeDefinitionRepository;
import com.autoadda.apis.other.repository.ServiceFilterDefinitionRepository;
import com.autoadda.apis.other.request.OfferingSearchRequest;
import com.autoadda.apis.other.service.OfferingSearchService;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

class OfferingSearchWhitelistTest {

    private final EntityManager entityManager = mock(EntityManager.class);
    private final ServiceAttributeDefinitionRepository attributes = mock(ServiceAttributeDefinitionRepository.class);
    private final OfferingSearchService search = new OfferingSearchService(entityManager,
            mock(ServiceFilterDefinitionRepository.class), new ObjectMapper(), attributes,
            mock(OfferingAttributeIndexer.class));

    @Test
    void injectionInAttributeKeyIsRejectedBeforeAnySql() {
        when(attributes.findActiveCodes(3)).thenReturn(List.of("rim_size"));
        OfferingSearchRequest request = OfferingSearchRequest.builder()
                .categoryId(3)
                .attributes(Map.of("rim_size') = 1 OR 1=1 -- ", "15"))
                .build();

        assertThatThrownBy(() -> search.search(request))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("Unknown search attribute");
        verify(entityManager, never()).createNativeQuery(anyString());
    }

    @Test
    void wellFormedButUnknownCodeIsRejected() {
        when(attributes.findActiveCodes(3)).thenReturn(List.of("rim_size"));
        OfferingSearchRequest request = OfferingSearchRequest.builder()
                .categoryId(3)
                .attributes(Map.of("brand", "MRF"))
                .build();

        assertThatThrownBy(() -> search.search(request)).isInstanceOf(BusinessException.class);
        verify(entityManager, never()).createNativeQuery(anyString());
    }
}
