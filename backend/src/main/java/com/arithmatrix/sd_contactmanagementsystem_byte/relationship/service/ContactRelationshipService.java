package com.arithmatrix.sd_contactmanagementsystem_byte.relationship.service;

import com.arithmatrix.sd_contactmanagementsystem_byte.contact.entity.Contact;
import com.arithmatrix.sd_contactmanagementsystem_byte.contact.exception.ContactNotFoundException;
import com.arithmatrix.sd_contactmanagementsystem_byte.contact.repository.ContactRepository;
import com.arithmatrix.sd_contactmanagementsystem_byte.relationship.dto.CreateRelationshipRequest;
import com.arithmatrix.sd_contactmanagementsystem_byte.relationship.dto.RelationshipResponse;
import com.arithmatrix.sd_contactmanagementsystem_byte.relationship.entity.ContactRelationship;
import com.arithmatrix.sd_contactmanagementsystem_byte.relationship.exception.DuplicateRelationshipException;
import com.arithmatrix.sd_contactmanagementsystem_byte.relationship.exception.InvalidRelationshipException;
import com.arithmatrix.sd_contactmanagementsystem_byte.relationship.repository.RelationshipRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ContactRelationshipService {
    private final RelationshipRepository relationshipRepository;
    private final ContactRepository contactRepository;

    @Transactional
    public RelationshipResponse createRelationship(CreateRelationshipRequest request) {
        Long sourceId = request.sourceContactId();
        Long targetId = request.targetContactId();

        if(sourceId.equals(targetId)){
            throw new InvalidRelationshipException("A contact cannot have a relationship with itself");
        }

        Contact sourceContact = contactRepository.findById(sourceId).orElseThrow(()-> new ContactNotFoundException("Source contact not found with ID: "+sourceId));

        Contact targetContact = contactRepository.findById(targetId).orElseThrow(()-> new ContactNotFoundException("Target contact not found with ID: "+targetId));

        boolean alreadyExists = relationshipRepository.existsBySourceContact_IdAndTargetContact_IdAndRelationshipType(sourceId,targetId,request.relationshipType());

        if(alreadyExists){
            throw new DuplicateRelationshipException("Relationship already exists");
        }


        ContactRelationship relationship = ContactRelationship.create(sourceContact,targetContact,request.relationshipType());

        ContactRelationship saved = relationshipRepository.save(relationship);

        return toDto(saved);
    }

    @Transactional
    public List<RelationshipResponse> getRelationshipsForContact(Long contactId){

        if(!contactRepository.existsById(contactId)){
            throw new ContactNotFoundException("Contact not found with ID: " + contactId);
        }

        List<ContactRelationship> contacts = relationshipRepository.findBySourceContact_IdOrTargetContact_Id(contactId,contactId);

        return contacts.stream()
                .map(this::toDto)
                .toList();
    }


    private RelationshipResponse toDto(ContactRelationship relationship){
        if(relationship == null){return null;}

        return new RelationshipResponse(
                relationship.getId(),
                relationship.getSourceContact().getId(),
                relationship.getSourceContact().getFullName(),
                relationship.getTargetContact().getId(),
                relationship.getTargetContact().getFullName(),
                relationship.getRelationshipType()
        );
    }
}
