package com.arithmatrix.sd_contactmanagementsystem_byte.relationship.controller;

import com.arithmatrix.sd_contactmanagementsystem_byte.contact.exception.ContactNotFoundException;
import com.arithmatrix.sd_contactmanagementsystem_byte.relationship.dto.*;
import com.arithmatrix.sd_contactmanagementsystem_byte.relationship.enums.RelationshipType;
import com.arithmatrix.sd_contactmanagementsystem_byte.relationship.exception.DuplicateRelationshipException;
import com.arithmatrix.sd_contactmanagementsystem_byte.relationship.exception.InvalidRelationshipException;
import com.arithmatrix.sd_contactmanagementsystem_byte.relationship.service.ContactRelationshipService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.ObjectMapper;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@WebMvcTest(ContactRelationshipController.class)
@AutoConfigureMockMvc(addFilters = false)
public class ContactRelationshipControllerTest {
    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;
    @MockitoBean
    private ContactRelationshipService relationshipService;

    @Test
    void shouldCreateContactRelationship() throws Exception {
        CreateRelationshipRequest request = new CreateRelationshipRequest(12L,13L, RelationshipType.MANAGES);

        RelationshipResponse response = new RelationshipResponse(1L,12L,"Tester One",13L,"Tester Two",RelationshipType.MANAGES);

        when(relationshipService.createRelationship(any(CreateRelationshipRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/v1/relationships")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.sourceContactId").value(12L))
                .andExpect(jsonPath("$.sourceContactName").value("Tester One"))
                .andExpect(jsonPath("$.targetContactId").value(13L))
                .andExpect(jsonPath("$.targetContactName").value("Tester Two"))
                .andExpect(jsonPath("$.relationshipType").value(RelationshipType.MANAGES.name()));

        verify(relationshipService).createRelationship(argThat(actual->
                actual.sourceContactId().equals(12L)
                && actual.targetContactId().equals(13L)
                && actual.relationshipType().equals(RelationshipType.MANAGES)));
    }

    @Test
    void shouldFailCreateContactRelationship_DuplicateRelationship() throws Exception {
        CreateRelationshipRequest request = new CreateRelationshipRequest(12L,13L, RelationshipType.MANAGES);

        when(relationshipService.createRelationship(any(CreateRelationshipRequest.class))).thenThrow(DuplicateRelationshipException.class);

        mockMvc.perform(post("/api/v1/relationships")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict());


        verify(relationshipService).createRelationship(any(CreateRelationshipRequest.class));
    }


    @Test
    void shouldFailCreateContactRelationship_ContactLinkToItself() throws Exception {
        CreateRelationshipRequest request = new CreateRelationshipRequest(12L,13L, RelationshipType.MANAGES);

        when(relationshipService.createRelationship(any(CreateRelationshipRequest.class))).thenThrow(InvalidRelationshipException.class);

        mockMvc.perform(post("/api/v1/relationships")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());


        verify(relationshipService).createRelationship(any(CreateRelationshipRequest.class));
    }

    @Test
    void shouldFailCreateContactRelationship_SourceContactNotFound() throws Exception {
        CreateRelationshipRequest request = new CreateRelationshipRequest(12L,13L, RelationshipType.MANAGES);

        when(relationshipService.createRelationship(any(CreateRelationshipRequest.class))).thenThrow(ContactNotFoundException.class);

        mockMvc.perform(post("/api/v1/relationships")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound());



        verify(relationshipService).createRelationship(any(CreateRelationshipRequest.class));
    }

    @Test
    void shouldFailCreateContactRelationship_TargetContactNotFound() throws Exception {
        CreateRelationshipRequest request = new CreateRelationshipRequest(12L,13L, RelationshipType.MANAGES);

        when(relationshipService.createRelationship(any(CreateRelationshipRequest.class))).thenThrow(ContactNotFoundException.class);

        mockMvc.perform(post("/api/v1/relationships")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound());



        verify(relationshipService).createRelationship(any(CreateRelationshipRequest.class));
    }

    @Test
    void shouldFailCreateContactRelationship_SourceContactMissing() throws Exception {
        CreateRelationshipRequest request = new CreateRelationshipRequest(null,13L, RelationshipType.MANAGES);

        mockMvc.perform(post("/api/v1/relationships")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());


        verifyNoInteractions(relationshipService);
    }

    @Test
    void shouldFailCreateContactRelationship_RelationshipTypeInvalid() throws Exception {

        String invalidJson = """
            {
                "sourceContactId": 1,
                "targetContactId": 2,
                "relationshipType": "BEST_FRIEND"
            }
            """;

        mockMvc.perform(post("/api/v1/relationships")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidJson))
                .andExpect(status().isBadRequest());


        verifyNoInteractions(relationshipService);

    }


