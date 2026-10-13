# AI Usage

I was using codex with the skill grilling-with-docs to create a full plan,discuss archtechture, tradeoffs and caveat, implement the plan, care dockerization and tests.

## Example 1 - Architecture trade-off

- **What I was trying to solve:** The first thing i tried to solve is be able to be able to handle load without choking the api.
i also designed the solution around not making sure every provider data get delivere
- **What AI recommended:** A minimal one-hop Kafka design first, then source-specific raw topics and independently scalable normalizer consumer groups after the isolation requirement was clarified.
- **What I did:** Modified the solution to have internal topics for each provider, and another topic for the destination.
- **Why:** Two raw topics provide a clearer path, making sure each source getting equaly ingested and prevent choking by a larger provider.

## Example 2 - Delivery semantics

- **What I was trying to solve:** Define an honest reliability contract for HTTP ingestion and Kafka publication.
- **What AI recommended:** Return `202` only after broker acknowledgement and describe the result as at-least-once delivery.
- **What I did:** Accepted the recommendation.
- **Why:** It avoids acknowledging messages that were not accepted by Kafka, while correctly acknowledging that source retries can create duplicates.

## Example 3 - Operational failure handling

- **What I was trying to solve:** Keep the first version focused without silently losing unexpected normalization failures.
- **What AI recommended:** Bounded retries before a DLQ.
- **What I did:** Modified the recommendation to direct DLQ handling.
- **Why:** prefered to focus on fewer features in this time frame, with more time to define this mechanism and test it, whould probably go with codex suggestion and modify it a bit

## Validation approach

I reviewed the generated design, retained only components I could explain, added unit tests for both mappings and invalid country data, and used the Docker Compose flow to demonstrate end-to-end processing.
