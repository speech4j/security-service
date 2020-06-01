package org.speech4j.securityservice.util;

import lombok.Getter;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

import javax.validation.ConstraintViolation;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.web.reactive.function.BodyInserters.fromValue;

@Component
public class ValidationUtil {

    private Map<String, String> responseBody = new HashMap<>();

    public <T> Mono<? extends ServerResponse> validateMono(Set<ConstraintViolation<T>> errors) {
        StringBuilder errorsMsgs = new StringBuilder();
        for (ConstraintViolation<T> error:errors) {
            errorsMsgs.append("Invalid value: ")
                    .append(error.getInvalidValue())
                    .append(" Error message: ")
                    .append(error.getMessage());
        }
        responseBody.put("message", errorsMsgs.toString());
        return ServerResponse.badRequest()
                .contentType(APPLICATION_JSON)
                .body(fromValue(responseBody));
    }

}
