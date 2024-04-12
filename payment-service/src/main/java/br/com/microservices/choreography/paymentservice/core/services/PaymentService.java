package br.com.microservices.choreography.paymentservice.core.services;

import br.com.microservices.choreography.paymentservice.configs.exceptions.ValidationException;
import br.com.microservices.choreography.paymentservice.core.dtos.Event;
import br.com.microservices.choreography.paymentservice.core.dtos.History;
import br.com.microservices.choreography.paymentservice.core.dtos.OrderProducts;
import br.com.microservices.choreography.paymentservice.core.enums.EPaymentStatus;
import br.com.microservices.choreography.paymentservice.core.enums.ESagaStatus;
import br.com.microservices.choreography.paymentservice.core.models.Payment;
import br.com.microservices.choreography.paymentservice.core.producers.KafkaProducer;
import br.com.microservices.choreography.paymentservice.core.repositories.PaymentRepository;
import br.com.microservices.choreography.paymentservice.core.utils.JsonUtil;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Slf4j
@AllArgsConstructor
@Service
public class PaymentService {

    private static final String CURRENT_SOURCE = "PAYMENT_SERVICE";
    private static final Double REDUCE_SUM_VALUE = 0.0;
    private static final Double MIN_AMOUNT_VALUE = 0.1;

    private final JsonUtil jsonUtil;
    private final KafkaProducer kafkaProducer;
    private final PaymentRepository paymentRepository;


    public void realizePayment(Event event) {
        try {
            checkCurrentValidation(event);
            createPendingPayment(event);
            var payment = findByOrderIdAndTransactionId(event);
            validateAmount(payment.getTotalAmount());
            changePaymentToSuccess(payment);
            handleSuccess(event);
        } catch (Exception ex) {
            log.error("Error trying to make payment: ", ex);
            handleFailCurrentNotExecuted(event, ex.getMessage());
        }
        kafkaProducer.sendEvent(jsonUtil.toJson(event));
    }

    public void realizeRefund(Event event) {
        event.setStatus(ESagaStatus.FAIL);
        event.setSource(CURRENT_SOURCE);
        try {
            changePaymentStatusToRefund(event);
            addHistory(event, "Rollback executed for payment");
        } catch (Exception ex) {
            addHistory(event, "Rollback not executed for payment. ".concat(ex.getMessage()));
        }
        kafkaProducer.sendEvent(jsonUtil.toJson(event));
    }

    private void changePaymentStatusToRefund(Event event) {
        var payment = findByOrderIdAndTransactionId(event);
        payment.setStatus(EPaymentStatus.REFUND);
        setEventAmountItems(event, payment);
        save(payment);
    }

    private void checkCurrentValidation(Event event) {
        Boolean existsTransaction =
                paymentRepository.existsByOrderIdAndTransactionId(event.getOrderId(), event.getTransactionId());
        if (existsTransaction) {
            throw new ValidationException("There's another transactionID for this validation.");
        }
    }

    private void createPendingPayment(Event event) {
        var totalAmount = totalAmount(event);
        var totalItems = totalItems(event);
        var payment = Payment
                .builder()
                .orderId(event.getPayload().getId())
                .transactionId(event.getTransactionId())
                .totalAmount(totalAmount)
                .totalItems(totalItems)
                .build();
        save(payment);
        setEventAmountItems(event, payment);
    }

    private double totalAmount(Event event) {
        return event.getPayload().getProducts().stream()
                .map(product -> product.getQuantity() * product.getProduct().getUnitValue())
                .reduce(REDUCE_SUM_VALUE, Double::sum);
    }

    private int totalItems(Event event) {
        return event.getPayload().getProducts().stream()
                .map(OrderProducts::getQuantity)
                .reduce(REDUCE_SUM_VALUE.intValue(), Integer::sum);
    }

    private Payment findByOrderIdAndTransactionId(Event event) {
        return paymentRepository.findByOrderIdAndTransactionId(
                event.getPayload().getId(),
                event.getTransactionId()
        ).orElseThrow(() -> new ValidationException("Payment not found by orderID and transactionID."));
    }

    private void validateAmount(double amount) {
        if (amount < MIN_AMOUNT_VALUE) {
            throw new ValidationException("The minimum amount available is ".concat(MIN_AMOUNT_VALUE.toString()));
        }
    }

    private void changePaymentToSuccess(Payment payment) {
        payment.setStatus(EPaymentStatus.SUCCESS);
        save(payment);
    }

    private void handleSuccess(Event event) {
        event.setStatus(ESagaStatus.SUCCESS);
        event.setSource(CURRENT_SOURCE);
        addHistory(event, "Payment realized successfully!");
    }

    private void addHistory(Event event, String message) {
        var history = History
                .builder()
                .source(event.getSource())
                .status(event.getStatus())
                .message(message)
                .createdAt(LocalDateTime.now())
                .build();
        event.addToHistory(history);
    }

    private void handleFailCurrentNotExecuted(Event event, String message) {
        event.setStatus(ESagaStatus.ROLLBACK_PENDING);
        event.setSource(CURRENT_SOURCE);
        addHistory(event, "Fail to realized payment: ".concat(message));
    }

    private void save(Payment payment) {
        paymentRepository.save(payment);
    }

    private void setEventAmountItems(Event event, Payment payment) {
        event.getPayload().setTotalAmount(payment.getTotalAmount());
        event.getPayload().setTotalItems(payment.getTotalItems());
    }
}
