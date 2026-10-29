package com.ecommerce.composite.exception;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClientResponseException;

import java.net.URI;

@RestControllerAdvice
public class ApiExceptionHandler {

    @ExceptionHandler(RestClientResponseException.class)
    public ResponseEntity<ProblemDetail> handleUpstreamHttpError(
            RestClientResponseException exception,
            HttpServletRequest request
    ) {
        HttpStatus status = exception.getStatusCode().value() == HttpStatus.NOT_FOUND.value()
                ? HttpStatus.NOT_FOUND
                : HttpStatus.BAD_GATEWAY;
        String detail = status == HttpStatus.NOT_FOUND
                ? "The product or its inventory could not be found."
                : "A dependent service returned an error.";

        return problem(status, detail, request);
    }

    @ExceptionHandler(ResourceAccessException.class)
    public ResponseEntity<ProblemDetail> handleUpstreamConnectionError(
            ResourceAccessException exception,
            HttpServletRequest request
    ) {
        return problem(
                HttpStatus.BAD_GATEWAY,
                "A dependent service could not be reached.",
                request
        );
    }

    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<ProblemDetail> handleInvalidUpstreamResponse(
            IllegalStateException exception,
            HttpServletRequest request
    ) {
        return problem(
                HttpStatus.BAD_GATEWAY,
                "A dependent service returned an invalid response.",
                request
        );
    }

    private ResponseEntity<ProblemDetail> problem(
            HttpStatus status,
            String detail,
            HttpServletRequest request
    ) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(status, detail);
        problem.setInstance(URI.create(request.getRequestURI()));
        return ResponseEntity.status(status).body(problem);
    }
}
