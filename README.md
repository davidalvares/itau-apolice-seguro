# 🛡️ Microsserviço de Apólices de Seguros - Itaú Challenge

Bem-vindo ao serviço de Orquestração de Apólices de Seguros! 🚀

Este projeto é uma solução robusta e orientada a eventos para gerenciar todo o ciclo de vida de uma apólice de seguro, desde a solicitação inicial até a aprovação final, integrando verificação de fraudes, validação de regras de negócio, pagamentos e subscrição.

---

## 🏗️ Arquitetura & Fluxo

O sistema adota uma arquitetura reativa para garantir escalabilidade e resiliência.

### O Fluxo da Apólice ("A Jornada")

Uma apólice passa por diversos estados. Aqui está o caminho feliz:

1.  📥 **RECEBIDO**: O cliente envia a solicitação via API REST.
2.  🔍 **VALIDADO**: O sistema consulta *automaticamente* uma API externa de Fraudes. Com base no risco retornado (ex: `REGULAR`, `HIGH_RISK`), aplicamos regras de negócio sobre o valor segurado.
3.  ⏳ **PENDENTE**: Se aprovada na validação, a apólice fica aguardando processamento externo.
4.  💳 **Pagamento (Async)**: O sistema escuta eventos de confirmação de pagamento.
5.  ✍️ **Subscrição (Async)**: O sistema escuta eventos de autorização da subscrição.
6.  ✅ **APROVADO**: Somente após pagamento confirmado E subscrição autorizada, a apólice é efetivada.

---

## 🧠 Regras de Negócio Inteligentes

O coração do sistema é o motor de validação. Dependendo da classificação de risco do cliente (retornada pela API de Fraudes), limites diferentes de **Valor Segurado** são aplicados:

| Categoria do Produto | 🟢 Regular (Limite) | 🟡 High Risk (Limite) | 🔵 Preferential (Limite) | ⚪ No Info (Limite) |
| :--- | :--- | :--- | :--- | :--- |
| **AUTO** | R$ 350.000,00 | R$ 250.000,00 | R$ 449.999,00 | R$ 75.000,00 |
| **VIDA** | R$ 500.000,00 | R$ 125.000,00 | R$ 799.999,00 | R$ 200.000,00 |
| **RESIDENCIAL** | R$ 500.000,00 | R$ 150.000,00 | R$ 449.999,00 | N/A |
| **OUTROS** | R$ 255.000,00 | N/A | R$ 375.000,00 | R$ 55.000,00 |

*Se o valor solicitado exceder o limite para o perfil de risco, a apólice é automaticamente **REJEITADA**.*

---

## 🛠️ Tech Stack

*   **Java 17 & Spring Boot 3**: Performance e produtividade.
*   **MySQL**: Persistência relacional robusta.
*   **Apache Kafka**: Backbone de eventos para pagamentos e subscrição.
*   **OpenFeign**: Cliente HTTP declarativo para integração com API de Fraudes.
*   **Docker Compose**: Ambiente de desenvolvimento completo em um comando.

---

## 🚀 Como Rodar

### 1. Preparar o Ambiente
Certifique-se de ter Docker e Java 17 instalados.
Suba os serviços de dependência (MySQL, Kafka, Zookeeper, MockServer):

```bash
docker-compose up -d
```

### 2. Iniciar a Aplicação
```bash
./mvnw clean spring-boot:run
```
*A aplicação iniciará na porta `8081`.*

### 3. Executar Testes
Para garantir que tudo está funcionando (incluindo as regras de validação acima):
```bash
./mvnw test
```

---

## 🎮 Simulando o Sistema

Para facilitar os testes, o sistema possui um **"Simulador Automático"** embutido (`SimuladorEventosExternosJob`).

Você não precisa configurar ferramentas complexas de Kafka para ver a mágica acontecer.
1.  **Crie uma Solicitação** via API.
2.  **Aguarde**: O Job roda a cada 10 segundos.
    *   Ele detecta apólices `PENDENTE`.
    *   Simula um pagamento (`CONFIRMED` ✅ ou `REJECTED` ❌).
    *   Simula uma subscrição (`AUTHORIZED` ✅ ou `DENIED` ❌).
3.  **Consulte** o status final.

### Exemplo de Requisição (CURL)

**Criar uma Apólice de Câmbio Automático (Perfil Regular)**
```bash
curl -X POST http://localhost:8081/solicitacoes \
  -H "Content-Type: application/json" \
  -d '{
  "idCliente": "adc56d77-348c-4bf0-908f-22d402ee715c",
  "idProduto": "prod-001",
  "categoria": "AUTO",
  "canalVenda": "MOBILE",
  "formaPagamento": "CARTAO_CREDITO",
  "valorPremioMensalTotal": 150.00,
  "valorSegurado": 80000.00,
  "coberturas": { "Colisao": 80000.00 },
  "assistencias": [ "Guincho 24h", "Carro Reserva" ]
}'
```

**Verificar Status** (Substitua `{id}` pelo UUID retornado):
```bash
curl http://localhost:8081/solicitacoes/{id}
```

---

## 📨 API Reference

| Método | Endpoint | Descrição |
| :--- | :--- | :--- |
| `POST` | `/solicitacoes` | Cria uma nova solicitação de apólice. |
| `GET` | `/solicitacoes/{id}` | Busca detalhes e status atual de uma apólice. |
| `GET` | `/solicitacoes/cliente/{idCliente}` | Lista todas as apólices de um cliente específico. |
| `DELETE` | `/solicitacoes/{id}` | Cancela uma apólice (se ainda não finalizada). |

---

Desenvolvido para o Desafio de Engenharia de Software - Seguradora.
