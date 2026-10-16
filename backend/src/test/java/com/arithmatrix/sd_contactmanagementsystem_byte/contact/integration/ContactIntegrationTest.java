package com.arithmatrix.sd_contactmanagementsystem_byte.contact.integration;

import com.arithmatrix.sd_contactmanagementsystem_byte.contact.dto.ContactResponse;
import com.arithmatrix.sd_contactmanagementsystem_byte.contact.dto.CreateContactRequest;
import com.arithmatrix.sd_contactmanagementsystem_byte.contact.dto.UpdateContactRequest;
import com.arithmatrix.sd_contactmanagementsystem_byte.contact.entity.Contact;
import com.arithmatrix.sd_contactmanagementsystem_byte.contact.entity.TestContact;
import com.arithmatrix.sd_contactmanagementsystem_byte.contact.repository.ContactRepository;
import com.arithmatrix.sd_contactmanagementsystem_byte.contact.service.ContactService;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.data.domain.Sort;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.ObjectMapper;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
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
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(contact.getId()))
                .andExpect(jsonPath("$[0].fullName").value(contact.getFullName()))
                .andExpect(jsonPath("$[0].email").value(contact.getEmail()))
                .andExpect(jsonPath("$[0].phoneNumber").value(contact.getPhoneNumber()));
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
    void shouldSearchContactFullName() throws Exception {

        mockMvc.perform(get("/api/v1/contacts/fetchAll")
                .param("search","Test"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(contact.getId()))
                .andExpect(jsonPath("$[0].fullName").value(contact.getFullName()))
                .andExpect(jsonPath("$[0].email").value(contact.getEmail()))
                .andExpect(jsonPath("$[0].phoneNumber").value(contact.getPhoneNumber()));

    }

    @Test
    void shouldSearchContactFullName_IgnoreCase() throws Exception {

        mockMvc.perform(get("/api/v1/contacts/fetchAll")
                        .param("search","TEST"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(contact.getId()))
                .andExpect(jsonPath("$[0].fullName").value(contact.getFullName()))
                .andExpect(jsonPath("$[0].email").value(contact.getEmail()))
                .andExpect(jsonPath("$[0].phoneNumber").value(contact.getPhoneNumber()));

    }

    @Test
    void shouldSearchContactEmail() throws Exception {

        mockMvc.perform(get("/api/v1/contacts/fetchAll")
                        .param("search","test@gmail.com"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].id").value(contact.getId()))
                .andExpect(jsonPath("$[0].fullName").value(contact.getFullName()))
                .andExpect(jsonPath("$[0].email").value(contact.getEmail()))
                .andExpect(jsonPath("$[0].phoneNumber").value(contact.getPhoneNumber()));

    }

    @Test
    void shouldSearchContactEmail_NoSearch() throws Exception {

        Contact contact1 = Contact.create("Nate","nate@gmail.com","0248521515");
        contactRepository.save(contact1);


        mockMvc.perform(get("/api/v1/contacts/fetchAll")
                        .param("search","   "))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2));

    }


    @Test
    void shouldSearchContact_PhoneNumber() throws Exception {

        mockMvc.perform(get("/api/v1/contacts/fetchAll")
                        .param("search","0843215547"))
                .andExpect(status().isOk());

        List<Contact> list = contactRepository.findByFullNameContainingIgnoreCaseOrEmailContainingIgnoreCaseOrPhoneNumberContainingIgnoreCase("0843215547","0843215547","0843215547", Sort.by(Sort.Direction.ASC,"fullName"));

        assertNotNull(list);

        assertEquals(1, list.size());

        assertEquals("Tester One", list.get(0).getFullName());
        assertEquals("test@gmail.com", list.get(0).getEmail());
        assertEquals("0843215547", list.get(0).getPhoneNumber());
    }




    @Test
    void shouldGetContactById() throws Exception {
        mockMvc.perform(get("/api/v1/contacts/fetch/{id}",contact.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(contact.getId()))
                .andExpect(jsonPath("$.fullName").value(contact.getFullName()))
                .andExpect(jsonPath("$.email").value(contact.getEmail()))
                .andExpect(jsonPath("$.phoneNumber").value(contact.getPhoneNumber()));
    }

    @Test
    void shouldFailGetContactById_NotFound() throws Exception {
        mockMvc.perform(get("/api/v1/contacts/fetch/112"))
                .andExpect(status().isNotFound());
    }


    @Test
    void shouldUpdateContact() throws Exception {
        UpdateContactRequest request = new UpdateContactRequest("Tester Two","tester@gmail.com","0874561235");

        mockMvc.perform(put("/api/v1/contacts/update/{id}",contact.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());


        Contact contact1 = contactRepository.findById(contact.getId()).get();

        assertNotNull(contact1);

        assertEquals("Tester Two", contact1.getFullName());
        assertEquals("tester@gmail.com", contact1.getEmail());
        assertEquals("0874561235", contact1.getPhoneNumber());
    }

    @Test
    void shouldFailUpdateContact_DuplicateEmail() throws Exception {
        Contact contact1 = Contact.create("Tester One","tester@gmail.com","01548511555");
        contactRepository.save(contact1);
        UpdateContactRequest request = new UpdateContactRequest("Tester Two","tester@gmail.com","0874561235");

        mockMvc.perform(put("/api/v1/contacts/update/{id}",contact.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict());
    }

    @Test
    void shouldFailUpdateContact_DuplicatePhoneNumber() throws Exception {
        Contact contact1 = Contact.create("Tester One","tester@gmail.com","01548511555");
        contactRepository.save(contact1);
        UpdateContactRequest request = new UpdateContactRequest("Tester Two","tester@gmail.com","01548511555");

        mockMvc.perform(put("/api/v1/contacts/update/{id}",contact.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict());
    }

    @Test
    void shouldFailUpdateContact_NotFound() throws Exception {
        UpdateContactRequest request = new UpdateContactRequest("Tester Two","tester@gmail.com","0874561235");

        mockMvc.perform(put("/api/v1/contacts/update/666")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound());
    }


    @Test
    void shouldDeleteContact_AndTryToRetrieve() throws Exception {
        mockMvc.perform(delete("/api/v1/contacts/delete/{id}",contact.getId()))
                .andExpect(status().isNoContent());

        List<Contact> list = contactRepository.findAll();

        assertTrue(list.isEmpty());
    }

    @Test
    void shouldFailDelete_NotFound() throws Exception {
        mockMvc.perform(delete("/api/v1/contacts/delete/1233"))
                .andExpect(status().isNotFound());
    }


}
