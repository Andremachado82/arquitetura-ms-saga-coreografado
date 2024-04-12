package br.com.microservices.choreography.productvalidationservice.core.services;

import br.com.microservices.choreography.productvalidationservice.configs.exceptions.ValidationException;
import br.com.microservices.choreography.productvalidationservice.core.dtos.Event;
import br.com.microservices.choreography.productvalidationservice.core.dtos.History;
import br.com.microservices.choreography.productvalidationservice.core.dtos.OrderProducts;
import br.com.microservices.choreography.productvalidationservice.core.models.Validation;
import br.com.microservices.choreography.productvalidationservice.core.producers.KafkaProducer;
import br.com.microservices.choreography.productvalidationservice.core.repositories.ProductRepository;
import br.com.microservices.choreography.productvalidationservice.core.repositories.ValidationRepository;
import br.com.microservices.choreography.productvalidationservice.core.utils.JsonUtil;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

import static br.com.microservices.choreography.productvalidationservice.core.enums.ESagaStatus.*;
import static org.springframework.util.ObjectUtils.isEmpty;

@Slf4j
@AllArgsConstructor
@Service
public class ProductValidationService {

    private static final String CURRENT_SOURCE = "PRODUCT_VALIDATION_SERVICE";

    private final JsonUtil jsonUtil;
    private final KafkaProducer kafkaProducer;
    private final ProductRepository productRepository;
    private final ValidationRepository validationRepository;

    public void validateExistsProducts(Event event) {
        try {
            checkCurrentValidation(event);
            createValidation(event, true);
            handleSuccess(event, "Products are validated successfully.");
        } catch (Exception ex) {
            log.error("Error trying to validate products: ", ex);
            handleFailCurrentNotExecuted(event, ex.getMessage());
        }
        kafkaProducer.sendEvent(jsonUtil.toJson(event), "");
    }

    private void checkCurrentValidation(Event event) {
        validateProductsInformed(event);
        Boolean existsTransaction =
                validationRepository.existsByOrderIdAndTransactionId(event.getOrderId(), event.getTransactionId());
        if (existsTransaction) {
            throw new ValidationException("There's another transactionID for this validation");
        }
        event.getPayload().getProducts().forEach(product -> {
            validateProductInformed(product);
            validateExistingProduct(product.getProduct().getCode());
        });

    }

    private void validateProductsInformed(Event event) {
        if (isEmpty(event.getPayload()) || isEmpty(event.getPayload().getProducts())) {
            throw new ValidationException("Product list is empty!");
        }
        if (isEmpty(event.getPayload().getId()) || isEmpty(event.getPayload().getTransactionId())) {
            throw new ValidationException("OrderID and TransactionID must be informed!");
        }
    }

    private void validateProductInformed(OrderProducts products) {
        if (isEmpty(products.getProduct()) || isEmpty(products.getProduct().getCode())) {
            throw new ValidationException("Product must be informed!");
        }
    }

    private void validateExistingProduct(String code) {
        if (!productRepository.existsByCode(code)) {
            throw new ValidationException("Product does not exists in database!");
        }
    }

    private void createValidation(Event event, Boolean success) {
        var validation = Validation
                .builder()
                .orderId(event.getOrderId())
                .transactionId(event.getTransactionId())
                .success(success)
                .build();
        validationRepository.save(validation);
    }

    private void handleSuccess(Event event, String message) {
        event.setStatus(SUCCESS);
        event.setSource(CURRENT_SOURCE);
        addHistory(event, message);
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
        event.setStatus(ROLLBACK_PENDING);
        event.setSource(CURRENT_SOURCE);
        addHistory(event, "Fail to validate products: ".concat(message));
    }

    public void rollbackEvent(Event event) {
        changeValidationToFail(event);
        event.setStatus(FAIL);
        event.setSource(CURRENT_SOURCE);
        addHistory(event, "Rollback executed on product validation");
        kafkaProducer.sendEvent(jsonUtil.toJson(event), "");
    }

    private void changeValidationToFail(Event event) {
        validationRepository.findByOrderIdAndTransactionId(event.getPayload().getId(), event.getTransactionId())
                .ifPresentOrElse(validation -> {
                    validation.setSuccess(false);
                    validationRepository.save(validation);
                },
                () -> createValidation(event, false));
    }


}
