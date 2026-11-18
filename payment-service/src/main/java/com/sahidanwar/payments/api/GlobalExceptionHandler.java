package com.sahidanwar.payments.api;

import com.sahidanwar.payments.domain.DomainException;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.net.URI;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(DomainException.class)
    ProblemDetail domain(DomainException exception, HttpServletRequest request) {
        ProblemDetail detail = ProblemDetail.forStatusAndDetail(
                HttpStatusCode.valueOf(exception.status()), exception.getMessage());
        detail.setTitle("Request failed");
        detail.setType(URI.create("https://orders.sahidanwar.dev/problems/" + exception.code()));
        detail.setInstance(URI.create(request.getRequestURI()));
        return detail;
    }
}
