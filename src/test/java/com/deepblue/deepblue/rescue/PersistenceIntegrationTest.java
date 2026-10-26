package com.deepblue.deepblue.rescue;

import com.deepblue.rescue.domain.*;
import com.deepblue.rescue.repository.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.transaction.annotation.Transactional;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

@Testcontainers
@SpringBootTest
@Transactional
class PersistenceIntegrationTest {

    @Container
    @ServiceConnection
    static final PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:18-alpine")
            .withDatabaseName("deepblue_test")
            .withUsername("deepblue")
            .withPassword("deepblue");

    @Autowired private RescueCenterRepository rescueCenterRepository;
    @Autowired private RescueCaseRepository rescueCaseRepository;
    @Autowired private AnimalRepository animalRepository;
    @Autowired private MedicalRecordRepository medicalRecordRepository;
    @Autowired private SpecialistRepository specialistRepository;
    @Autowired private ExpertiseRepository expertiseRepository;
    @Autowired private TreatmentRepository treatmentRepository;

    @Test
    @DisplayName("Probar repositorios y migración V3")
    void testIntegratorChallenge() {
        RescueCenter center = rescueCenterRepository.save(new RescueCenter("DB-CAR", "DeepBlue Caribbean", "Santa Marta"));

        RescueCase rescueCase = new RescueCase();
        rescueCase.setCaseCode("RES-2026-100");
        rescueCase.setRescueDate(LocalDate.of(2026, 8, 18));
        rescueCase.setRescueLocation("Bahía Concha");
        rescueCase.setStatus(RescueStatus.IN_REHABILITATION);
        rescueCase.setRescueCenter(center);

        Animal animal = new Animal();
        animal.setAnimalCode("AN-2026-100");
        animal.setCommonName("Green Sea Turtle");
        animal.setScientificName("Chelonia mydas");
        animal.setSex(AnimalSex.FEMALE);
        animal.setTrackingDeviceCode("GPS-999");
        rescueCase.assignAnimal(animal);

        MedicalRecord record = new MedicalRecord();
        record.setInitialWeight(new BigDecimal("27.80"));
        record.setInitialCondition("STABLE");
        record.setInjuries("Net injury");
        animal.assignMedicalRecord(record);

        rescueCaseRepository.saveAndFlush(rescueCase);

        Optional<RescueCase> foundCase = rescueCaseRepository.findByCaseCode("RES-2026-100");
        assertThat(foundCase).isPresent();
    }
}
