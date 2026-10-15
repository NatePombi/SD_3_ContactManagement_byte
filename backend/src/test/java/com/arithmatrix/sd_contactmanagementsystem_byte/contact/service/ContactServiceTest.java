package com.arithmatrix.sd_contactmanagementsystem_byte.contact.service;

import com.arithmatrix.sd_contactmanagementsystem_byte.contact.dto.ContactResponse;
import com.arithmatrix.sd_contactmanagementsystem_byte.contact.dto.CreateContactRequest;
import com.arithmatrix.sd_contactmanagementsystem_byte.contact.dto.UpdateContactRequest;
import com.arithmatrix.sd_contactmanagementsystem_byte.contact.entity.Contact;
import com.arithmatrix.sd_contactmanagementsystem_byte.contact.entity.TestContact;
import com.arithmatrix.sd_contactmanagementsystem_byte.contact.exception.ContactNotFoundException;
import com.arithmatrix.sd_contactmanagementsystem_byte.contact.exception.DuplicateContactException;
import com.arithmatrix.sd_contactmanagementsystem_byte.contact.repository.ContactRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Sort;

import java.util.List;
import java.util.Optional;

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


    @Test
    void shouldGetContactById(){
        Contact contact1 = new TestContact(11L,"Tester One","test1@gmail.com","4845121155");

        when(contactRepository.findById(11L)).thenReturn(Optional.of(contact1));

        ContactResponse contact = contactService.getContactById(11L);

        assertNotNull(contact);

        assertEquals(11L,contact.id());
        assertEquals("Tester One",contact.fullName());
        assertEquals("test1@gmail.com",contact.email());
        assertEquals("4845121155",contact.phoneNumber());

        verify(contactRepository).findById(11L);
    }

    @Test
    void shouldFailGetContactById_NotFound(){
        when(contactRepository.findById(11L)).thenThrow(ContactNotFoundException.class);

        assertThrows(ContactNotFoundException.class,()->contactService.getContactById(11L));
    }



    @Test
    void shouldGetAllContacts(){
        Contact contact1 = new TestContact(11L,"Tester One","test1@gmail.com","4845121155");
        Contact contact2 = new TestContact(13L,"Tester Two","test2@gmail.com","051541215");
        Contact contact3 = new TestContact(14L,"Tester Three","test3@gmail.com","24818450554");

        when(contactRepository.findAll(Sort.by(Sort.Direction.ASC,"fullName"))).thenReturn(List.of(contact1,contact2,contact3));


        List<ContactResponse> contacts = contactService.getAllContacts();

        assertNotNull(contacts);

        assertEquals(3, contacts.size());
        assertEquals("Tester One",contacts.get(0).fullName());
        assertEquals("Tester Two",contacts.get(1).fullName());
        assertEquals("Tester Three",contacts.get(2).fullName());

    }


    @Test
    void shouldUpdateContact(){
        Contact saved = new TestContact(12L,"Test One","test@gmail.com","0874561235");
        UpdateContactRequest request = new UpdateContactRequest("Tester One","tester@gmail.com","0763314526");
        when(contactRepository.findById(12L)).thenReturn(Optional.of(saved));
        when(contactRepository.existsByEmailIgnoreCaseAndIdNot("tester@gmail.com",12L)).thenReturn(false);
        when(contactRepository.existsByPhoneNumberIgnoreCaseAndIdNot("0763314526",12L)).thenReturn(false);


        ContactResponse response = contactService.updateContact(request,12L);

        assertNotNull(response);

        assertEquals(12L,response.id());
        assertEquals("Tester One",response.fullName());
        assertEquals("tester@gmail.com",response.email());
        assertEquals("0763314526",response.phoneNumber());
    }


    @Test
    void shouldUpdateContact_WithSameEmailAndPhoneNumber(){
        Contact saved = new TestContact(12L,"Test One","test@gmail.com","0874561235");
        UpdateContactRequest request = new UpdateContactRequest("Tester One","test@gmail.com","0874561235");
        when(contactRepository.findById(12L)).thenReturn(Optional.of(saved));
        when(contactRepository.existsByEmailIgnoreCaseAndIdNot("test@gmail.com",12L)).thenReturn(false);
        when(contactRepository.existsByPhoneNumberIgnoreCaseAndIdNot("0874561235",12L)).thenReturn(false);


        ContactResponse response = contactService.updateContact(request,12L);

        assertNotNull(response);

        assertEquals(12L,response.id());
        assertEquals("Tester One",response.fullName());
        assertEquals("test@gmail.com",response.email());
        assertEquals("0874561235",response.phoneNumber());

        verify(contactRepository).findById(12L);
        verify(contactRepository).existsByEmailIgnoreCaseAndIdNot("test@gmail.com",12L);
        verify(contactRepository).existsByPhoneNumberIgnoreCaseAndIdNot("0874561235",12L);
    }



    @Test
    void shouldFailUpdateContact_ContactNotFound(){
        when(contactRepository.findById(12L)).thenThrow(ContactNotFoundException.class);

        UpdateContactRequest request = new UpdateContactRequest("Tester One","test@gmail.com","0874561235");

        assertThrows(ContactNotFoundException.class,()->contactService.updateContact(request,12L));
    }


    @Test
    void shouldFailUpdateContact_DuplicateEmail(){
        Contact saved = new TestContact(12L,"Test One","test@gmail.com","0874561235");

        when(contactRepository.findById(12L)).thenReturn(Optional.of(saved));
        when(contactRepository.existsByEmailIgnoreCaseAndIdNot("test@gmail.com",12L)).thenReturn(true);

        UpdateContactRequest request = new UpdateContactRequest("Tester One","test@gmail.com","0874561235");

        assertThrows(DuplicateContactException.class,()->{
            contactService.updateContact(request,12L);
        });
    }

    @Test
    void shouldFailUpdateContact_DuplicatePhoneNumber(){
        Contact saved = new TestContact(12L,"Test One","test@gmail.com","0874561235");

        when(contactRepository.findById(12L)).thenReturn(Optional.of(saved));
        when(contactRepository.existsByEmailIgnoreCaseAndIdNot("test@gmail.com",12L)).thenReturn(false);
        when(contactRepository.existsByPhoneNumberIgnoreCaseAndIdNot("0874561235",12L)).thenReturn(true);

        UpdateContactRequest request = new UpdateContactRequest("Tester One","test@gmail.com","0874561235");

        assertThrows(DuplicateContactException.class,()->{
            contactService.updateContact(request,12L);
        });
    }

}
