package com.deepblue.rescue.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.deepblue.rescue.domain.Specialist;

public interface SpecialistRepository extends JpaRepository<Specialist, Long> {

    Optional<Specialist> findByProfessionalCode(String professionalCode);
}