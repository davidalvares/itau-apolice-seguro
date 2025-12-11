package com.itau.seguro.app.infrastructure.client;

import com.itau.seguro.app.infrastructure.client.dto.RespostaAnaliseFraude;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.UUID;

@FeignClient(name = "fraud-api", url = "${integration.fraud-api.url}")
public interface ClienteFraude {

    @GetMapping("/analyze/{customerId}?orderId={orderId}")
    RespostaAnaliseFraude analisarRisco(@PathVariable("customerId") UUID customerId,
            @PathVariable("orderId") UUID orderId);
}
