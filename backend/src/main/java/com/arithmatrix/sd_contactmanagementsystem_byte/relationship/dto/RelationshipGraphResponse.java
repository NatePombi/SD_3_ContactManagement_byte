package com.arithmatrix.sd_contactmanagementsystem_byte.relationship.dto;

import java.util.List;

public record RelationshipGraphResponse(
        List<GraphNodeResponse> nodes,
        List<GraphEdgeResponse> edges
) {}
