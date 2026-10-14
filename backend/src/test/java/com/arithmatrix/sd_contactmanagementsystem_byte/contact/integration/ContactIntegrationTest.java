package com.arithmatrix.sd_contactmanagementsystem_byte.contact.integration;

import com.arithmatrix.sd_contactmanagementsystem_byte.contact.dto.CreateContactRequest;
import com.arithmatrix.sd_contactmanagementsystem_byte.contact.entity.Contact;
import com.arithmatrix.sd_contactmanagementsystem_byte.contact.repository.ContactRepository;
import com.arithmatrix.sd_contactmanagementsystem_byte.contact.service.ContactService;
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
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureMockMvc
@Transactional
public class ContactIntegrationTest {
    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private ContactService contactService;
    @Autowired
    private ContactRepository contactRepository;

    private Contact contact;


    @BeforeEach
    void setup(){
        contactRepository.deleteAll();
        Contact con = Contact.create("Tester One", "test@gmail.com","0843215547");
        contact = contactRepository.save(con);
    }


    @Test
    void shouldCreateContact() throws Exception {
        CreateContactRequest request = new CreateContactRequest("Tester Two","test1@gmail.com","04774561235");

        mockMvc.perform(post("/api/v1/contacts")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated());


        Contact contact1 = contactRepository.findByFullName("Tester Two");

        assertNotNull(contact1);

        assertEquals("Tester Two", contact1.getFullName());
        assertEquals("test1@gmail.com", contact1.getEmail());
        assertEquals("04774561235", contact1.getPhoneNumber());
    }

    @Test
    void shouldFailCreateContact_DuplicateEmail() throws Exception {
        CreateContactRequest request = new CreateContactRequest("Tester Two","test@gmail.com","04774561235");

        mockMvc.perform(post("/api/v1/contacts")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict());
    }

    @Test
    void shouldFailCreateContact_DuplicatePhoneNumber() throws Exception {
        CreateContactRequest request = new CreateContactRequest("Tester Two","test1@gmail.com","0843215547");

        mockMvc.perform(post("/api/v1/contacts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict());
    }

    @Test
    void shouldFailCreateContact_InvalidEmail() throws Exception {
        CreateContactRequest request = new CreateContactRequest("Tester Two","testgmail.com","04774561235");

        mockMvc.perform(post("/api/v1/contacts")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldGetAllContacts() throws Exception {

        mockMvc.perform(get("/api/v1/contacts/fetchAll"))
                .andExpect(status().isOk());


        List<Contact> list = contactRepository.findAll();

        assertNotNull(list);

        assertEquals(1, list.size());

        Contact contact = list.get(0);

        assertEquals("Tester One", contact.getFullName());
        assertEquals("test@gmail.com", contact.getEmail());
        assertEquals("0843215547", contact.getPhoneNumber());
    }

    @Test
    void shouldGetAllContacts_SucceedWithEmptyList() throws Exception {
        contactRepository.deleteAll();

        mockMvc.perform(get("/api/v1/contacts/fetchAll"))
                .andExpect(status().isOk());


        List<Contact> list = contactRepository.findAll();

        assertTrue(list.isEmpty());
    }


    @Test
    void shouldGetContactById() throws Exception {
        mockMvc.perform(get("/api/v1/contacts/fetch/{id}",contact.getId()))
                .andExpect(status().isOk());

        Contact contact1 = contactRepository.findById(contact.getId()).get();

        assertNotNull(contact1);

        assertEquals("Tester One", contact1.getFullName());
        assertEquals("test@gmail.com", contact1.getEmail());
        assertEquals("0843215547", contact1.getPhoneNumber());
    }

    @Test
    void shouldFailGetContactById_NotFound() throws Exception {
        mockMvc.perform(get("/api/v1/contacts/fetch/112"))
                .andExpect(status().isNotFound());
    }

}
