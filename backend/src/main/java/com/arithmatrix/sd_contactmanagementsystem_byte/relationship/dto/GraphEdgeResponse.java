package com.arithmatrix.sd_contactmanagementsystem_byte.relationship.dto;


import com.arithmatrix.sd_contactmanagementsystem_byte.relationship.enums.RelationshipType;

public record GraphEdgeResponse(
        Long id,
        Long sourceContactId,
        Long targetContactId,
        RelationshipType relationType
) {}
