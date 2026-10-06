package com.deepblue.rescue.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.deepblue.rescue.domain.Treatment;

public interface TreatmentRepository extends JpaRepository<Treatment, Long> {

    List<Treatment> findByAnimalAnimalCodeOrderByPerformedAtAsc(String animalCode);
}