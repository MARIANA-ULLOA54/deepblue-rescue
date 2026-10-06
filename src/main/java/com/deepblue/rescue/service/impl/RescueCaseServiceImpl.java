package com.deepblue.rescue.service.impl;

import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.deepblue.exception.BusinessRuleException;
import com.deepblue.exception.ResourceNotFoundException;
import com.deepblue.rescue.domain.RescueCase;
import com.deepblue.rescue.domain.RescueStatus;
import com.deepblue.rescue.dto.request.ChangeRescueStatusRequest;
import com.deepblue.rescue.dto.response.RescueCaseResponse;
import com.deepblue.rescue.mapper.RescueCaseMapper;
import com.deepblue.rescue.repository.RescueCaseRepository;
import com.deepblue.rescue.service.RescueCaseService;

import static com.deepblue.rescue.domain.RescueStatus.*;

@Service
public class RescueCaseServiceImpl implements RescueCaseService {

    private static final Map<RescueStatus, Set<RescueStatus>> ALLOWED = Map.of(
            ADMITTED, EnumSet.of(UNDER_EVALUATION),
            UNDER_EVALUATION, EnumSet.of(IN_REHABILITATION),
            IN_REHABILITATION, EnumSet.of(READY_FOR_RELEASE),
            READY_FOR_RELEASE, EnumSet.of(RELEASED),
            RELEASED, EnumSet.of(CLOSED),
            CLOSED, EnumSet.noneOf(RescueStatus.class));

    private final RescueCaseRepository repository;
    private final RescueCaseMapper mapper;

    public RescueCaseServiceImpl(RescueCaseRepository repository, RescueCaseMapper mapper) {
        this.repository = repository;
        this.mapper = mapper;
    }

    @Override
    @Transactional(readOnly = true)
    public RescueCaseResponse findByCode(String caseCode) {
        return mapper.toResponse(getCase(caseCode));
    }

    @Override
    @Transactional(readOnly = true)
    public List<RescueCaseResponse> findByStatus(RescueStatus status) {
        return repository.findByStatusOrderByRescueDateAsc(status)
                .stream().map(mapper::toResponse).toList();
    }

    @Override
    @Transactional
    public RescueCaseResponse changeStatus(String caseCode, ChangeRescueStatusRequest request) {
        RescueCase rescueCase = getCase(caseCode);
        RescueStatus current = rescueCase.getStatus();
        RescueStatus next = request.status();

        if (!ALLOWED.get(current).contains(next)) {
            throw new BusinessRuleException(
                    "Invalid status transition: " + current + " -> " + next);
        }
        rescueCase.setStatus(next);
        return mapper.toResponse(repository.save(rescueCase));
    }

    private RescueCase getCase(String caseCode) {
        return repository.findByCaseCode(caseCode)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Rescue case not found: " + caseCode));
    }
}