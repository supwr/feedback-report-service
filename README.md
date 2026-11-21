# feedback-report-service

Worker em Quarkus para AWS Lambda acionado por SQS (`feedbacks-report`).

## Evento de entrada (SQS body)

```json
{
  "date": "2026-11-20",
  "periodWeek": "2026-W47"
}
```

## Fluxo

1. Lambda recebe evento SQS.
2. Serviço consulta feedbacks no DynamoDB por `periodWeek`.
3. Gera CSV com os feedbacks encontrados.
4. Salva o arquivo no bucket S3 `feedback-report` (configurável).

## Build local

```bash
./mvnw clean test
```

## Build nativo para Lambda

```bash
./mvnw clean package -Plambda -Dquarkus.native.enabled=true -Dquarkus.native.container-build=true
```
