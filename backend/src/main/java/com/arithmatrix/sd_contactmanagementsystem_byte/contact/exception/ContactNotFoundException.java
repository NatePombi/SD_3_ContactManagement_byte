package com.arithmatrix.sd_contactmanagementsystem_byte.contact.exception;

import com.arithmatrix.sd_contactmanagementsystem_byte.common.exception.ResourceNotFoundException;

public class ContactNotFoundException extends ResourceNotFoundException {
    public ContactNotFoundException(String message) {
        super(message);
    }
}
