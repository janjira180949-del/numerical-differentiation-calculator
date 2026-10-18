package edu.numerical.model;

import jakarta.validation.constraints.*;

public record CalculationRequest(
    @NotBlank @Size(max=200) String expression,
    @NotNull Double x, @NotNull @Positive Double h,
    @Min(1) @Max(4) int derivativeOrder,
    @NotBlank String method, @NotBlank String accuracy,
    Double referenceValue) {}
