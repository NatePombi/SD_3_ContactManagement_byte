package com.arithmatrix.sd_contactmanagementsystem_byte.contact.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record UpdateContactRequest(
        @NotBlank(message = "Full name is required")
        @Size(max = 120)
        String fullName,
        @NotBlank(message = "Email is required")
        @Email(message = "Invalid email format")
        String email,
        @NotBlank(message = "Phone number is required")
        @Pattern(
                regexp = "^\\+?[0-9]{7,15}$",
                message = "Invalid phone number format"
        )
        String phoneNumber
) {}
