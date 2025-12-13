package com.ecommerce.order_processing_system.exception.handler;

import com.ecommerce.order_processing_system.exception.*;
import feign.FeignException;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.net.URI;
import java.time.Instant;
import java.util.List;

import static org.springframework.http.HttpStatus.*;

@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    ProblemDetail handleMethodArgumentNotValidException(final MethodArgumentNotValidException ex, final HttpServletRequest request) {
        ProblemDetail problem = ProblemDetail.forStatus(BAD_REQUEST);
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
        ProblemDetail problem = ProblemDetail.forStatus(NOT_FOUND);
        problem.setType(URI.create(request.getContextPath()));
        problem.setTitle("Product");
        problem.setDetail(ex.getMessage());
        problem.setProperty("timestamp", Instant.now());
        return problem;
    }

    @ExceptionHandler(OrderNotFoundException.class)
    ProblemDetail handleOrderNotFoundException(final OrderNotFoundException ex, final HttpServletRequest request) {
        ProblemDetail problem = ProblemDetail.forStatus(NOT_FOUND);
        problem.setType(URI.create(request.getContextPath()));
        problem.setTitle("Order");
        problem.setDetail(ex.getMessage());
        problem.setProperty("timestamp", Instant.now());
        return problem;
    }

    @ExceptionHandler(ProductIsNotAvailableException.class)
    ProblemDetail handleProductIsNotAvailableException(final ProductIsNotAvailableException ex, final HttpServletRequest request) {
        ProblemDetail problem = ProblemDetail.forStatus(BAD_REQUEST);
        problem.setType(URI.create(request.getContextPath()));
        problem.setTitle("OUT_OF_STOCK");
        problem.setDetail(ex.getMessage());
        problem.setProperty("timestamp", Instant.now());
        return problem;
    }

    @ExceptionHandler(QuantityInvalidException.class)
    ProblemDetail handleQuantityInvalidException(final QuantityInvalidException ex, final HttpServletRequest request) {
        ProblemDetail problem = ProblemDetail.forStatus(BAD_REQUEST);
        problem.setType(URI.create(request.getContextPath()));
        problem.setTitle("OUT_OF_STOCK");
        problem.setDetail(ex.getMessage());
        problem.setProperty("timestamp", Instant.now());
        return problem;
    }

    @ExceptionHandler(ErrorSystemDefaultException.class)
    ProblemDetail handleErrorSystemException(final ErrorSystemDefaultException ex, final HttpServletRequest request) {
        ProblemDetail problem = ProblemDetail.forStatus(INTERNAL_SERVER_ERROR);
        problem.setType(URI.create(request.getContextPath()));
        problem.setTitle(INTERNAL_SERVER_ERROR.name());
        problem.setDetail(ex.getMessage());
        problem.setProperty("timestamp", Instant.now());
        return problem;
    }

    @ExceptionHandler(OutOfStockException.class)
    ProblemDetail handleOutOfStockExpcetion(final OutOfStockException ex, final HttpServletRequest request) {
        ProblemDetail problem = ProblemDetail.forStatus(BAD_REQUEST);
        problem.setType(URI.create(request.getContextPath()));
        problem.setTitle("OUT_OF_STOCK");
        problem.setDetail(ex.getMessage());
        problem.setProperty("timestamp", Instant.now());
        return problem;
    }

    @ExceptionHandler(WarehouseUnavailableException.class)
    ProblemDetail handleWareHouseUnaivalableException(final WarehouseUnavailableException ex, final HttpServletRequest request) {
        ProblemDetail problem = ProblemDetail.forStatus(INTERNAL_SERVER_ERROR);
        problem.setType(URI.create(request.getContextPath()));
        problem.setTitle("WAREHOUSE_UNAVAILABLE");
        problem.setDetail(ex.getMessage());
        problem.setProperty("timestamp", Instant.now());
        return problem;
    }

    @ExceptionHandler(SubscriptionLimitExceededException.class)
    ProblemDetail handleSubscriptionLimitExceededException(final SubscriptionLimitExceededException ex, final HttpServletRequest request) {
        ProblemDetail problem = ProblemDetail.forStatus(BAD_REQUEST);
        problem.setType(URI.create(request.getContextPath()));
        problem.setTitle("SUBSCRIPTION_LIMIT_EXCEEDED");
        problem.setDetail(ex.getMessage());
        problem.setProperty("timestamp", Instant.now());
        return problem;
    }

    @ExceptionHandler(DuplicateActiveSubscriptionException.class)
    ProblemDetail handleDuplicateActiveSubscriptionException(final DuplicateActiveSubscriptionException ex, final HttpServletRequest request) {
        ProblemDetail problem = ProblemDetail.forStatus(BAD_REQUEST);
        problem.setType(URI.create(request.getContextPath()));
        problem.setTitle("DUPLICATE_ACTIVE_SUBSCRIPTION");
        problem.setDetail(ex.getMessage());
        problem.setProperty("timestamp", Instant.now());
        return problem;
    }

    @ExceptionHandler(IncompatibleSubscriptionsException.class)
    ProblemDetail handleIncompatibleSubscriptionsException(final IncompatibleSubscriptionsException ex, final HttpServletRequest request) {
        ProblemDetail problem = ProblemDetail.forStatus(BAD_REQUEST);
        problem.setType(URI.create(request.getContextPath()));
        problem.setTitle("INCOMPATIBLE_SUBSCRIPTIONS");
        problem.setDetail(ex.getMessage());
        problem.setProperty("timestamp", Instant.now());
        return problem;
    }

    @ExceptionHandler(FraudDetectedException.class)
    ProblemDetail handleFraudDetectedException(final FraudDetectedException ex, final HttpServletRequest request) {
        ProblemDetail problem = ProblemDetail.forStatus(BAD_REQUEST);
        problem.setType(URI.create(request.getContextPath()));
        problem.setTitle("FRAUD_ALERT");
        problem.setDetail(ex.getMessage());
        problem.setProperty("timestamp", Instant.now());
        return problem;
    }

    @ExceptionHandler(FeignException.class)
    ProblemDetail handleFeignException(final FeignException ex, final HttpServletRequest request) {
        ProblemDetail problem = ProblemDetail.forStatus(NOT_FOUND);
        problem.setType(URI.create(request.getContextPath()));
        problem.setTitle(NOT_FOUND.name());
        problem.setDetail("Product not found or inactive");
        problem.setProperty("timestamp", Instant.now());
        return problem;
    }
}