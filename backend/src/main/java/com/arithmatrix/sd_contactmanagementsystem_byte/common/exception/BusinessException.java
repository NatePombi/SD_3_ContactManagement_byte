package com.arithmatrix.sd_contactmanagementsystem_byte.common.exception;

public abstract class BusinessException extends RuntimeException {
    public BusinessException(String message) {
        super(message);
    }
}
