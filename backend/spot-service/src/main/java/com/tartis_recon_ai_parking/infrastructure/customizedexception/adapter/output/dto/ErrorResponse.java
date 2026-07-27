package com.tartis_recon_ai_parking.infrastructure.customizedexception.adapter.output.dto;

public record ErrorResponse(
    String timestamp,
    int status,
    String error,
    String message,
    String path
) {}
