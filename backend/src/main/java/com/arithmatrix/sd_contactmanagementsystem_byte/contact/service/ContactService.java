package com.arithmatrix.sd_contactmanagementsystem_byte.contact.service;

import com.arithmatrix.sd_contactmanagementsystem_byte.common.exception.ConflictException;
import com.arithmatrix.sd_contactmanagementsystem_byte.contact.dto.ContactResponse;
import com.arithmatrix.sd_contactmanagementsystem_byte.contact.dto.CreateContactRequest;
import com.arithmatrix.sd_contactmanagementsystem_byte.contact.entity.Contact;
import com.arithmatrix.sd_contactmanagementsystem_byte.contact.exception.DuplicateContactException;
import com.arithmatrix.sd_contactmanagementsystem_byte.contact.repository.ContactRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

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
}
