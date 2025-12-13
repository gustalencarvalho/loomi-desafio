package com.loomi.product_catalog_service.api.handler;

import com.loomi.product_catalog_service.domain.exeception.ProductNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.net.URI;
import java.time.Instant;

@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ProductNotFoundException.class)
    ProblemDetail handleLocationNotFoundException(final ProductNotFoundException ex) {
        ProblemDetail problem = ProblemDetail.forStatus(HttpStatus.NOT_FOUND);
        problem.setType(URI.create("http://localhost:8081/"));
        problem.setTitle("Product not found");
        problem.setDetail(ex.getMessage());
        problem.setProperty("timestamp", Instant.now());
        return problem;
    }
}
