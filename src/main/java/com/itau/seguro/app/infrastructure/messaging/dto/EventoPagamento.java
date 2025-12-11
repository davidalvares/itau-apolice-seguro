package com.itau.seguro.app.infrastructure.messaging.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@NoArgsConstructor
public class EventoPagamento {
    private UUID idApolice;
    private String status; // CONFIRMED, REJECTED (Manter valores de status técnicos em ingles ou enum?)
                           // Keeping string for now but mapping logic might change.
}
