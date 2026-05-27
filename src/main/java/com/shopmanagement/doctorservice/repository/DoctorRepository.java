package com.shopmanagement.doctorservice.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.shopmanagement.doctorservice.model.Doctor;

public interface DoctorRepository extends JpaRepository<Doctor, Long> {

    List<Doctor> findByTenantIdAndShopIdOrderByDisplayNameAsc(Long tenantId, String shopId);

    List<Doctor> findByTenantIdAndShopIdAndStatusOrderByDisplayNameAsc(
            Long tenantId, String shopId, String status);

    List<Doctor> findByTenantIdAndShopIdAndDepartmentIdOrderByDisplayNameAsc(
            Long tenantId, String shopId, Long departmentId);

    Optional<Doctor> findByIdAndTenantIdAndShopId(Long id, Long tenantId, String shopId);

    Optional<Doctor> findByTenantIdAndShopIdAndAccountId(Long tenantId, String shopId, Long accountId);
}
