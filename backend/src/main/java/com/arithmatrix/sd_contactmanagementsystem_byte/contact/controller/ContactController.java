package com.arithmatrix.sd_contactmanagementsystem_byte.contact.controller;

import com.arithmatrix.sd_contactmanagementsystem_byte.contact.dto.ContactResponse;
import com.arithmatrix.sd_contactmanagementsystem_byte.contact.dto.CreateContactRequest;
import com.arithmatrix.sd_contactmanagementsystem_byte.contact.dto.UpdateContactRequest;
import com.arithmatrix.sd_contactmanagementsystem_byte.contact.entity.Contact;
import com.arithmatrix.sd_contactmanagementsystem_byte.contact.service.ContactService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/contacts")
@RequiredArgsConstructor
public class ContactController {

    private final ContactService contactService;

    @PostMapping
    public ResponseEntity<ContactResponse> createContact(@Valid @RequestBody CreateContactRequest request){
        return ResponseEntity.status(HttpStatus.CREATED).body(contactService.createContact(request));
    }

    @GetMapping("/fetchAll")
    public ResponseEntity<List<ContactResponse>> fetchAllContacts(@RequestParam(required = false) String search){

        List<ContactResponse> contacts;

        if(search == null || search.isBlank()){
            contacts = contactService.getAllContacts();
        }
        else{
            contacts = contactService.searchContacts(search);
        }

        return ResponseEntity.ok(contacts);
    }

    @GetMapping("/fetch/{id}")
    public ResponseEntity<ContactResponse> fetchContact(@PathVariable Long id){
        return ResponseEntity.ok(contactService.getContactById(id));
    }

    @PutMapping("/update/{id}")
    public ResponseEntity<ContactResponse> updateContact(@PathVariable Long id, @Valid @RequestBody UpdateContactRequest request){
        return ResponseEntity.ok(contactService.updateContact(request,id));
    }


    @DeleteMapping("/delete/{id}")
    public ResponseEntity<Void> deleteContact(@PathVariable Long id){
        contactService.deleteContact(id);

        return ResponseEntity.noContent().build();
    }
}
