package com.shopmanagement.doctorservice.web;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.shopmanagement.doctorservice.model.Doctor;
import com.shopmanagement.doctorservice.model.DoctorSchedule;
import com.shopmanagement.doctorservice.service.DoctorService;

@RestController
@RequestMapping("/doctors")
public class DoctorController {

    private final DoctorService doctorService;

    public DoctorController(DoctorService doctorService) {
        this.doctorService = doctorService;
    }

    @GetMapping
    public List<Doctor> list(
            @RequestParam(required = false) Long departmentId,
            @RequestParam(required = false) String status) {
        return doctorService.list(departmentId, status);
    }

    @GetMapping("/{id}")
    public Doctor get(@PathVariable Long id) {
        return doctorService.get(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Doctor create(@RequestBody Doctor doctor) {
        return doctorService.create(doctor);
    }

    @PutMapping("/{id}")
    public Doctor update(@PathVariable Long id, @RequestBody Doctor doctor) {
        return doctorService.update(id, doctor);
    }

    @GetMapping("/{id}/schedule")
    public List<DoctorSchedule> listSchedule(@PathVariable Long id) {
        return doctorService.listSchedule(id);
    }

    @PutMapping("/{id}/schedule")
    public List<DoctorSchedule> replaceSchedule(@PathVariable Long id, @RequestBody List<DoctorSchedule> rows) {
        return doctorService.replaceSchedule(id, rows);
    }
}
