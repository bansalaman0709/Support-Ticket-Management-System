package com.supportticket.web.dto;

import java.util.ArrayList;
import java.util.List;

public class ErrorResponse {

    private String message;
    private List<FieldErrorDetail> errors = new ArrayList<>();

    public ErrorResponse() {
    }

    public ErrorResponse(String message) {
        this.message = message;
    }

    public ErrorResponse(String message, List<FieldErrorDetail> errors) {
        this.message = message;
        this.errors = errors != null ? errors : new ArrayList<>();
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public List<FieldErrorDetail> getErrors() {
        return errors;
    }

    public void setErrors(List<FieldErrorDetail> errors) {
        this.errors = errors;
    }
}
