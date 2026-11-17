# mechanical-hub-repair-service

Microsserviço de **catálogo, diagnóstico e execução de reparos** do Mechanical Hub (Fase 4 — Tech Challenge). Dono do catálogo de serviços/materiais e das tarefas de execução de cada Ordem de Serviço (OS), e participante ativo do Saga Pattern orquestrado pelo [`core-service`](https://github.com/estefanyneres/mechanical-hub-core-service).

## Arquitetura

Clean Architecture (`domain` / `application` / `infrastructure`):

- **domain** — `OrderTask`, `ServiceData`, `Material`, `MaterialRequirement` (materiais padrão de cada serviço do catálogo).
- **application** — `OrderTaskUseCase` (criação/atualização de tarefas), `ServiceUseCase`, `MaterialUseCase`, `StockReservationUseCase`.
- **infrastructure** — DynamoDB (single-table), segurança JWT stateless, mensageria, **dois clientes HTTP síncronos pra outros serviços** (`StockReservationGatewayHttpClient`, `CoreServiceGatewayHttpClient`), autenticados com um JWT de serviço-pra-serviço de curta duração (`ServiceTokenIssuer`, perfil `ADMINISTRATOR`, mesmo segredo compartilhado do core-service).

### Papel no Saga

Reserva de material só acontece **depois do pagamento confirmado** — nunca na criação da tarefa. Fluxo:

1. Escuta `pagamento.confirmado` (billing-service) → chama `POST /stock/reservations` no stock-service, síncrono
2. Reserva OK → publica `material.reservado` · Reserva pendente → publica `material.indisponivel`
3. Escuta também `estoque.reabastecido` (stock-service) pra tentar reservar de novo quando repuser estoque
4. Mecânico marca a **primeira** tarefa da OS como `INICIADO` → publica `execucao.iniciada` (core-service move a OS pra "Em execução")
5. Última tarefa pendente finalizada (`FINALIZADO`) → publica `execucao.finalizada`
6. Falha reportada durante a execução já iniciada → publica `execucao.falhou` (core-service volta a OS pra diagnóstico)

**Antes de aceitar `INICIADO`**, confirma de forma síncrona com o core-service (`GET /service-orders/{id}/status`) que a OS está em "Pronto para execução" — rejeita com 400 se não estiver, em vez de aceitar e só descobrir o problema depois quando `execucao.finalizada` for rejeitado do outro lado.

**Publica:** `material.reservado`, `material.indisponivel`, `execucao.iniciada`, `execucao.finalizada`, `execucao.falhou`
**Consome:** `pagamento.confirmado`, `estoque.reabastecido`

## Tecnologias

- Java 21 / Spring Boot 3.2
- **DynamoDB** (banco não-relacional do conjunto de microsserviços — ADR-001 do core-service), tabela única `mechanical-hub-repair-service`
- RabbitMQ (Spring AMQP)
- Spring Security (JWT stateless)
- Datadog (`dd-java-agent` + Micrometer/statsd)
- springdoc-openapi (Swagger UI)
- JUnit 5 + Mockito + AssertJ

## Rodando localmente

```bash
./mvnw spring-boot:run
```

Variáveis de ambiente em `k8s/configmap.yaml`/`k8s/secret.yaml` (`DYNAMODB_TABLE_NAME`, `STOCK_SERVICE_URL`, `CORE_SERVICE_URL`, `RABBITMQ_HOST`, `JWT_SECRET`).

Swagger UI: `http://localhost:8080/swagger-ui/index.html`

## Testes

```bash
./mvnw test
```

**134 testes**, **93% de cobertura** (gate SonarQube: 80% em código novo). Exclusões do Sonar: só classes de wiring puro do Spring (`SecurityConfiguration`, `RabbitMQConfig`).

## Deploy

- `Dockerfile` multi-stage (`eclipse-temurin:21-jre-jammy` + `dd-java-agent`)
- `k8s/` — namespace, configmap, secret, deployment, service (NodePort `30082`), HPA
- CI/CD: `feature.yml` (build → testes → SonarQube com gate → PR) e `main.yml` (build → push ECR → deploy EKS)

Exposto publicamente via API Gateway nos paths `/materials*`, `/services*`, `/service-orders/{id}/tasks*`.

## Documentação de arquitetura

As decisões de arquitetura compartilhadas entre os 4 microsserviços (decomposição de domínio, Saga orquestrado, RabbitMQ, DynamoDB) estão documentadas no repositório do [`core-service`](https://github.com/estefanyneres/mechanical-hub-core-service/tree/main/docs).
