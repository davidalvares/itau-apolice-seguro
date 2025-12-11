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
`RECEBIDO` -> `VALIDADO` -> `PENDENTE` -> `APROVADO` (ou `REJEITADO`/`CANCELADO`)

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

3. **Executar Testes Unitários**
   Para rodar a suíte de testes automatizados (unitários e mocks):
   ```bash
   ./mvnw test
   ```

## Simulação Automática de Eventos

O projeto agora inclui um **Job Agendado** (`SimuladorEventosExternosJob`) que roda a cada 10 segundos.
Este job busca apólices com status `PENDENTE` e simula automaticamente as respostas dos sistemas externos:

1. **Envio de Pagamento**: Simula um evento de pagamento (`CONFIRMED` com 80% de chance).
2. **Autorização de Subscrição**: Se pago, simula um evento de subscrição (`AUTHORIZED` com 90% de chance).

Isso significa que você não precisa mais postar mensagens no Kafka manualmente para ver uma apólice ser aprovada. Basta criá-la via API e aguardar alguns segundos.

## API Endpoints

- **POST /solicitacoes**: Cria uma nova solicitação.
- **GET /solicitacoes/{id}**: Consulta uma solicitação.
- **GET /solicitacoes/cliente/{idCliente}**: Consulta apólices de um cliente.
- **DELETE /solicitacoes/{id}**: Cancela uma solicitação.

## Mão na Massa

### 1. Criar Solicitação
```http
POST http://localhost:8081/solicitacoes
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

### 2. Acompanhar Status
Use o **ID** retornado na criação para consultar o estado. Você verá ele transitar automaticamente de `RECEBIDO` para `PENDENTE` e, em seguida (pelo job), para `APROVADO` ou `REJEITADO`.

```http
GET http://localhost:8081/solicitacoes/{id}
```
