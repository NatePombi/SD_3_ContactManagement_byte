package com.arithmatrix.sd_contactmanagementsystem_byte.relationship.service;

import com.arithmatrix.sd_contactmanagementsystem_byte.contact.entity.Contact;
import com.arithmatrix.sd_contactmanagementsystem_byte.contact.entity.TestContact;
import com.arithmatrix.sd_contactmanagementsystem_byte.contact.exception.ContactNotFoundException;
import com.arithmatrix.sd_contactmanagementsystem_byte.contact.repository.ContactRepository;
import com.arithmatrix.sd_contactmanagementsystem_byte.relationship.dto.*;
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
import org.springframework.data.domain.Sort;
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


    @Test
    void GetRelationshipGraph_ShouldReturnAllNodesAndEdges(){
        Contact john = new TestContact(1L ,"John Smith","john@gmail.com","0284521888");
        Contact sarah = new TestContact(2L ,"Sarah Morgan","sarah@gmail.com","076418018");
        Contact max = new TestContact(3L ,"Max Payne","max@gmail.com","0698751158");
        Contact ned = new TestContact(4L ,"Ned Wed","ned@gmail.com","0915188151");

        ContactRelationship manages = ContactRelationship.create(john, max, RelationshipType.MANAGES);
        ContactRelationship mentors = ContactRelationship.create(max, ned, RelationshipType.MENTORS);

        ReflectionTestUtils.setField(manages,"id",6L);
        ReflectionTestUtils.setField(mentors,"id",7L);

        Sort contactSort = Sort.by(Sort.Direction.ASC,"fullName").and(Sort.by("id"));
        Sort relationshipSort = Sort.by(Sort.Direction.ASC,"id");

        when(contactRepository.findAll(contactSort)).thenReturn(List.of(john,sarah,max,ned));
        when(relationshipRepository.findAll(relationshipSort)).thenReturn(List.of(manages,mentors));

        RelationshipGraphResponse graphResponse = contactRelationshipService.getRelationshipGraph();

        assertNotNull(graphResponse);

        assertEquals(4,graphResponse.nodes().size());
        assertEquals(2,graphResponse.edges().size());

        assertEquals(
                List.of(new GraphNodeResponse(1L,"John Smith"),
                        new GraphNodeResponse(2L,"Sarah Morgan"),
                        new GraphNodeResponse(3L,"Max Payne"),
                        new GraphNodeResponse(4L,"Ned Wed")),graphResponse.nodes()
        );

        assertEquals(
                List.of(new GraphEdgeResponse(6L,1L,3L,RelationshipType.MANAGES),
                        new GraphEdgeResponse(7L,3L,4L,RelationshipType.MENTORS)),graphResponse.edges()
        );

        verify(contactRepository).findAll(contactSort);
        verify(relationshipRepository).findAll(relationshipSort);
    }


    @Test
    void getRelationshipGraph_shouldReturnNodes_WhenNoRelationshipsExists(){
        Contact john = new TestContact(1L ,"John Smith","john@gmail.com","0284521888");
        Contact sarah = new TestContact(2L ,"Sarah Morgan","sarah@gmail.com","076418018");

        Sort contactSort = Sort.by(Sort.Direction.ASC,"fullName").and(Sort.by("id"));
        Sort relationshipSort = Sort.by(Sort.Direction.ASC,"id");

        when(contactRepository.findAll(contactSort)).thenReturn(List.of(john,sarah));
        when(relationshipRepository.findAll(relationshipSort)).thenReturn(List.of());

        RelationshipGraphResponse graphResponse = contactRelationshipService.getRelationshipGraph();

        assertNotNull(graphResponse);

        assertEquals(2,graphResponse.nodes().size());
        assertTrue(graphResponse.edges().isEmpty());
        assertEquals(1L,graphResponse.nodes().get(0).id());
        assertEquals("John Smith",graphResponse.nodes().get(0).fullName());
        assertEquals(2L,graphResponse.nodes().get(1).id());
        assertEquals("Sarah Morgan",graphResponse.nodes().get(1).fullName());

        verify(contactRepository).findAll(contactSort);
        verify(relationshipRepository).findAll(relationshipSort);
    }

    @Test
    void getRelationshipGraph_shouldReturnEmptyGraph_whenDatabaseIsEmpty(){
        Sort contactSort = Sort.by(Sort.Direction.ASC,"fullName").and(Sort.by("id"));
        Sort relationshipSort = Sort.by(Sort.Direction.ASC,"id");

        when(contactRepository.findAll(contactSort)).thenReturn(List.of());
        when(relationshipRepository.findAll(relationshipSort)).thenReturn(List.of());

        RelationshipGraphResponse graphResponse = contactRelationshipService.getRelationshipGraph();

        assertNotNull(graphResponse);
        assertNotNull(graphResponse.nodes());
        assertNotNull(graphResponse.edges());

        assertTrue(graphResponse.nodes().isEmpty());
        assertTrue(graphResponse.edges().isEmpty());

        verify(contactRepository).findAll(contactSort);
        verify(relationshipRepository).findAll(relationshipSort);

    }
}
