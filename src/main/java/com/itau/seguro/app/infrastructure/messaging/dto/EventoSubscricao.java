package com.itau.seguro.app.infrastructure.messaging.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@NoArgsConstructor
public class EventoSubscricao {
    private UUID idApolice;
    private String status; // AUTHORIZED, DENIED
}
