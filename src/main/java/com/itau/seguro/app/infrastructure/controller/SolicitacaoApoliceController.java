package com.itau.seguro.app.infrastructure.controller;

import com.itau.seguro.app.application.service.SolicitacaoApoliceService;
import com.itau.seguro.app.infrastructure.controller.dto.CriarSolicitacaoDTO;
import com.itau.seguro.app.infrastructure.controller.dto.SolicitacaoResponseDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/solicitacoes")
@RequiredArgsConstructor
public class SolicitacaoApoliceController {

    private final SolicitacaoApoliceService service;

    @PostMapping
    public ResponseEntity<SolicitacaoResponseDTO> criar(@RequestBody CriarSolicitacaoDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.criar(dto));
    }

    @GetMapping("/{id}")
    public ResponseEntity<SolicitacaoResponseDTO> buscarPorId(@PathVariable UUID id) {
        return ResponseEntity.ok(service.buscarPorId(id));
    }

    @GetMapping("/cliente/{idCliente}")
    public ResponseEntity<List<SolicitacaoResponseDTO>> buscarPorCliente(@PathVariable UUID idCliente) {
        return ResponseEntity.ok(service.buscarPorIdCliente(idCliente));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> cancelar(@PathVariable UUID id) {
        service.cancelar(id);
        return ResponseEntity.noContent().build();
    }
}
