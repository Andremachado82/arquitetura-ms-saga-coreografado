package br.com.microservices.choreography.inventoryservice.core.saga;

import br.com.microservices.choreography.inventoryservice.core.dtos.Event;
import br.com.microservices.choreography.inventoryservice.core.producers.KafkaProducer;
import br.com.microservices.choreography.inventoryservice.core.utils.JsonUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Slf4j
@RequiredArgsConstructor
@Component
public class SagaExecutionController {

    public static final String SAGA_LOG_ID = "ORDER ID: %s  | TRANSACTION ID %s | EVENT ID %s";

    private final JsonUtil jsonUtil;

    private final KafkaProducer kafkaProducer;

    @Value("${spring.kafka.topic.notify-ending}")
    private String notifyEndingTopic;

    @Value("${spring.kafka.topic.inventory-fail}")
    private String inventoryFailTopic;

    @Value("${spring.kafka.topic.payment-fail}")
    private String paymentFailTopic;



    public void handleSaga(Event event) {
        switch (event.getStatus()) {
            case SUCCESS -> handleSuccess(event);
            case ROLLBACK_PENDING -> handleRollbackPending(event);
            case FAIL -> handleFail(event);
        }
    }

    private void handleSuccess(Event event) {
        log.info("### CURRENT SAGA: {} | SUCCESS | NEXT TOPIC {} | {}",
                event.getSource(), notifyEndingTopic, createSagaId(event));
        sendEvent(event, notifyEndingTopic);
    }

    private void handleRollbackPending(Event event) {
        log.info("### CURRENT SAGA: {} | SENDING TO ROLLBACK CURRENT SERVICE | NEXT TOPIC {} | {}",
                event.getSource(), inventoryFailTopic, createSagaId(event));
        sendEvent(event, inventoryFailTopic);
    }

    private void handleFail(Event event) {
        log.info("### CURRENT SAGA: {} | SENDING TO ROLLBACK PREVIOUS SERVICE | NEXT TOPIC {} | {} ",
                event.getSource(), paymentFailTopic, createSagaId(event));
        sendEvent(event, paymentFailTopic);
    }

    private void sendEvent(Event event, String topic) {
        var json = jsonUtil.toJson(event);
        kafkaProducer.sendEvent(json, topic);
    }

    private String createSagaId(Event event) {
        return String.format(SAGA_LOG_ID, event.getPayload().getId(), event.getTransactionId(), event.getId());
    }
}