    @Test
    void shouldGetRelationshipForContact() throws Exception {
        Long contactId = 2L;

        RelationshipResponse first = new RelationshipResponse(10L,1L,"Tester One",2L,"Tester Two",RelationshipType.MANAGES);

        RelationshipResponse second = new RelationshipResponse(11L,2L,"Tester Two",3L,"Tester Three",RelationshipType.MENTORS);

        when(relationshipService.getRelationshipsForContact(contactId)).thenReturn(List.of(first, second));

        mockMvc.perform(get("/api/v1/relationships/contact/" + contactId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$.[0].id").value(10L))
                .andExpect(jsonPath("$.[0].sourceContactId").value(1L))
                .andExpect(jsonPath("$.[0].targetContactId").value(2L))
                .andExpect(jsonPath("$.[0].relationshipType").value(RelationshipType.MANAGES.name()))

                .andExpect(jsonPath("$.[1].id").value(11L))
                .andExpect(jsonPath("$.[1].sourceContactId").value(2L))
                .andExpect(jsonPath("$.[1].targetContactId").value(3L))
                .andExpect(jsonPath("$.[1].relationshipType").value(RelationshipType.MENTORS.name()));

        verify(relationshipService).getRelationshipsForContact(contactId);

    }

    @Test
    void shouldGetRelationshipForContact_whenNoRelationshipsExist_EmptyList() throws Exception {
        Long contactId = 2L;

        when(relationshipService.getRelationshipsForContact(contactId)).thenReturn(List.of());

        mockMvc.perform(get("/api/v1/relationships/contact/" + contactId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));

        verify(relationshipService).getRelationshipsForContact(contactId);

    }


    @Test
    void getRelationShipGraph_shouldReturn200_withNodesAndEdges() throws Exception {

        RelationshipGraphResponse graphResponse = new RelationshipGraphResponse(
                List.of(
                        new GraphNodeResponse(1L,"John Smith"),
                        new GraphNodeResponse(2L,"Sarah Morgan")),
                List.of(
                        new GraphEdgeResponse(12L,1L,2L,RelationshipType.MANAGES)
                )
        );

        when(relationshipService.getRelationshipGraph()).thenReturn(graphResponse);

        mockMvc.perform(get("/api/v1/relationships/graph"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nodes.length()").value(2))
                .andExpect(jsonPath("$.edges.length()").value(1))

                .andExpect(jsonPath("$.nodes[0].id").value(1L))
                .andExpect(jsonPath("$.nodes[0].fullName").value("John Smith"))
                .andExpect(jsonPath("$.nodes[1].id").value(2L))
                .andExpect(jsonPath("$.nodes[1].fullName").value("Sarah Morgan"))

                .andExpect(jsonPath("$.edges[0].id").value(12L))
                .andExpect(jsonPath("$.edges[0].sourceContactId").value(1L))
                .andExpect(jsonPath("$.edges[0].targetContactId").value(2L))
                .andExpect(jsonPath("$.edges[0].relationType").value(RelationshipType.MANAGES.name()));

        verify(relationshipService).getRelationshipGraph();
    }

    @Test
    void getRelationshipGraph_shouldReturn200_whenGraphIsEmpty() throws Exception {
        RelationshipGraphResponse response = new RelationshipGraphResponse(List.of(),List.of());

        when(relationshipService.getRelationshipGraph()).thenReturn(response);

        mockMvc.perform(get("/api/v1/relationships/graph"))
                .andExpect(status().isOk());


        verify(relationshipService).getRelationshipGraph();
    }

    @Test
    void getRelationshipGraph_shouldIncludeIsolatedContacts() throws Exception {
        RelationshipGraphResponse graphResponse = new RelationshipGraphResponse(
                List.of(
                        new GraphNodeResponse(1L,"John Smith")),
                List.of()
        );

        when(relationshipService.getRelationshipGraph()).thenReturn(graphResponse);

        mockMvc.perform(get("/api/v1/relationships/graph"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nodes.length()").value(1))
                .andExpect(jsonPath("$.edges.length()").value(0))

                .andExpect(jsonPath("$.nodes[0].id").value(1L))
                .andExpect(jsonPath("$.nodes[0].fullName").value("John Smith"));

        verify(relationshipService).getRelationshipGraph();

    }

}
