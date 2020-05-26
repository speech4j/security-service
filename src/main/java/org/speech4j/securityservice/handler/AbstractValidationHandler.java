package org.speech4j.securityservice.handler;

import lombok.extern.slf4j.Slf4j;
import org.speech4j.securityservice.dto.validation.Existing;
import org.speech4j.securityservice.dto.validation.New;
import org.springframework.http.HttpStatus;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Mono;

@Slf4j
public abstract class AbstractValidationHandler<T, U extends Validator> {

    private final Class<T> validationClass;

    private final U validator;

    protected AbstractValidationHandler(Class<T> clazz, U validator) {
        this.validationClass = clazz;
        this.validator = validator;
    }

    public final Mono<ServerResponse> handleRequest(final ServerRequest request, String id) {
        return request.bodyToMono(this.validationClass)
            .flatMap(body -> {
                Errors errors = new BeanPropertyBindingResult(body, this.validationClass.getName());
                if (id == null) {
                    validator.validate(body, errors);
                } else {
                    validator.validate(body, errors);
                }
                if (errors.getAllErrors().isEmpty()) {
                    LOGGER.debug("Validating entity: {}", body);
                    return processBody(body, request, id);
                } else {
                    LOGGER.error("Validating entity: {} with errors: {}", body, errors.getErrorCount());
                    return onValidationErrors(errors);
                }
            });
    }

    protected Mono<ServerResponse> onValidationErrors(Errors errors) {
        throw new ResponseStatusException(HttpStatus.BAD_REQUEST, errors.getAllErrors().toString());
    }

    abstract protected Mono<ServerResponse> processBody(
            T validBody, ServerRequest originalRequest, String id);
}


