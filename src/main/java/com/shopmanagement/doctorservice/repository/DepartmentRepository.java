package com.shopmanagement.doctorservice.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.shopmanagement.doctorservice.model.Department;

public interface DepartmentRepository extends JpaRepository<Department, Long> {

    List<Department> findByTenantIdAndShopIdOrderByNameAsc(Long tenantId, String shopId);

    Optional<Department> findByIdAndTenantIdAndShopId(Long id, Long tenantId, String shopId);

    boolean existsByTenantIdAndShopIdAndCodeIgnoreCase(Long tenantId, String shopId, String code);
}
