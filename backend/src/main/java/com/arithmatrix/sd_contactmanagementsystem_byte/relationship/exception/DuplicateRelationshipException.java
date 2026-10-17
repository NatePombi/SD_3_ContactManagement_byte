package com.arithmatrix.sd_contactmanagementsystem_byte.relationship.exception;

import com.arithmatrix.sd_contactmanagementsystem_byte.common.exception.ConflictException;

public class DuplicateRelationshipException extends ConflictException {
    public DuplicateRelationshipException(String message) {
        super(message);
    }
}
