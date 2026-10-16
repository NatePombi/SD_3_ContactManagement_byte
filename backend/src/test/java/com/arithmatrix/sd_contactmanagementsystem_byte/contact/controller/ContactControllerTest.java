package com.arithmatrix.sd_contactmanagementsystem_byte.contact.controller;

import com.arithmatrix.sd_contactmanagementsystem_byte.contact.dto.ContactResponse;
import com.arithmatrix.sd_contactmanagementsystem_byte.contact.dto.CreateContactRequest;
import com.arithmatrix.sd_contactmanagementsystem_byte.contact.dto.UpdateContactRequest;
import com.arithmatrix.sd_contactmanagementsystem_byte.contact.entity.Contact;
import com.arithmatrix.sd_contactmanagementsystem_byte.contact.entity.TestContact;
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

import java.util.List;
import java.util.Optional;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

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
    void shouldSearchContact() throws Exception {

      String keyword = "Test";

      ContactResponse response = new ContactResponse(1L,"Tester One","test@gmail.com","0545125");

      when(contactService.searchContacts(keyword)).thenReturn(List.of(response));


      mockMvc.perform(get("/api/v1/contacts/fetchAll")
              .param("search",keyword))
              .andExpect(status().isOk())
              .andExpect(jsonPath("$.length()").value(1))
              .andExpect(jsonPath("$[0].id").value(1L))
              .andExpect(jsonPath("$[0].fullName").value("Tester One"))
              .andExpect(jsonPath("$[0].email").value("test@gmail.com"))
              .andExpect(jsonPath("$[0].phoneNumber").value("0545125"));
    }


    @Test
    void shouldSearchContact_whenNoMatchesExist() throws Exception {

        String keyword = "unknownperson";


        when(contactService.searchContacts(keyword)).thenReturn(List.of());


        mockMvc.perform(get("/api/v1/contacts/fetchAll")
                        .param("search",keyword))
                .andExpect(status().isOk())
                .andExpect(content().json("[]"));

        verify(contactService).searchContacts(keyword);
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

    @Test
    void shouldUpdateContact() throws Exception {
        UpdateContactRequest request = new UpdateContactRequest("Tester One","tester@gmail.com","0874561235");
        ContactResponse response = new ContactResponse(12L,"Tester One","tester@gmail.com", "0874561235");

        when(contactService.updateContact(request,12L)).thenReturn(response);
        mockMvc.perform(put("/api/v1/contacts/update/{id}", 12L)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(12L))
                .andExpect(jsonPath("$.fullName").value("Tester One"))
                .andExpect(jsonPath("$.email").value("tester@gmail.com"))
                .andExpect(jsonPath("phoneNumber").value("0874561235"));

    }

    @Test
    void shouldFailUpdateContact_InvalidId() throws Exception {

        mockMvc.perform(put("/api/v1/contacts/update/{id}", "Invalid"))
                .andExpect(status().isBadRequest());
    }


    @Test
    void shouldFailUpdateContact_FullNameEmpty() throws Exception {
        UpdateContactRequest request = new UpdateContactRequest("  ","tester@gmail.com","0874561235");


        mockMvc.perform(put("/api/v1/contacts/update/{id}", 12L)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());

    }

    @Test
    void shouldFailUpdateContact_EmailEmpty() throws Exception {
        UpdateContactRequest request = new UpdateContactRequest("Tester One","    ","0874561235");


        mockMvc.perform(put("/api/v1/contacts/update/{id}", 12L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());

    }

    @Test
    void shouldFailUpdateContact_PhoneNumberEmpty() throws Exception {
        UpdateContactRequest request = new UpdateContactRequest("Tester One","tester@gmail.com","   ");


        mockMvc.perform(put("/api/v1/contacts/update/{id}", 12L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());

    }


    @Test
    void shouldDeleteContact() throws Exception {

        doNothing().when(contactService).deleteContact(12L);

        mockMvc.perform(delete("/api/v1/contacts/delete/{id}",12L))
                .andExpect(status().isNoContent())
                .andExpect(content().string(""));


        verify(contactService).deleteContact(12L);
    }

    @Test
    void shouldFailDeleteContact_NotFound() throws Exception {

        doThrow(new ContactNotFoundException("Contact not found with ID: 9999")).when(contactService).deleteContact(9999L);

        mockMvc.perform(delete("/api/v1/contacts/delete/{id}",9999L))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Contact not found with ID: 9999"));

        verify(contactService).deleteContact(9999L);

    }






}
