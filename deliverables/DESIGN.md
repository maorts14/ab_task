# Design

## Architecture

```text
Source A → POST /orders/source-a → raw-orders.source-a.v1 ┐
                                                           ├→  Normalizer Workers
Source B → POST /orders/source-b → raw-orders.source-b.v1 ┘
                                                                     ↓
                                                             processed-orders.v1
                                                                     ↓
                                                         Mock Destination logs JSON
```

The Gateway, Normalizer Worker, and mock Destination are separate containerized applications. `order-core` is a shared Java module containing the source contracts, validation/normalization logic, and country lookup.

## Processing flow

1. A source calls its explicit HTTP route.
2. The Gateway fully validates the source contract and returns `400` for invalid data.
3. The Gateway publishes the unchanged valid JSON to that source's raw topic. It returns `202` with an `eventId` only after Kafka acknowledges publication.
4. The source-specific Normalizer Worker consumes the raw event, maps it to the target structure, enriches country details, calculates the total value and publishes to `processed-orders.v1`.
5. The mock Destination independently consumes and logs the processed event.

## Technical decisions

| Decision | Choice | Why |
| --- | --- | --- |
| Transport | Kafka | Load handling, downtime recovery |
| Source isolation | One raw topic and worker group per source | A busy provider cannot consume the other provider's normalization capacity. |
| Message value | Original JSON in raw topics; target JSON in processed topic | Raw data can be replayed; the destination receives its stated contract. |
| Message key | `sourceSystem:orderReference` | Stable identity within each source. |
| Delivery | At least once | our service is not the business wise owner of the data, so he cant decide how to handle duplication, thats related to the downsream services |
| Money | `BigDecimal` | Avoids floating-point rounding errors. |
| Country data | In-memory `CountryCatalog` | easy to implement dosnt seem in scope to over complicate |
| Failure handling | Direct DLQ, no actual handling | felt out of scope, but allow further implementation for production standart. |

## Assumptions

- `sourceSystem + orderReference` uniquely identifies an immutable order. Repeated submission is intentionally republished.
- The Destination System is responsible for idempotent processing of repeated business orders.
- The destination service is also ours and therefore its comfortable to commincate with him over kafka topic.

## Production concerns not fully implemented

| Concern | Production approach |
| --- | --- |
| Kafka availability | Deploy against a configured multi-broker cluster with replication, ISR policy, and monitored retention. |
| Broker security | Supply broker addresses and TLS/SASL credentials via deployment configuration and secrets. |
| Scale | Increase raw-topic partitions and independently scale the appropriate normalizer consumer group according to lag with an autoscaler like keda |
| Authentication | Authenticate webhook callers with mTLS, OAuth client credentials, or signed requests. |
| Observability | Export structured logs, metrics, tracing, and alerts for HTTP failures, publish failures, DLQ writes, and consumer lag. |

## Alternatives considered

- **Synchronous target HTTP delivery:** rejected because a target outage would make source ingestion unavailable.
- **One raw topic:** rejected because an uneven source distribution could consume shared normalizer capacity.
- **Database queue:** viable at this volume, but Kafka better for demonstrating production mindset.

## Questions before production

1. What authentication and source identity contract will each provider support?
2. What retention, privacy, and replay policy applies to raw order data?
3. What idempotency behavior does the real destination provide?


## Evolve to production

1. processing volume - in the production kafka cluster we will habve more partitions and therefore be able to increase amount of consumers.
we can also dinamically add partitions for ongoing spikes.
2. number of source systems increased - we will have to create another topic, register it to the api(that require adaptations becuase the current version is fixed to the existing sources) and implement
3. i whould also make a load test, which i didnt have time to implement.
i whould
