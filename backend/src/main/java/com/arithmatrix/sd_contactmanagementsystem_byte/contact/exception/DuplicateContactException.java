package com.arithmatrix.sd_contactmanagementsystem_byte.contact.exception;

import com.arithmatrix.sd_contactmanagementsystem_byte.common.exception.ConflictException;

public class DuplicateContactException extends ConflictException {
    public DuplicateContactException(String message) {
        super(message);
    }
}
