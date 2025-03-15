package dev.vineeth.pip.api;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

import java.math.BigDecimal;

public record ApprovalRequest(
    @NotBlank String requestId,
    @NotBlank String requester,
    @NotNull @PositiveOrZero BigDecimal amount,
    String managerId,
    String reason
) { }
