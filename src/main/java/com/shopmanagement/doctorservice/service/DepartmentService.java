package com.shopmanagement.doctorservice.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.shopmanagement.doctorservice.model.Department;
import com.shopmanagement.doctorservice.repository.DepartmentRepository;
import com.shopmanagement.doctorservice.support.TenantContext;

@Service
public class DepartmentService {

    private final DepartmentRepository departmentRepository;

    public DepartmentService(DepartmentRepository departmentRepository) {
        this.departmentRepository = departmentRepository;
    }

    @Transactional(readOnly = true)
    public List<Department> list() {
        return departmentRepository.findByTenantIdAndShopIdOrderByNameAsc(
                TenantContext.requireTenantId(), TenantContext.requireShopId());
    }

    @Transactional
    public Department create(Department department) {
        TenantContext.requireAnyPermission("MANAGE_STAFF", "MANAGE_DOCTORS");
        Long tenantId = TenantContext.requireTenantId();
        String shopId = TenantContext.requireShopId();
        if (department.getCode() == null || department.getCode().isBlank()) {
            throw new IllegalArgumentException("code is required");
        }
        if (department.getName() == null || department.getName().isBlank()) {
            throw new IllegalArgumentException("name is required");
        }
        String code = department.getCode().trim().toUpperCase();
        if (departmentRepository.existsByTenantIdAndShopIdAndCodeIgnoreCase(tenantId, shopId, code)) {
            throw new IllegalArgumentException("Department code already exists: " + code);
        }
        department.setTenantId(tenantId);
        department.setShopId(shopId);
        department.setCode(code);
        department.setStatus(normalizeStatus(department.getStatus()));
        return departmentRepository.save(department);
    }

    @Transactional
    public Department update(Long id, Department payload) {
        TenantContext.requireAnyPermission("MANAGE_STAFF", "MANAGE_DOCTORS");
        Department existing = requireDepartment(id);
        if (payload.getName() != null && !payload.getName().isBlank()) {
            existing.setName(payload.getName().trim());
        }
        if (payload.getDescription() != null) {
            existing.setDescription(payload.getDescription());
        }
        if (payload.getStatus() != null && !payload.getStatus().isBlank()) {
            existing.setStatus(normalizeStatus(payload.getStatus()));
        }
        return departmentRepository.save(existing);
    }

    Department requireDepartment(Long id) {
        return departmentRepository.findByIdAndTenantIdAndShopId(
                        id, TenantContext.requireTenantId(), TenantContext.requireShopId())
                .orElseThrow(() -> new IllegalArgumentException("Department not found: " + id));
    }

    private static String normalizeStatus(String status) {
        return status == null || status.isBlank() ? "ACTIVE" : status.trim().toUpperCase();
    }
}
