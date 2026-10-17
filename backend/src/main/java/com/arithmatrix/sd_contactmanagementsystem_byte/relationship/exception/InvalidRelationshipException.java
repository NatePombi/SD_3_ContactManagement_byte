package com.arithmatrix.sd_contactmanagementsystem_byte.relationship.exception;

import com.arithmatrix.sd_contactmanagementsystem_byte.common.exception.BusinessException;

public class InvalidRelationshipException extends BusinessException {
    public InvalidRelationshipException(String message) {
        super(message);
    }
}
