package com.deepblue.rescue.service.impl;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.deepblue.exception.BusinessRuleException;
import com.deepblue.exception.ResourceNotFoundException;
import com.deepblue.rescue.domain.Animal;
import com.deepblue.rescue.domain.RescueStatus;
import com.deepblue.rescue.domain.Specialist;
import com.deepblue.rescue.domain.Treatment;
import com.deepblue.rescue.dto.request.CreateTreatmentRequest;
import com.deepblue.rescue.dto.response.TreatmentResponse;
import com.deepblue.rescue.mapper.TreatmentMapper;
import com.deepblue.rescue.repository.AnimalRepository;
import com.deepblue.rescue.repository.SpecialistRepository;
import com.deepblue.rescue.repository.TreatmentRepository;
import com.deepblue.rescue.service.TreatmentService;

@Service
public class TreatmentServiceImpl implements TreatmentService {

    private final TreatmentRepository treatmentRepository;
    private final AnimalRepository animalRepository;
    private final SpecialistRepository specialistRepository;
    private final TreatmentMapper mapper;

    public TreatmentServiceImpl(TreatmentRepository treatmentRepository,
                                AnimalRepository animalRepository,
                                SpecialistRepository specialistRepository,
                                TreatmentMapper mapper) {
        this.treatmentRepository = treatmentRepository;
        this.animalRepository = animalRepository;
        this.specialistRepository = specialistRepository;
        this.mapper = mapper;
    }

    @Override
    @Transactional
    public TreatmentResponse register(CreateTreatmentRequest request) {
        Animal animal = animalRepository.findByAnimalCode(request.animalCode())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Animal not found: " + request.animalCode()));

        Specialist specialist = specialistRepository
                .findByProfessionalCode(request.specialistCode())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Specialist not found: " + request.specialistCode()));

        if (!Boolean.TRUE.equals(specialist.getActive())) {
            throw new BusinessRuleException(
                    "Specialist is not active: " + request.specialistCode());
        }

        RescueStatus status = animal.getRescueCase().getStatus();
        if (status == RescueStatus.RELEASED || status == RescueStatus.CLOSED) {
            throw new BusinessRuleException("Released animals cannot receive treatments");
        }

        if (request.performedAt().toLocalDate()
                .isBefore(animal.getRescueCase().getRescueDate())) {
            throw new BusinessRuleException(
                    "Treatment cannot be performed before the rescue date");
        }

        Treatment treatment = new Treatment();
        treatment.setAnimal(animal);
        treatment.setSpecialist(specialist);
        treatment.setPerformedAt(request.performedAt());
        treatment.setType(request.type());
        treatment.setDescription(request.description());

        return mapper.toResponse(treatmentRepository.save(treatment));
    }

    @Override
    @Transactional(readOnly = true)
    public List<TreatmentResponse> findByAnimalCode(String animalCode) {
        if (animalRepository.findByAnimalCode(animalCode).isEmpty()) {
            throw new ResourceNotFoundException("Animal not found: " + animalCode);
        }
        return treatmentRepository
                .findByAnimalAnimalCodeOrderByPerformedAtAsc(animalCode)
                .stream().map(mapper::toResponse).toList();
    }
}