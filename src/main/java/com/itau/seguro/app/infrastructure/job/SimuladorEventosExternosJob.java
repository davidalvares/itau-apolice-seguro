package com.itau.seguro.app.infrastructure.job;

import com.itau.seguro.app.domain.model.SolicitacaoApolice;
import com.itau.seguro.app.domain.model.StatusSolicitacao;
import com.itau.seguro.app.infrastructure.messaging.dto.EventoPagamento;
import com.itau.seguro.app.infrastructure.messaging.dto.EventoSubscricao;
import com.itau.seguro.app.infrastructure.persistence.SolicitacaoApoliceRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Random;

@Component
@RequiredArgsConstructor
@Slf4j
public class SimuladorEventosExternosJob {

    private final SolicitacaoApoliceRepository repository;
    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final Random random = new Random();

    @Scheduled(fixedDelay = 10000)
    public void executar() {
        List<SolicitacaoApolice> pendentes = repository.findByStatus(StatusSolicitacao.PENDENTE);

        if (pendentes.isEmpty()) {
            return;
        }

        log.info("Iniciando simulação para {} apólices pendentes", pendentes.size());

        for (SolicitacaoApolice apolice : pendentes) {
            try {
                processarApolice(apolice);
            } catch (Exception e) {
                log.error("Erro ao simular eventos para apólice {}", apolice.getId(), e);
            }
        }
    }

    private void processarApolice(SolicitacaoApolice apolice) {
        // Simula processamento assíncrono, não age em todas as execuções para parecer
        // mais real
        if (random.nextInt(10) < 3) {
            log.debug("Pulando simulação para apólice {} nesta execução para variar tempo.", apolice.getId());
            return;
        }

        if (!apolice.isPago()) {
            simularPagamento(apolice);
        } else if (!apolice.isSubscrito()) {
            simularSubscricao(apolice);
        }
    }

    private void simularPagamento(SolicitacaoApolice apolice) {
        EventoPagamento evento = new EventoPagamento();
        evento.setIdApolice(apolice.getId());

        // 80% de chance de aprovação
        boolean aprovado = random.nextInt(10) < 8;
        evento.setStatus(aprovado ? "CONFIRMED" : "REJECTED");

        log.info("Simulando Pagamento para apólice {}: {}", apolice.getId(), evento.getStatus());
        kafkaTemplate.send("payment-events", apolice.getId().toString(), evento);
    }

    private void simularSubscricao(SolicitacaoApolice apolice) {
        EventoSubscricao evento = new EventoSubscricao();
        evento.setIdApolice(apolice.getId());

        // 90% de chance de autorização se já pagou
        boolean autorizado = random.nextInt(10) < 9;
        evento.setStatus(autorizado ? "AUTHORIZED" : "DENIED");

        log.info("Simulando Subscrição para apólice {}: {}", apolice.getId(), evento.getStatus());
        kafkaTemplate.send("subscription-events", apolice.getId().toString(), evento);
    }
}
