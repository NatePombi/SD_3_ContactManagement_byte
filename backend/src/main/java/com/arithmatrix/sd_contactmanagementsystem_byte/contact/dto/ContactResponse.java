package com.arithmatrix.sd_contactmanagementsystem_byte.contact.dto;

public record ContactResponse(
        Long id,
        String fullName,
        String email,
        String phoneNumber
) {}
