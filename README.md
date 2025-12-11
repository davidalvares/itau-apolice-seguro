# Microsserviço de Apólices de Seguros

Este projeto implementa um microsserviço para gestão de solicitações de apólices de seguros, utilizando arquitetura orientada a eventos (EDA).

## Tecnologias

- **Java 17**
- **Spring Boot 3.x**
- **Spring Data JPA (MySQL)**
- **Spring Kafka**
- **Spring Cloud OpenFeign**
- **Docker & Docker Compose**
- **MockServer** (Simulação de API de Fraudes)

## Arquitetura

O sistema gerencia o ciclo de vida da apólice através dos seguintes estados:
`RECEIVED` -> `VALIDATED` -> `PENDING` -> `APPROVED` (ou `REJECTED`/`CANCELED`)

### Integrações
- **API de Fraudes**: Consultada via HTTP (Feign) para classificar o risco do cliente.
- **Pagamentos e Subscrição**: Processados via eventos Kafka (`payment-events`, `subscription-events`).
- **Eventos de Domínio**: Publicados no tópico `policy-events` a cada mudança de estado.

## Como Executar

1. **Subir a Infraestrutura**
   ```bash
   docker-compose up -d
   ```
   Isso iniciará:
   - MySQL (Porta 3306)
   - Kafka (Porta 9092)
   - Zookeeper (Porta 2181)
   - MockServer (Porta 1080)

2. **Compilar e Executar a Aplicação**
   ```bash
   ./mvnw clean spring-boot:run
   ```
   *Nota: Caso ocorram erros de dependência, tente `./mvnw -U clean install -DskipTests`.*

## API Endpoints

- **POST /solicitacoes**: Cria uma nova solicitação.
- **GET /solicitacoes/{id}**: Consulta uma solicitação.
- **DELETE /solicitacoes/{id}**: Cancela uma solicitação.

## Testando

### 1. Criar Solicitação
```http
POST http://localhost:8080/solicitacoes
Content-Type: application/json

{
  "idCliente": "adc56d77-348c-4bf0-908f-22d402ee715c",
  "idProduto": "1b2da7cc-b367-4196-8a78-9cfeec21f587",
  "categoria": "AUTO",
  "canalVenda": "MOBILE",
  "formaPagamento": "CARTAO_CREDITO",
  "valorPremioMensalTotal": 75.25,
  "valorSegurado": 100000.00,
  "coberturas": { "Roubo": 100000.00 },
  "assistencias": [ "Guincho" ]
}
```

### 2. Simular Pagamento (Kafka)
Produza uma mensagem no tópico `payment-events`:
```json
{
  "idApolice": "<ID_DA_APOLICE_UUID>",
  "status": "CONFIRMED"
}
```

### 3. Simular Subscrição (Kafka)
Produza uma mensagem no tópico `subscription-events`:
```json
{
  "idApolice": "<ID_DA_APOLICE_UUID>",
  "status": "AUTHORIZED"
}
```

Ao final, o status da apólice deve ser `APPROVED`.
