package org.speech4j.securityservice.exception;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;
import org.springframework.boot.web.reactive.error.ErrorWebExceptionHandler;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DataBufferFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.HashMap;
import java.util.Map;

@Configuration
public class GlobalErrorHandler implements ErrorWebExceptionHandler {

    private ObjectMapper mapper = new ObjectMapper();

    @SneakyThrows
    @Override
    public Mono<Void> handle(ServerWebExchange exchange, Throwable ex) {
        DataBufferFactory bufferFactory = exchange.getResponse().bufferFactory();
        Map<String, String> errorBody = new HashMap<>();
        if (ex instanceof UserExistsException || ex instanceof DataOperationException) {
            exchange.getResponse().setStatusCode(HttpStatus.BAD_REQUEST);
            return getJsonMessage(exchange, bufferFactory, errorBody);
        }
        if (ex instanceof UserNotFoundException) {
            exchange.getResponse().setStatusCode(HttpStatus.NOT_FOUND);
            return getJsonMessage(exchange, bufferFactory, errorBody);
        }

        exchange.getResponse().setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR);
        exchange.getResponse().getHeaders().setContentType(MediaType.APPLICATION_JSON);
        errorBody.put("message", "unknown server-side error");
        DataBuffer dataBuffer = bufferFactory.wrap(mapper.writeValueAsBytes(errorBody));
        return exchange.getResponse().writeWith(Mono.just(dataBuffer));
    }

    private Mono<Void> getJsonMessage(ServerWebExchange exchange, DataBufferFactory bufferFactory, Map<String, String> errorBody) {
        DataBuffer dataBuffer;
        try {
            dataBuffer = bufferFactory.wrap(mapper.writeValueAsBytes(errorBody));
        } catch (JsonProcessingException e) {
            dataBuffer = bufferFactory.wrap("".getBytes());
        }
        exchange.getResponse().getHeaders().setContentType(MediaType.APPLICATION_JSON);
        return exchange.getResponse().writeWith(Mono.just(dataBuffer));
    }
}
