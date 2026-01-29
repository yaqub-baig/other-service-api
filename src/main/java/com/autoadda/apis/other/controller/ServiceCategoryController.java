package com.autoadda.apis.other.controller;

import com.autoadda.apis.other.entity.ServiceCategory;
import com.autoadda.apis.other.response.OsGenericResponse;
import com.autoadda.apis.other.service.ServiceCategoryService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/service-categories")
public class ServiceCategoryController {

    private final ServiceCategoryService serviceCategoryService;

    public ServiceCategoryController(ServiceCategoryService serviceCategoryService) {
        this.serviceCategoryService = serviceCategoryService;
    }

    @GetMapping
    public ResponseEntity<List<ServiceCategory>> getAll() {
        return ResponseEntity.ok(serviceCategoryService.getAll());
    }

    @PostMapping
    public ResponseEntity<OsGenericResponse> create(@RequestBody ServiceCategory request) {
        ServiceCategory created = serviceCategoryService.create(request);
        return ResponseEntity.ok(OsGenericResponse.success("Service category created", created));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ServiceCategory> getById(@PathVariable Integer id) {
        return ResponseEntity.ok(serviceCategoryService.getById(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<OsGenericResponse> update(
            @PathVariable Integer id,
            @RequestBody ServiceCategory updatedCategory) {
        serviceCategoryService.update(id, updatedCategory);
        return ResponseEntity.ok(OsGenericResponse.success("Service category updated successfully", updatedCategory));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<OsGenericResponse> delete(@PathVariable Integer id) {
        serviceCategoryService.delete(id);
        return ResponseEntity.ok(OsGenericResponse.success("Service category deleted successfully", null));
    }

    @GetMapping("/parent/{parentId}")
    public ResponseEntity<List<ServiceCategory>> getByParentId(@PathVariable Integer parentId) {
        return ResponseEntity.ok(serviceCategoryService.getByParentId(parentId));
    }

    @GetMapping("/root")
    public ResponseEntity<List<ServiceCategory>> getRootCategories() {
        return ResponseEntity.ok(serviceCategoryService.getRootCategories());
    }
}
