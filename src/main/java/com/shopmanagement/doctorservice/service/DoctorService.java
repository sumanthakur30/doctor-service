package com.shopmanagement.doctorservice.service;

import java.util.List;
import java.util.Objects;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.shopmanagement.doctorservice.model.Doctor;
import com.shopmanagement.doctorservice.model.DoctorSchedule;
import com.shopmanagement.doctorservice.repository.DoctorRepository;
import com.shopmanagement.doctorservice.repository.DoctorScheduleRepository;
import com.shopmanagement.doctorservice.support.TenantContext;

@Service
public class DoctorService {

    private final DoctorRepository doctorRepository;
    private final DoctorScheduleRepository doctorScheduleRepository;
    private final DepartmentService departmentService;

    public DoctorService(
            DoctorRepository doctorRepository,
            DoctorScheduleRepository doctorScheduleRepository,
            DepartmentService departmentService) {
        this.doctorRepository = doctorRepository;
        this.doctorScheduleRepository = doctorScheduleRepository;
        this.departmentService = departmentService;
    }

    @Transactional(readOnly = true)
    public List<Doctor> list(Long departmentId, String status) {
        Long tenantId = TenantContext.requireTenantId();
        String shopId = TenantContext.requireShopId();
        if (departmentId != null) {
            return doctorRepository.findByTenantIdAndShopIdAndDepartmentIdOrderByDisplayNameAsc(
                    tenantId, shopId, departmentId);
        }
        if (status != null && !status.isBlank()) {
            return doctorRepository.findByTenantIdAndShopIdAndStatusOrderByDisplayNameAsc(
                    tenantId, shopId, status.trim().toUpperCase());
        }
        return doctorRepository.findByTenantIdAndShopIdOrderByDisplayNameAsc(tenantId, shopId);
    }

    @Transactional(readOnly = true)
    public Doctor get(Long id) {
        return requireDoctor(id);
    }

    @Transactional
    public Doctor create(Doctor doctor) {
        TenantContext.requireAnyPermission("MANAGE_STAFF", "MANAGE_DOCTORS");
        Long tenantId = TenantContext.requireTenantId();
        String shopId = TenantContext.requireShopId();
        if (doctor.getAccountId() == null) {
            throw new IllegalArgumentException("accountId is required");
        }
        if (doctor.getDisplayName() == null || doctor.getDisplayName().isBlank()) {
            throw new IllegalArgumentException("displayName is required");
        }
        doctorRepository.findByTenantIdAndShopIdAndAccountId(tenantId, shopId, doctor.getAccountId())
                .ifPresent(existing -> {
                    throw new IllegalArgumentException("Doctor already registered for account: " + doctor.getAccountId());
                });
        if (doctor.getDepartmentId() != null) {
            departmentService.requireDepartment(doctor.getDepartmentId());
        }
        doctor.setTenantId(tenantId);
        doctor.setShopId(shopId);
        doctor.setStatus(normalizeStatus(doctor.getStatus()));
        applyFeeDefaults(doctor);
        return doctorRepository.save(doctor);
    }

    @Transactional
    public Doctor update(Long id, Doctor payload) {
        TenantContext.requireAnyPermission("MANAGE_STAFF", "MANAGE_DOCTORS");
        Doctor existing = requireDoctor(id);
        if (payload.getDisplayName() != null && !payload.getDisplayName().isBlank()) {
            existing.setDisplayName(payload.getDisplayName().trim());
        }
        if (payload.getDepartmentId() != null) {
            departmentService.requireDepartment(payload.getDepartmentId());
            existing.setDepartmentId(payload.getDepartmentId());
        }
        if (payload.getRegistrationNumber() != null) {
            existing.setRegistrationNumber(payload.getRegistrationNumber());
        }
        if (payload.getSpecialization() != null) {
            existing.setSpecialization(payload.getSpecialization());
        }
        if (payload.getQualification() != null) {
            existing.setQualification(payload.getQualification());
        }
        if (payload.getConsultationFee() != null) {
            existing.setConsultationFee(payload.getConsultationFee());
        }
        if (payload.getFollowUpFee() != null) {
            existing.setFollowUpFee(payload.getFollowUpFee());
        }
        if (payload.getCommissionPercent() != null) {
            existing.setCommissionPercent(payload.getCommissionPercent());
        }
        if (payload.getSignatureUrl() != null) {
            existing.setSignatureUrl(payload.getSignatureUrl());
        }
        if (payload.getStatus() != null && !payload.getStatus().isBlank()) {
            existing.setStatus(normalizeStatus(payload.getStatus()));
        }
        return doctorRepository.save(existing);
    }

    @Transactional(readOnly = true)
    public List<DoctorSchedule> listSchedule(Long doctorId) {
        requireDoctor(doctorId);
        return doctorScheduleRepository.findByTenantIdAndShopIdAndDoctorIdOrderByDayOfWeekAscStartTimeAsc(
                TenantContext.requireTenantId(), TenantContext.requireShopId(), doctorId);
    }

    @Transactional
    public List<DoctorSchedule> replaceSchedule(Long doctorId, List<DoctorSchedule> rows) {
        TenantContext.requireAnyPermission("MANAGE_STAFF", "MANAGE_DOCTORS");
        requireDoctor(doctorId);
        Long tenantId = TenantContext.requireTenantId();
        String shopId = TenantContext.requireShopId();
        List<DoctorSchedule> existing = doctorScheduleRepository
                .findByTenantIdAndShopIdAndDoctorIdOrderByDayOfWeekAscStartTimeAsc(tenantId, shopId, doctorId);
        doctorScheduleRepository.deleteAll(existing);
        if (rows == null || rows.isEmpty()) {
            return List.of();
        }
        for (DoctorSchedule row : rows) {
            row.setId(null);
            row.setTenantId(tenantId);
            row.setShopId(shopId);
            row.setDoctorId(doctorId);
            if (row.getBranchId() == null) {
                throw new IllegalArgumentException("branchId is required on schedule row");
            }
            if (row.getDayOfWeek() == null || row.getStartTime() == null || row.getEndTime() == null) {
                throw new IllegalArgumentException("dayOfWeek, startTime, endTime are required");
            }
            if (row.getSlotDurationMinutes() == null) {
                row.setSlotDurationMinutes(15);
            }
            if (row.getMaxPatientsPerSlot() == null) {
                row.setMaxPatientsPerSlot(1);
            }
            if (row.getActive() == null) {
                row.setActive(true);
            }
            doctorScheduleRepository.save(row);
        }
        return listSchedule(doctorId);
    }

    Doctor requireDoctor(Long id) {
        return doctorRepository.findByIdAndTenantIdAndShopId(
                        id, TenantContext.requireTenantId(), TenantContext.requireShopId())
                .orElseThrow(() -> new IllegalArgumentException("Doctor not found: " + id));
    }

    private static void applyFeeDefaults(Doctor doctor) {
        if (doctor.getConsultationFee() == null) {
            doctor.setConsultationFee(0.0);
        }
        if (doctor.getFollowUpFee() == null) {
            doctor.setFollowUpFee(0.0);
        }
        if (doctor.getCommissionPercent() == null) {
            doctor.setCommissionPercent(0.0);
        }
    }

    private static String normalizeStatus(String status) {
        return status == null || status.isBlank() ? "ACTIVE" : status.trim().toUpperCase();
    }
}
