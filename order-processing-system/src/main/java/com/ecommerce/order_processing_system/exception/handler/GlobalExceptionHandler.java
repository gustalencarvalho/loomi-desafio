package com.ecommerce.order_processing_system.exception.handler;

import com.ecommerce.order_processing_system.exception.*;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.net.URI;
import java.time.Instant;
import java.util.List;

@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    ProblemDetail handleMethodArgumentNotValidException(final MethodArgumentNotValidException ex, final HttpServletRequest request) {
        ProblemDetail problem = ProblemDetail.forStatus(HttpStatus.BAD_REQUEST);
        problem.setType(URI.create(request.getContextPath()));
        problem.setTitle("Payload invalid");

        List<String> errors = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(err -> err.getField() + ": " + err.getDefaultMessage())
                .toList();

        problem.setProperty("errors", errors);
        return problem;
    }

    @ExceptionHandler(ProductNotFoundException.class)
    ProblemDetail handleProductNotFoundException(final ProductNotFoundException ex, final HttpServletRequest request) {
        ProblemDetail problem = ProblemDetail.forStatus(HttpStatus.NOT_FOUND);
        problem.setType(URI.create(request.getContextPath()));
        problem.setTitle("Product");
        problem.setDetail(ex.getMessage());
        problem.setProperty("timestamp", Instant.now());
        return problem;
    }

    @ExceptionHandler(OrderNotFoundException.class)
    ProblemDetail handleOrderNotFoundException(final OrderNotFoundException ex, final HttpServletRequest request) {
        ProblemDetail problem = ProblemDetail.forStatus(HttpStatus.NOT_FOUND);
        problem.setType(URI.create(request.getContextPath()));
        problem.setTitle("Order");
        problem.setDetail(ex.getMessage());
        problem.setProperty("timestamp", Instant.now());
        return problem;
    }

    @ExceptionHandler(ProductIsNotAvailableException.class)
    ProblemDetail handleProductIsNotAvailableException(final ProductIsNotAvailableException ex, final HttpServletRequest request) {
        ProblemDetail problem = ProblemDetail.forStatus(HttpStatus.BAD_REQUEST);
        problem.setType(URI.create(request.getContextPath()));
        problem.setTitle("Product available");
        problem.setDetail(ex.getMessage());
        problem.setProperty("timestamp", Instant.now());
        return problem;
    }

    @ExceptionHandler(QuantityInvalidException.class)
    ProblemDetail handleQuantityInvalidException(final QuantityInvalidException ex, final HttpServletRequest request) {
        ProblemDetail problem = ProblemDetail.forStatus(HttpStatus.BAD_REQUEST);
        problem.setType(URI.create(request.getContextPath()));
        problem.setTitle("Product");
        problem.setDetail(ex.getMessage());
        problem.setProperty("timestamp", Instant.now());
        return problem;
    }

    @ExceptionHandler(ErrorSystemException.class)
    ProblemDetail handleErrorSystemException(final ErrorSystemException ex, final HttpServletRequest request) {
        ProblemDetail problem = ProblemDetail.forStatus(HttpStatus.INTERNAL_SERVER_ERROR);
        problem.setType(URI.create(request.getContextPath()));
        problem.setTitle("Error system");
        problem.setDetail(ex.getMessage());
        problem.setProperty("timestamp", Instant.now());
        return problem;
    }

    @ExceptionHandler(OutOfStockException.class)
    ProblemDetail handleOutOfStockExpcetion(final OutOfStockException ex, final HttpServletRequest request) {
        ProblemDetail problem = ProblemDetail.forStatus(HttpStatus.BAD_REQUEST);
        problem.setType(URI.create(request.getContextPath()));
        problem.setTitle("Product Stock");
        problem.setDetail(ex.getMessage());
        problem.setProperty("timestamp", Instant.now());
        return problem;
    }

    @ExceptionHandler(WarehouseUnavailableException.class)
    ProblemDetail handleWareHouseUnaivalableException(final WarehouseUnavailableException ex, final HttpServletRequest request) {
        ProblemDetail problem = ProblemDetail.forStatus(HttpStatus.INTERNAL_SERVER_ERROR);
        problem.setType(URI.create(request.getContextPath()));
        problem.setTitle("Product");
        problem.setDetail(ex.getMessage());
        problem.setProperty("timestamp", Instant.now());
        return problem;
    }
}