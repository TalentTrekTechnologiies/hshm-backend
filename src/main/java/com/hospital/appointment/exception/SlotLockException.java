package com.hospital.appointment.exception;

public class SlotLockException extends RuntimeException {

    public SlotLockException(String message) {
        super(message);
    }
}

