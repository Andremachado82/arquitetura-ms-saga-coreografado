package br.com.microservices.choreography.orderservice.core.documents;

import br.com.microservices.choreography.orderservice.core.enums.ESagaStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class History {

    private String source;

    private ESagaStatus status;

    private String message;

    private LocalDateTime createdAt;
}
