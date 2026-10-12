package com.arithmatrix.sd_contactmanagementsystem_byte.contact.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record CreateContactRequest(
        @NotBlank(message = "Full name required")
        @Size(max =120, message = "Full name must not exceed 120 characters")
        String fullName,
        @NotBlank(message = "Email required")
        @Email(message = "Email is invalid")
        String email,
        @NotBlank(message = "Phone number required")
        @Pattern(
                regexp = "^\\+?[0-9]{7,15}$",
                message = "Phone number must contains 7 to 15 digits, with an optional + prefix"
        )
        String phoneNumber

) {}
