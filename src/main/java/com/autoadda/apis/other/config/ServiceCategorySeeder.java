package com.autoadda.apis.other.config;


import com.autoadda.apis.other.entity.ServiceCategory;
import com.autoadda.apis.other.repository.ServiceCategoryRepository;
import com.autoadda.apis.other.util.VendorServiceType;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Arrays;

/**
 * Ensures one {@code ServiceCategory} row exists per {@link VendorServiceType}.
 * The category name is the enum's display name, which the per-service onboarding
 * flow uses to auto-attach the matching category to a vendor.
 */
@Slf4j
@Configuration
public class ServiceCategorySeeder {

    @Bean
    CommandLineRunner initServiceCategories(ServiceCategoryRepository repository) {
        return args -> Arrays.stream(VendorServiceType.values()).forEach(type -> {
            String name = type.getDisplayName();
            repository.findByName(name).orElseGet(() -> {
                ServiceCategory category = new ServiceCategory();
                category.setName(name);
                category.setDescription("");
                log.info("Seeding service category: {}", name);
                return repository.save(category);
            });
        });
    }
}
