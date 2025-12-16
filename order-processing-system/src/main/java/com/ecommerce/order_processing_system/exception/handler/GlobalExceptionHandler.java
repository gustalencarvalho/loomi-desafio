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
        problem.setDetail(ex.getMessage());
        problem.setProperty("timestamp", Instant.now());
        return problem;
    }

    @ExceptionHandler(OrderNotFoundException.class)
    ProblemDetail handleOrderNotFoundException(final OrderNotFoundException ex, final HttpServletRequest request) {
        ProblemDetail problem = ProblemDetail.forStatus(NOT_FOUND);
        problem.setType(URI.create(request.getContextPath()));
        problem.setDetail(ex.getMessage());
        problem.setProperty("timestamp", Instant.now());
        return problem;
    }

    @ExceptionHandler(ProductIsNotAvailableException.class)
    ProblemDetail handleProductIsNotAvailableException(final ProductIsNotAvailableException ex, final HttpServletRequest request) {
        ProblemDetail problem = ProblemDetail.forStatus(BAD_REQUEST);
        problem.setType(URI.create(request.getContextPath()));
        problem.setDetail(ex.getMessage());
        problem.setProperty("timestamp", Instant.now());
        return problem;
    }

    @ExceptionHandler(QuantityInvalidException.class)
    ProblemDetail handleQuantityInvalidException(final QuantityInvalidException ex, final HttpServletRequest request) {
        ProblemDetail problem = ProblemDetail.forStatus(BAD_REQUEST);
        problem.setType(URI.create(request.getContextPath()));
        problem.setDetail(ex.getMessage());
        problem.setProperty("timestamp", Instant.now());
        return problem;
    }

    @ExceptionHandler(ErrorSystemDefaultException.class)
    ProblemDetail handleErrorSystemException(final ErrorSystemDefaultException ex, final HttpServletRequest request) {
        ProblemDetail problem = ProblemDetail.forStatus(INTERNAL_SERVER_ERROR);
        problem.setType(URI.create(request.getContextPath()));
        problem.setDetail(ex.getMessage());
        problem.setProperty("timestamp", Instant.now());
        return problem;
    }

    @ExceptionHandler(OutOfStockException.class)
    ProblemDetail handleOutOfStockExpcetion(final OutOfStockException ex, final HttpServletRequest request) {
        ProblemDetail problem = ProblemDetail.forStatus(BAD_REQUEST);
        problem.setType(URI.create(request.getContextPath()));
        problem.setDetail(ex.getMessage());
        problem.setProperty("timestamp", Instant.now());
        return problem;
    }

    @ExceptionHandler(WarehouseUnavailableException.class)
    ProblemDetail handleWareHouseUnaivalableException(final WarehouseUnavailableException ex, final HttpServletRequest request) {
        ProblemDetail problem = ProblemDetail.forStatus(INTERNAL_SERVER_ERROR);
        problem.setType(URI.create(request.getContextPath()));
        problem.setDetail(ex.getMessage());
        problem.setProperty("timestamp", Instant.now());
        return problem;
    }

    @ExceptionHandler(SubscriptionLimitExceededException.class)
    ProblemDetail handleSubscriptionLimitExceededException(final SubscriptionLimitExceededException ex, final HttpServletRequest request) {
        ProblemDetail problem = ProblemDetail.forStatus(BAD_REQUEST);
        problem.setType(URI.create(request.getContextPath()));
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
        problem.setDetail(ex.getMessage());
        problem.setProperty("timestamp", Instant.now());
        return problem;
    }

    @ExceptionHandler(FraudDetectedException.class)
    ProblemDetail handleFraudDetectedException(final FraudDetectedException ex, final HttpServletRequest request) {
        ProblemDetail problem = ProblemDetail.forStatus(BAD_REQUEST);
        problem.setType(URI.create(request.getContextPath()));
        problem.setDetail(ex.getMessage());
        problem.setProperty("timestamp", Instant.now());
        return problem;
    }

    @ExceptionHandler(FeignException.class)
    ProblemDetail handleFeignException(final FeignException ex, final HttpServletRequest request) {
        ProblemDetail problem = ProblemDetail.forStatus(NOT_FOUND);
        problem.setType(URI.create(request.getContextPath()));
        problem.setDetail("Product not found or inactive");
        problem.setProperty("timestamp", Instant.now());
        return problem;
    }

    @ExceptionHandler(LicenseUnavailableException.class)
    ProblemDetail handleLicenseUnavailableException(final LicenseUnavailableException ex, final HttpServletRequest request) {
        ProblemDetail problem = ProblemDetail.forStatus(BAD_REQUEST);
        problem.setType(URI.create(request.getContextPath()));
        problem.setDetail(ex.getMessage());
        problem.setProperty("timestamp", Instant.now());
        return problem;
    }

    @ExceptionHandler(AlreadyOwnedDigitalProductException.class)
    ProblemDetail handleAlreadyOwnedDigitalProductException(final AlreadyOwnedDigitalProductException ex, final HttpServletRequest request) {
        ProblemDetail problem = ProblemDetail.forStatus(BAD_REQUEST);
        problem.setType(URI.create(request.getContextPath()));
        problem.setDetail(ex.getMessage());
        problem.setProperty("timestamp", Instant.now());
        return problem;
    }

    @ExceptionHandler(ReleaseDatePassedException.class)
    ProblemDetail handleReleaseDatePassedException(final ReleaseDatePassedException ex, final HttpServletRequest request) {
        ProblemDetail problem = ProblemDetail.forStatus(BAD_REQUEST);
        problem.setType(URI.create(request.getContextPath()));
        problem.setDetail(ex.getMessage());
        problem.setProperty("timestamp", Instant.now());
        return problem;
    }

    @ExceptionHandler(CreditLimitExceededException.class)
    ProblemDetail handleCreditLimitExceededException(final CreditLimitExceededException ex, final HttpServletRequest request) {
        ProblemDetail problem = ProblemDetail.forStatus(BAD_REQUEST);
        problem.setType(URI.create(request.getContextPath()));
        problem.setDetail(ex.getMessage());
        problem.setProperty("timestamp", Instant.now());
        return problem;
    }

    @ExceptionHandler(PreOrderSoldOutException.class)
    ProblemDetail handlePreOrderSoldOutException(final PreOrderSoldOutException ex, final HttpServletRequest request) {
        ProblemDetail problem = ProblemDetail.forStatus(BAD_REQUEST);
        problem.setType(URI.create(request.getContextPath()));
        problem.setDetail(ex.getMessage());
        problem.setProperty("timestamp", Instant.now());
        return problem;
    }

    @ExceptionHandler(InvalidCorporateDataException.class)
    ProblemDetail handleInvalidCorporateDataException(final InvalidCorporateDataException ex, final HttpServletRequest request) {
        ProblemDetail problem = ProblemDetail.forStatus(BAD_REQUEST);
        problem.setType(URI.create(request.getContextPath()));
        problem.setDetail(ex.getMessage());
        problem.setProperty("timestamp", Instant.now());
        return problem;
    }

    @ExceptionHandler(InvalidPaymentTermsException.class)
    ProblemDetail handleInvalidPaymentTermsException(final InvalidPaymentTermsException ex, final HttpServletRequest request) {
        ProblemDetail problem = ProblemDetail.forStatus(BAD_REQUEST);
        problem.setType(URI.create(request.getContextPath()));
        problem.setDetail(ex.getMessage());
        problem.setProperty("timestamp", Instant.now());
        return problem;
    }

    @ExceptionHandler(UnsupportedOperationException.class)
    ProblemDetail handleUnsupportedOperationException(final UnsupportedOperationException ex, final HttpServletRequest request) {
        ProblemDetail problem = ProblemDetail.forStatus(BAD_REQUEST);
        problem.setType(URI.create(request.getContextPath()));
        problem.setDetail(ex.getMessage());
        problem.setProperty("timestamp", Instant.now());
        return problem;
    }

    @ExceptionHandler(SlotLimitException.class)
    ProblemDetail handleSlotLimitException(final SlotLimitException ex, final HttpServletRequest request) {
        ProblemDetail problem = ProblemDetail.forStatus(BAD_REQUEST);
        problem.setType(URI.create(request.getContextPath()));
        problem.setDetail(ex.getMessage());
        problem.setProperty("timestamp", Instant.now());
        return problem;
    }
}