package com.arithmatrix.sd_contactmanagementsystem_byte.contact.service;

import com.arithmatrix.sd_contactmanagementsystem_byte.contact.dto.ContactResponse;
import com.arithmatrix.sd_contactmanagementsystem_byte.contact.dto.CreateContactRequest;
import com.arithmatrix.sd_contactmanagementsystem_byte.contact.entity.Contact;
import com.arithmatrix.sd_contactmanagementsystem_byte.contact.entity.TestContact;
import com.arithmatrix.sd_contactmanagementsystem_byte.contact.exception.DuplicateContactException;
import com.arithmatrix.sd_contactmanagementsystem_byte.contact.repository.ContactRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ContactServiceTest {
    @Mock
    ContactRepository contactRepository;
    @InjectMocks
    ContactService contactService;

    @Test
    void shouldCreateContact() {
        Contact saved = new TestContact(12L,"Test One","test@gmail.com","0874561235");
        when(contactRepository.existsByEmailIgnoreCase("test@gmail.com")).thenReturn(false);
        when(contactRepository.existsByPhoneNumber("0874561235")).thenReturn(false);
        when(contactRepository.save(any(Contact.class))).thenReturn(saved);
        CreateContactRequest request = new CreateContactRequest("Tester One","test@gmail.com","0874561235");

        ContactResponse contact = contactService.createContact(request);

        assertNotNull(contact);

        assertEquals(12L,contact.id());
        assertEquals("Test One",contact.fullName());
        assertEquals("0874561235",contact.phoneNumber());
        assertEquals("test@gmail.com",contact.email());

        verify(contactRepository).save(any(Contact.class));
    }

    @Test
    void shouldFailCreateContact_DuplicateEmail(){
        when(contactRepository.existsByEmailIgnoreCase("test@gmail.com")).thenReturn(true);

        CreateContactRequest request = new CreateContactRequest("Tester One","test@gmail.com","0874561235");

        assertThrows(DuplicateContactException.class,()->contactService.createContact(request));

        verify(contactRepository, never()).save(any(Contact.class));

    }

    @Test
    void shouldFailCreateContact_DuplicatePhoneNumber(){
        when(contactRepository.existsByEmailIgnoreCase("test@gmail.com")).thenReturn(false);
        when(contactRepository.existsByPhoneNumber("0874561235")).thenReturn(true);

        CreateContactRequest request = new CreateContactRequest("Tester One","test@gmail.com","0874561235");

        assertThrows(DuplicateContactException.class,()->contactService.createContact(request));

        verify(contactRepository, never()).save(any(Contact.class));

    }

    @Test
    void shouldCreateContact_shouldNormalizeDetails_beforeSaving(){
        CreateContactRequest request = new CreateContactRequest("Tester One   ","test@gmail.com  ","0874561235  ");

        when(contactRepository.save(any(Contact.class))).thenAnswer(invocation ->  invocation.getArgument(0));

        contactService.createContact(request);

        ArgumentCaptor<Contact> captor = ArgumentCaptor.forClass(Contact.class);

        verify(contactRepository).save(captor.capture());

        Contact capturedContact = captor.getValue();

        assertNotNull(capturedContact);

        assertEquals("Tester One",capturedContact.getFullName());
        assertEquals("0874561235",capturedContact.getPhoneNumber());
        assertEquals("test@gmail.com",capturedContact.getEmail());

        verify(contactRepository).existsByEmailIgnoreCase("test@gmail.com");
        verify(contactRepository).existsByPhoneNumber("0874561235");


    }
}
