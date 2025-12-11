package com.itau.seguro.app.infrastructure.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.itau.seguro.app.application.service.SolicitacaoApoliceService;
import com.itau.seguro.app.domain.model.CategoriaProduto;
import com.itau.seguro.app.infrastructure.controller.dto.CriarSolicitacaoDTO;
import com.itau.seguro.app.infrastructure.controller.dto.SolicitacaoResponseDTO;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(SolicitacaoApoliceController.class)
@AutoConfigureMockMvc(addFilters = false)
class SolicitacaoApoliceControllerTest {

        @Autowired
        private MockMvc mockMvc;

        @MockBean
        private SolicitacaoApoliceService service;

        @Autowired
        private ObjectMapper objectMapper;

        @Test
        @DisplayName("POST /solicitacoes - Deve retornar 201 Created")
        void deveCriarSolicitacao() throws Exception {
                CriarSolicitacaoDTO requestDTO = new CriarSolicitacaoDTO(
                                UUID.randomUUID(), "prod123", CategoriaProduto.AUTO, null, null,
                                BigDecimal.TEN, BigDecimal.valueOf(50000), null, null);

                SolicitacaoResponseDTO responseDTO = new SolicitacaoResponseDTO(
                                UUID.randomUUID(), requestDTO.idCliente(), requestDTO.idProduto(),
                                requestDTO.categoria(),
                                null, null, null, null, null, null, null, null, null, null);

                when(service.criar(any(CriarSolicitacaoDTO.class))).thenReturn(responseDTO);

                mockMvc.perform(post("/solicitacoes")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(requestDTO)))
                                .andExpect(status().isCreated())
                                .andExpect(jsonPath("$.id").exists())
                                .andExpect(jsonPath("$.idCliente").value(requestDTO.idCliente().toString()));
        }
}
