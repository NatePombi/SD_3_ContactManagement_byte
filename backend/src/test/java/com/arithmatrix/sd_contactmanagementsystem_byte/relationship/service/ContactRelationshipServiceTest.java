package com.arithmatrix.sd_contactmanagementsystem_byte.relationship.service;

import com.arithmatrix.sd_contactmanagementsystem_byte.contact.entity.Contact;
import com.arithmatrix.sd_contactmanagementsystem_byte.contact.entity.TestContact;
import com.arithmatrix.sd_contactmanagementsystem_byte.contact.exception.ContactNotFoundException;
import com.arithmatrix.sd_contactmanagementsystem_byte.contact.repository.ContactRepository;
import com.arithmatrix.sd_contactmanagementsystem_byte.relationship.dto.CreateRelationshipRequest;
import com.arithmatrix.sd_contactmanagementsystem_byte.relationship.dto.RelationshipResponse;
import com.arithmatrix.sd_contactmanagementsystem_byte.relationship.entity.ContactRelationship;
import com.arithmatrix.sd_contactmanagementsystem_byte.relationship.entity.TestContactRelationship;
import com.arithmatrix.sd_contactmanagementsystem_byte.relationship.enums.RelationshipType;
import com.arithmatrix.sd_contactmanagementsystem_byte.relationship.exception.DuplicateRelationshipException;
import com.arithmatrix.sd_contactmanagementsystem_byte.relationship.exception.InvalidRelationshipException;
import com.arithmatrix.sd_contactmanagementsystem_byte.relationship.repository.RelationshipRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ContactRelationshipServiceTest {
    @Mock
    RelationshipRepository relationshipRepository;
    @Mock
    ContactRepository contactRepository;
    @InjectMocks
    ContactRelationshipService contactRelationshipService;

    @Test
    void shouldCreateRelationship() {
        Contact contact1 = new TestContact(12L, "Tester One", "test1@gmail.com", "0502489632");
        Contact contact2 = new TestContact(13L, "Tester Two", "test2@gmail.com", "05078489632");


        CreateRelationshipRequest request = new CreateRelationshipRequest(12L, 13L, RelationshipType.MANAGES);

        when(contactRepository.findById(12L)).thenReturn(java.util.Optional.of(contact1));
        when(contactRepository.findById(13L)).thenReturn(java.util.Optional.of(contact2));
        when(relationshipRepository.existsBySourceContact_IdAndTargetContact_IdAndRelationshipType(12L, 13L, RelationshipType.MANAGES)).thenReturn(false);
        when(relationshipRepository.save(any(ContactRelationship.class))).thenAnswer(invocation -> {
            ContactRelationship relationship = invocation.getArgument(0);

            ReflectionTestUtils.setField(relationship, "id", 10L);

            return relationship;
        });

        RelationshipResponse response = contactRelationshipService.createRelationship(request);

        assertNotNull(response);

        assertEquals(10L, response.id());
        assertEquals(12L, response.sourceContactId());
        assertEquals("Tester One", response.sourceContactName());
        assertEquals(13L, response.targetContactId());
        assertEquals("Tester Two", response.targetContactName());
        assertEquals(RelationshipType.MANAGES, response.relationshipType());

        ArgumentCaptor<ContactRelationship> captor = ArgumentCaptor.forClass(ContactRelationship.class);

        verify(relationshipRepository).save(captor.capture());

        ContactRelationship saved = captor.getValue();

        assertSame(contact1, saved.getSourceContact());
        assertSame(contact2, saved.getTargetContact());

        assertEquals(RelationshipType.MANAGES, saved.getRelationshipType());

        verify(relationshipRepository).save(any(ContactRelationship.class));

    }

    @Test
    void shouldFailCreateRelationship_SelfRelationship() {
        CreateRelationshipRequest request = new CreateRelationshipRequest(12L, 12L, RelationshipType.MANAGES);

        assertThrows(InvalidRelationshipException.class, () -> {
            contactRelationshipService.createRelationship(request);
        });

        verify(relationshipRepository, never()).save(any(ContactRelationship.class));

        verifyNoInteractions(contactRepository);
        verifyNoInteractions(relationshipRepository);
    }

    @Test
    void shouldFailCreateRelationship_SourceContactNotFound() {
        CreateRelationshipRequest request = new CreateRelationshipRequest(12L, 13L, RelationshipType.MANAGES);

        when(contactRepository.findById(12L)).thenThrow(ContactNotFoundException.class);

        assertThrows(ContactNotFoundException.class, () -> {
            contactRelationshipService.createRelationship(request);
        });

        verify(contactRepository).findById(12L);
        verifyNoInteractions(relationshipRepository);
    }

    @Test
    void shouldFailCreateRelationship_TargetContactNotFound() {
        CreateRelationshipRequest request = new CreateRelationshipRequest(12L, 13L, RelationshipType.MANAGES);

        when(contactRepository.findById(12L)).thenReturn(Optional.of(mock(Contact.class)));
        when(contactRepository.findById(13L)).thenThrow(ContactNotFoundException.class);

        assertThrows(ContactNotFoundException.class, () -> {
            contactRelationshipService.createRelationship(request);
        });

        verify(contactRepository).findById(12L);
        verify(contactRepository).findById(13L);
        verifyNoInteractions(relationshipRepository);
    }


    @Test
    void shouldFailCreateRelationship_DuplicateRelationships() {
        CreateRelationshipRequest request = new CreateRelationshipRequest(12L, 13L, RelationshipType.MANAGES);

        when(contactRepository.findById(12L)).thenReturn(Optional.of(mock(Contact.class)));
        when(contactRepository.findById(13L)).thenReturn(Optional.of(mock(Contact.class)));
        when(relationshipRepository.existsBySourceContact_IdAndTargetContact_IdAndRelationshipType(12L, 13L, RelationshipType.MANAGES)).thenReturn(true);

        assertThrows(DuplicateRelationshipException.class, () -> {
            contactRelationshipService.createRelationship(request);
        });

        verify(contactRepository).findById(12L);
        verify(contactRepository).findById(13L);
        verify(relationshipRepository, never()).save(any(ContactRelationship.class));

    }


    @Test
    void shouldGetRelationshipForContact() {
        Contact contact1 = new TestContact(12L, "Tester One", "test1@gmail.com", "0502489632");
        Contact contact2 = new TestContact(13L, "Tester Two", "test2@gmail.com", "05078489632");
        Contact contact3 = new TestContact(14L, "Tester Three", "test3@gmail.com", "0625458468");

        ContactRelationship relationship1 = ContactRelationship.create(contact1, contact2, RelationshipType.MANAGES);
        ContactRelationship relationship2 = ContactRelationship.create(contact2, contact3, RelationshipType.MENTORS);

        ReflectionTestUtils.setField(relationship1, "id", 3L);
        ReflectionTestUtils.setField(relationship2, "id", 4L);

        when(contactRepository.existsById(13L)).thenReturn(true);
        when(relationshipRepository.findBySourceContact_IdOrTargetContact_Id(13L, 13L)).thenReturn(List.of(relationship1, relationship2));

        List<RelationshipResponse> responses = contactRelationshipService.getRelationshipsForContact(13L);

        assertFalse(responses.isEmpty());

        assertEquals(2, responses.size());

        RelationshipResponse relationshipResponse1 = responses.get(0);
        RelationshipResponse relationshipResponse2 = responses.get(1);

        assertEquals(contact1.getId(), relationshipResponse1.sourceContactId());
        assertEquals(contact1.getFullName(), relationshipResponse1.sourceContactName());
        assertEquals(contact2.getId(), relationshipResponse1.targetContactId());
        assertEquals(contact2.getFullName(), relationshipResponse1.targetContactName());

        assertEquals(contact2.getId(), relationshipResponse2.sourceContactId());
        assertEquals(contact2.getFullName(), relationshipResponse2.sourceContactName());
        assertEquals(contact3.getId(), relationshipResponse2.targetContactId());
        assertEquals(contact3.getFullName(), relationshipResponse2.targetContactName());

        verify(contactRepository).existsById(13L);
        verify(relationshipRepository).findBySourceContact_IdOrTargetContact_Id(13L, 13L);

    }

    @Test
    void shouldGetRelationshipForContact_ReturnEmptyList_WhenNoRelationshipsExist() {

        when(contactRepository.existsById(13L)).thenReturn(true);
        when(relationshipRepository.findBySourceContact_IdOrTargetContact_Id(13L, 13L)).thenReturn(List.of());

        List<RelationshipResponse> responses = contactRelationshipService.getRelationshipsForContact(13L);

        assertTrue(responses.isEmpty());

        verify(contactRepository).existsById(13L);
        verify(relationshipRepository).findBySourceContact_IdOrTargetContact_Id(13L, 13L);

    }

    @Test
    void shouldFailGetRelationshipsForContact_ContactNotFound(){
        when(contactRepository.existsById(13L)).thenReturn(false);

        assertThrows(ContactNotFoundException.class, () -> {
            contactRelationshipService.getRelationshipsForContact(13L);
        });

        verify(contactRepository).existsById(13L);
        verifyNoInteractions(relationshipRepository);

    }
}
