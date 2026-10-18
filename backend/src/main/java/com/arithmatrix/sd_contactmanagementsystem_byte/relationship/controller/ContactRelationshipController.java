package com.arithmatrix.sd_contactmanagementsystem_byte.relationship.controller;

import com.arithmatrix.sd_contactmanagementsystem_byte.relationship.dto.CreateRelationshipRequest;
import com.arithmatrix.sd_contactmanagementsystem_byte.relationship.dto.RelationshipGraphResponse;
import com.arithmatrix.sd_contactmanagementsystem_byte.relationship.dto.RelationshipResponse;
import com.arithmatrix.sd_contactmanagementsystem_byte.relationship.service.ContactRelationshipService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/relationships")
@RequiredArgsConstructor
public class ContactRelationshipController {
    private final ContactRelationshipService relationshipService;

    @PostMapping
    public ResponseEntity<RelationshipResponse> createRelationship(@Valid @RequestBody CreateRelationshipRequest request){
        return ResponseEntity.status(HttpStatus.CREATED).body(relationshipService.createRelationship(request));
    }


    @GetMapping("/contact/{contactId}")
    public ResponseEntity<List<RelationshipResponse>> getRelationship(@PathVariable Long contactId){
        return ResponseEntity.ok(relationshipService.getRelationshipsForContact(contactId));
    }


    @GetMapping("/graph")
    public ResponseEntity<RelationshipGraphResponse> getRelationshipGraph(){
        return ResponseEntity.ok(relationshipService.getRelationshipGraph());
    }
}
