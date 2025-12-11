package com.itau.seguro.app.infrastructure.client.dto;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public record RespostaAnaliseFraude(
        UUID orderId,
        UUID customerId,
        LocalDateTime analyzedAt,
        String classification, // REGULAR, HIGH_RISK, etc
        List<OcorrenciaFraude> occurrences) {
}

record OcorrenciaFraude(
        String id,
        Long productId,
        String type,
        String description,
        LocalDateTime createdAt,
        LocalDateTime updatedAt) {
}
