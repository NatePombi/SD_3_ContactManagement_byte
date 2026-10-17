package com.arithmatrix.sd_contactmanagementsystem_byte.relationship.integration;

import com.arithmatrix.sd_contactmanagementsystem_byte.contact.entity.Contact;
import com.arithmatrix.sd_contactmanagementsystem_byte.contact.repository.ContactRepository;
import com.arithmatrix.sd_contactmanagementsystem_byte.relationship.dto.CreateRelationshipRequest;
import com.arithmatrix.sd_contactmanagementsystem_byte.relationship.dto.RelationshipResponse;
import com.arithmatrix.sd_contactmanagementsystem_byte.relationship.entity.ContactRelationship;
import com.arithmatrix.sd_contactmanagementsystem_byte.relationship.enums.RelationshipType;
import com.arithmatrix.sd_contactmanagementsystem_byte.relationship.repository.RelationshipRepository;
import com.arithmatrix.sd_contactmanagementsystem_byte.relationship.service.ContactRelationshipService;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.ObjectMapper;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
public class ContactRelationshipControllerIntegrationTest {
    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private ContactRelationshipService relationshipService;
    @Autowired
    private ContactRepository contactRepository;
    @Autowired
    private RelationshipRepository relationshipRepository;

    private Contact contact1;
    private Contact contact2;


    @BeforeEach
    public void setup(){
        contactRepository.deleteAll();
        Contact first = Contact.create("Tester One", "test1@gmail.com","021654897");
        Contact second = Contact.create("Tester Two", "test2@gmail.com","087122269");
        contact1 = contactRepository.save(first);
        contact2 = contactRepository.save(second);

    }

    @Test
    void shouldCreateContactRelationship() throws Exception {
        Long sourceId = contact1.getId();
        Long targetId = contact2.getId();
        CreateRelationshipRequest request = new CreateRelationshipRequest(sourceId,targetId, RelationshipType.MANAGES);


        mockMvc.perform(post("/api/v1/relationships")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated());


        List<ContactRelationship> responses = relationshipRepository.findAll();

        assertNotNull(responses);
        assertFalse(responses.isEmpty());

        ContactRelationship relationship = responses.get(0);

        assertEquals(sourceId,relationship.getSourceContact().getId());
        assertEquals(targetId,relationship.getTargetContact().getId());

    }

    @Test
    void shouldFailCreateContactRelationship_DuplicateRelationships() throws Exception {
        ContactRelationship relationship = ContactRelationship.create(contact1,contact2,RelationshipType.MANAGES);
        relationshipRepository.save(relationship);

        CreateRelationshipRequest request = new CreateRelationshipRequest(contact1.getId(),contact2.getId(),RelationshipType.MANAGES);

        mockMvc.perform(post("/api/v1/relationships")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict());
    }


    @Test
    void shouldFailCreateContactRelationship_ContactLinkToItself() throws Exception {
        CreateRelationshipRequest request = new CreateRelationshipRequest(contact1.getId(),contact1.getId(),RelationshipType.MANAGES);

        mockMvc.perform(post("/api/v1/relationships")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldFailCreateContactRelationship_SourceContactNotFound() throws Exception {
        CreateRelationshipRequest request = new CreateRelationshipRequest(999L,contact1.getId(),RelationshipType.MANAGES);

        mockMvc.perform(post("/api/v1/relationships")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldFailCreateContactRelationship_TargetContactNotFound() throws Exception {
        CreateRelationshipRequest request = new CreateRelationshipRequest(contact1.getId(),996L,RelationshipType.MANAGES);

        mockMvc.perform(post("/api/v1/relationships")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldFailCreateContactRelationship_SourceContactMissing() throws Exception {
        CreateRelationshipRequest request = new CreateRelationshipRequest(null,contact2.getId(),RelationshipType.MANAGES);

        mockMvc.perform(post("/api/v1/relationships")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }


    @Test
    void shouldGetRelationshipsForContact() throws Exception{
        Long sourceId = contact1.getId();
        Long targetId = contact2.getId();

        ContactRelationship relationship = ContactRelationship.create(contact1,contact2,RelationshipType.MANAGES);
        relationshipRepository.save(relationship);

        mockMvc.perform(get("/api/v1/relationships/contact/" + sourceId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$.[0].sourceContactId").value(sourceId))
                .andExpect(jsonPath("$.[0].targetContactId").value(targetId))
                .andExpect(jsonPath("$.[0].relationshipType").value(RelationshipType.MANAGES.name()));
    }

    @Test
    void shouldGetRelationshipsForContact_EmptyList_WhenNoRelationshipsExist() throws Exception{
        Long sourceId = contact1.getId();

        relationshipRepository.deleteAll();

        mockMvc.perform(get("/api/v1/relationships/contact/" + sourceId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    void shouldFailGetRelationshipsForContact_ContactNotFound() throws Exception{
        Long sourceId = 99L;


        mockMvc.perform(get("/api/v1/relationships/contact/" + sourceId))
                .andExpect(status().isNotFound());
    }


}
