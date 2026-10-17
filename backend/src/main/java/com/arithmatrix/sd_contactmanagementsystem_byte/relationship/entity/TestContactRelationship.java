package com.arithmatrix.sd_contactmanagementsystem_byte.relationship.entity;

import com.arithmatrix.sd_contactmanagementsystem_byte.contact.entity.Contact;
import com.arithmatrix.sd_contactmanagementsystem_byte.relationship.enums.RelationshipType;

public class TestContactRelationship extends ContactRelationship{
    public TestContactRelationship(Long id, Contact sourceContact, Contact targetContact, RelationshipType relationshipType) {
        super(id, sourceContact, targetContact, relationshipType);
    }
}
