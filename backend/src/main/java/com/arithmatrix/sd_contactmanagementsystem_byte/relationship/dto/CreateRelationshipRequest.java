package com.arithmatrix.sd_contactmanagementsystem_byte.relationship.dto;

import com.arithmatrix.sd_contactmanagementsystem_byte.relationship.enums.RelationshipType;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record CreateRelationshipRequest(
        @NotNull(message = "Source contact ID is required")
        @Positive(message = "Source contact ID must be positive")
        Long sourceContactId,

        @NotNull(message = "Target contact ID is required")
        @Positive(message = "Target contact ID must be positive")
        Long targetContactId,

        @NotNull(message = "Relationship type is required")
        RelationshipType relationshipType
) {}
