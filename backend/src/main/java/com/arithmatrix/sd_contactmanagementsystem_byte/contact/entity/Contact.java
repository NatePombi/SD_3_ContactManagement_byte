package com.arithmatrix.sd_contactmanagementsystem_byte.contact.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
@Table(name = "tables")
public class Contact {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false, name = "full_name",length = 120)
    private String fullName;
    @Column(nullable = false,length = 254)
    private String email;
    @Column(nullable = false,name = "phone_number",length = 30)
    private String phoneNumber;


    public static Contact create(String fullName,String email,String phoneNumber){
        Contact contact = new Contact();
        contact.fullName = fullName;
        contact.email = email;
        contact.phoneNumber = phoneNumber;
        return contact;
    }

    public void updateDetails(String fullName, String email, String phoneNumber){
        this.fullName = fullName;
        this.email = email;
        this.phoneNumber = phoneNumber;
    }
}
