package com.assignment.order.gateway;

import com.assignment.order.core.OrderNormalizer;
import com.assignment.order.core.SourceSystem;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/orders")
class OrderController {
    private final OrderNormalizer normalizer;
    private final KafkaPublisher publisher;

    OrderController(OrderNormalizer normalizer, KafkaPublisher publisher) {
        this.normalizer = normalizer;
        this.publisher = publisher;
    }

    @PostMapping("/source-a")
    @Operation(summary = "Accept a Source A order")
    ResponseEntity<AcceptedResponse> sourceA(@RequestBody String rawOrder) {
        return accept(SourceSystem.SOURCE_A, rawOrder);
    }

    @PostMapping("/source-b")
    @Operation(summary = "Accept a Source B order")
    ResponseEntity<AcceptedResponse> sourceB(@RequestBody String rawOrder) {
        return accept(SourceSystem.SOURCE_B, rawOrder);
    }

    private ResponseEntity<AcceptedResponse> accept(SourceSystem sourceSystem, String rawOrder) {
        String orderReference = normalizer.validate(sourceSystem, rawOrder);
        String eventId = publisher.publish(sourceSystem, orderReference, rawOrder);
        return ResponseEntity.status(HttpStatus.ACCEPTED).body(new AcceptedResponse(eventId));
    }
}
