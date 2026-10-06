package com.deepblue.rescue.service.impl;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.deepblue.exception.ResourceNotFoundException;
import com.deepblue.rescue.domain.Animal;
import com.deepblue.rescue.domain.RescueStatus;
import com.deepblue.rescue.dto.response.AnimalResponse;
import com.deepblue.rescue.mapper.AnimalMapper;
import com.deepblue.rescue.repository.AnimalRepository;
import com.deepblue.rescue.service.AnimalService;

@Service
public class AnimalServiceImpl implements AnimalService {

    private final AnimalRepository repository;
    private final AnimalMapper mapper;

    public AnimalServiceImpl(AnimalRepository repository, AnimalMapper mapper) {
        this.repository = repository;
        this.mapper = mapper;
    }

    @Override
    @Transactional(readOnly = true)
    public AnimalResponse findByCode(String animalCode) {
        return mapper.toResponse(getAnimal(animalCode));
    }

    @Override
    @Transactional(readOnly = true)
    public List<AnimalResponse> findAnimalsInRehabilitation() {
        return repository.findByRescueCaseStatus(RescueStatus.IN_REHABILITATION)
                .stream().map(mapper::toResponse).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public boolean canReceiveTreatment(String animalCode) {
        RescueStatus status = getAnimal(animalCode).getRescueCase().getStatus();
        return status != RescueStatus.RELEASED && status != RescueStatus.CLOSED;
    }

    private Animal getAnimal(String animalCode) {
        return repository.findByAnimalCode(animalCode)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Animal not found: " + animalCode));
    }
}