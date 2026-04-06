package com.fip.appointmentapi.exception;

public class InvalidAllocationException extends RuntimeException {
    public InvalidAllocationException(String message) {
        super(message);
    }
}