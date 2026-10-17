package com.arithmatrix.sd_contactmanagementsystem_byte.relationship.dto;

import com.arithmatrix.sd_contactmanagementsystem_byte.relationship.enums.RelationshipType;

public record RelationshipResponse(
        Long id,

        Long sourceContactId,
        String sourceContactName,

        Long targetContactId,
        String targetContactName,

        RelationshipType relationshipType
) {}
