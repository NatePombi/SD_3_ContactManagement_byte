package com.arithmatrix.sd_contactmanagementsystem_byte.contact.service;

import com.arithmatrix.sd_contactmanagementsystem_byte.contact.dto.ContactResponse;
import com.arithmatrix.sd_contactmanagementsystem_byte.contact.dto.CreateContactRequest;
import com.arithmatrix.sd_contactmanagementsystem_byte.contact.dto.UpdateContactRequest;
import com.arithmatrix.sd_contactmanagementsystem_byte.contact.entity.Contact;
import com.arithmatrix.sd_contactmanagementsystem_byte.contact.exception.ContactNotFoundException;
import com.arithmatrix.sd_contactmanagementsystem_byte.contact.exception.DuplicateContactException;
import com.arithmatrix.sd_contactmanagementsystem_byte.contact.repository.ContactRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Locale;

@Service
@RequiredArgsConstructor
public class ContactService {
    private final ContactRepository contactRepository;

    @Transactional
    public ContactResponse createContact(CreateContactRequest request) {
        String fullName = request.fullName().trim();
        String email = request.email().trim();
        String phoneNumber = request.phoneNumber().trim().toLowerCase(Locale.ROOT);

        if(contactRepository.existsByEmailIgnoreCase(email)){
            throw new DuplicateContactException("A contact with this email address already exists");
        }

        if (contactRepository.existsByPhoneNumber(phoneNumber)) {
            throw new DuplicateContactException("A contact with phone number already exists");
        }

        Contact contact = Contact.create(fullName, email, phoneNumber);

        Contact savedContact = contactRepository.save(contact);

        return new ContactResponse(savedContact.getId(),  savedContact.getFullName(), savedContact.getEmail(), savedContact.getPhoneNumber());
    }

    @Transactional
    public List<ContactResponse> getAllContacts() {
        List<Contact> contacts = contactRepository.findAll(Sort.by(Sort.Direction.ASC, "fullName"));

        return contacts.stream()
                .map(contact -> new ContactResponse(
                        contact.getId(),
                        contact.getFullName(),
                        contact.getEmail(),
                        contact.getPhoneNumber()
                ))
                .toList();
    }


    @Transactional
    public ContactResponse getContactById(Long id) {

        Contact contact = contactRepository.findById(id).orElseThrow(()-> new ContactNotFoundException("Contact with id " + id + " not found"));

        return new ContactResponse(contact.getId(), contact.getFullName(), contact.getEmail(), contact.getPhoneNumber());
    }


    @Transactional
    public ContactResponse updateContact(UpdateContactRequest request, Long id) {
        String fullName = request.fullName().trim();
        String email = request.email().trim();
        String phoneNumber = request.phoneNumber().trim().toLowerCase(Locale.ROOT);

        Contact contact = contactRepository.findById(id).orElseThrow(()-> new ContactNotFoundException("Contact with id " + id + " not found"));

        if(contactRepository.existsByEmailIgnoreCaseAndIdNot(email,id)) {
            throw new DuplicateContactException("A contact with this email address already exists");
        }

        if (contactRepository.existsByPhoneNumberIgnoreCaseAndIdNot(phoneNumber,id)) {
            throw new DuplicateContactException("A contact with phone number already exists");
        }

        contact.updateDetails(fullName, email, phoneNumber);

        return new ContactResponse(contact.getId(), contact.getFullName(), contact.getEmail(), contact.getPhoneNumber());
    }

    @Transactional
    public void deleteContact(Long id) {
        Contact contact = contactRepository.findById(id).orElseThrow(()-> new ContactNotFoundException("Contact with id " + id + " not found"));

        contactRepository.delete(contact);
    }
}
