package com.arithmatrix.sd_contactmanagementsystem_byte.contact.repository;

import com.arithmatrix.sd_contactmanagementsystem_byte.contact.entity.Contact;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ContactRepository extends JpaRepository<Contact,Long> {
    boolean existsByPhoneNumber(String phoneNumber);
    boolean existsByEmailIgnoreCase(String email);

    Contact findByFullName(String testerTwo);

    boolean existsByEmailIgnoreCaseAndIdNot(String email, Long id);
    boolean existsByPhoneNumberIgnoreCaseAndIdNot(String phoneNumber, Long id);

}
