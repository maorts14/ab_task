# Multi-Source Order Processing

This project accepts orders from two external JSON contracts, validates them, publishes the original validated messages to source-specific Kafka topics, normalizes them asynchronously, and publishes the required target JSON.

## Prerequisites

- Docker Desktop with Docker Compose
- Java 17 only when running Maven directly (Docker builds do not require a host Maven installation)

## Start

```powershell
docker compose up --build
```

The gateway is available at `http://localhost:8080`. Swagger UI is available at `http://localhost:8080/swagger-ui/index.html`.

## Demonstration
for demonstration, you send http requests from any possible way, postman, cmd/poweshell or swagger
http://localhost:8080/swagger-ui/index.html

## Demonstrate Source A

```powershell
Invoke-RestMethod -Method Post -Uri http://localhost:8080/orders/source-a -ContentType 'application/json' -Body '{"orderId":"ORD-10001","customerId":"CUST-501","customerName":"John Smith","country":"US","orderDate":"2026-09-01T10:30:00","productCode":"P100","quantity":2,"unitPrice":125.50}'
```

## Demonstrate Source B

```powershell
Invoke-RestMethod -Method Post -Uri http://localhost:8080/orders/source-b -ContentType 'application/json' -Body '{"order_number":"ORD-20001","customer":{"id":"CUST-842","first_name":"Jane","last_name":"Miller","country_code":"DE"},"created_at":"2026-09-01T11:15:00","item":{"sku":"P200","units":3,"price":80.00}}'
```

Both requests return `202 Accepted` with an `eventId` after Kafka acknowledges the raw message.

## Verify target output

```powershell
docker compose logs mock-destination
```

Look for `destination_received`. The log contains the `eventId` and the target-shaped JSON, including the enriched country, currency, full customer name, and calculated `totalOrderValue`.

## Run tests

```powershell
docker run --rm -v "${PWD}:/workspace" -w /workspace maven:3.9.9-eclipse-temurin-17 mvn -pl order-core test
```

The focused unit tests are in `order-core` and cover both source mappings plus invalid country handling.

## Stop

```powershell
docker compose down
```
