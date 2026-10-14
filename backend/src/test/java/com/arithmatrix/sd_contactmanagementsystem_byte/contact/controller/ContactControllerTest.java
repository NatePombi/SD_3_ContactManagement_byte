package com.arithmatrix.sd_contactmanagementsystem_byte.contact.controller;

import com.arithmatrix.sd_contactmanagementsystem_byte.contact.dto.ContactResponse;
import com.arithmatrix.sd_contactmanagementsystem_byte.contact.dto.CreateContactRequest;
import com.arithmatrix.sd_contactmanagementsystem_byte.contact.exception.ContactNotFoundException;
import com.arithmatrix.sd_contactmanagementsystem_byte.contact.service.ContactService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.ObjectMapper;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ContactController.class)
@AutoConfigureMockMvc(addFilters = false)
public class ContactControllerTest {
    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;
    @MockitoBean
    private ContactService contactService;

    @Test
    void shouldCreateContact() throws Exception {
        CreateContactRequest request = new CreateContactRequest("Tester One","test@gmail.com","0874561235");
        ContactResponse contactResponse = new ContactResponse(12L,"Tester One", "test@gmail.com", "0874561235");

        when(contactService.createContact(request)).thenReturn(contactResponse);

        mockMvc.perform(post("/api/v1/contacts")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.fullName").value("Tester One"))
                .andExpect(jsonPath("$.email").value("test@gmail.com"))
                .andExpect(jsonPath("$.phoneNumber").value("0874561235"));



        verify(contactService).createContact(request);
    }

    @Test
    void shouldFailToCreate_InvalidEmail() throws Exception {
        CreateContactRequest request = new CreateContactRequest("Tester One","testgmail.com","0874561235");

        mockMvc.perform(post("/api/v1/contacts")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());


        verify(contactService, never()).createContact(request);
    }

    @Test
    void shouldFailToCreate_MissingFullName() throws Exception {
        CreateContactRequest request = new CreateContactRequest("  ","test@gmail.com","0874561235");

        mockMvc.perform(post("/api/v1/contacts")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());


        verify(contactService, never()).createContact(request);

    }

    @Test
    void shouldFailToCreate_MissingPhoneNumber() throws Exception {
        CreateContactRequest request = new CreateContactRequest("Tester One","test@gmail.com","     ");

        mockMvc.perform(post("/api/v1/contacts")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());


        verify(contactService, never()).createContact(request);
    }


    @Test
    void shouldGetAllContacts() throws Exception {

        mockMvc.perform(get("/api/v1/contacts/fetchAll"))
                .andExpect(status().isOk());

        verify(contactService).getAllContacts();
    }


    @Test
    void shouldGetContactById() throws Exception {

        ContactResponse contactResponse = new ContactResponse(12L,"Tester One", "test@gmail.com", "0874561235");


        when(contactService.getContactById(12L)).thenReturn(contactResponse);

        mockMvc.perform(get("/api/v1/contacts/fetch/{id}", 12L))
                .andExpect(status().isOk());

        verify(contactService).getContactById(12L);
    }

    @Test
    void shouldFailGetContactById_NotFound() throws Exception {

        when(contactService.getContactById(12L)).thenThrow(ContactNotFoundException.class);

        mockMvc.perform(get("/api/v1/contacts/fetch/{id}", 12L))
                .andExpect(status().isNotFound());
    }
}
