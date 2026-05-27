package com.shopmanagement.doctorservice.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.shopmanagement.doctorservice.model.DoctorSchedule;

public interface DoctorScheduleRepository extends JpaRepository<DoctorSchedule, Long> {

    List<DoctorSchedule> findByTenantIdAndShopIdAndDoctorIdOrderByDayOfWeekAscStartTimeAsc(
            Long tenantId, String shopId, Long doctorId);
}
