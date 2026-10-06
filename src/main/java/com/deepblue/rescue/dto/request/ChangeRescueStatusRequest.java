package com.deepblue.rescue.dto.request;

import com.deepblue.rescue.model.RescueStatus;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChangeRescueStatusRequest {

    @NotNull(message = "Rescue id is required")
    private Long rescueId;

    @NotNull(message = "Status is required")
    private RescueStatus status;
}
