
import { useState } from "react";
import { apiUrl } from "../config/api.js";

const emptyForm = {
    fullName: "",
    email: "",
    phoneNumber: ""
};

export default function AddContactForm({ onCreated, onCancel }) {

    const [form, setForm] = useState(emptyForm);

    const [submitting, setSubmitting] = useState(false);

    const [generalError, setGeneralError] = useState("");

    const [fieldErrors, setFieldErrors] = useState({});

    function handleChange(event) {

        const { name, value } = event.target;

        setForm(previousForm => ({
            ...previousForm,
            [name]: value
        }));

        setFieldErrors(previousErrors => ({
            ...previousErrors,
            [name]: undefined
        }));

        setGeneralError("");
    }

    async function handleSubmit(event) {

        event.preventDefault();

        if (submitting) {
            return;
        }

        setSubmitting(true);
        setGeneralError("");
        setFieldErrors({});

        try {

            const requestBody = {
                fullName: form.fullName.trim(),
                email: form.email.trim(),
                phoneNumber: form.phoneNumber.trim()
            };

            const response = await fetch(apiUrl("/api/v1/contacts"), {
                method: "POST",

                headers: {
                    "Content-Type": "application/json"
                },

                body: JSON.stringify(requestBody)
            });

            const data = await response.json().catch(() => null);

            if (!response.ok) {

                if (data?.errors) {
                    setFieldErrors(data.errors);
                }

                setGeneralError(
                    data?.message ||
                    `Unable to create contact (HTTP ${response.status})`
                );

                return;
            }

            // Contact successfully saved
            setForm(emptyForm);

            // Notify the parent component
            onCreated(data);

        } catch (error) {

            setGeneralError(
                error.message || "Unable to connect to the server."
            );

        } finally {

            setSubmitting(false);
        }
    }

    return (
        <form
            className="add-contact-form"
            onSubmit={handleSubmit}
        >

            <div className="form-heading">
                <div>
                    <h3>Add New Contact</h3>
                    <p>Enter the contact's information below.</p>
                </div>
            </div>

            {generalError && (
                <div className="form-error" role="alert">
                    {generalError}
                </div>
            )}

            <div className="form-field">
                <label htmlFor="contact-fullName">
                    Full Name
                </label>

                <input
                    id="contact-fullName"
                    name="fullName"
                    type="text"
                    placeholder="John Smith"
                    value={form.fullName}
                    onChange={handleChange}
                    maxLength={120}
                    required
                    disabled={submitting}
                    aria-invalid={Boolean(fieldErrors.fullName)}
                />

                {fieldErrors.fullName && (
                    <span className="field-error">
                        {fieldErrors.fullName}
                    </span>
                )}
            </div>

            <div className="form-field">
                <label htmlFor="contact-email">
                    Email Address
                </label>

                <input
                    id="contact-email"
                    name="email"
                    type="email"
                    placeholder="john@example.com"
                    value={form.email}
                    onChange={handleChange}
                    maxLength={254}
                    required
                    disabled={submitting}
                    aria-invalid={Boolean(fieldErrors.email)}
                />

                {fieldErrors.email && (
                    <span className="field-error">
                        {fieldErrors.email}
                    </span>
                )}
            </div>

            <div className="form-field">
                <label htmlFor="contact-phone">
                    Phone Number
                </label>

                <input
                    id="contact-phone"
                    name="phoneNumber"
                    type="tel"
                    inputMode="tel"
                    placeholder="0821234567"
                    value={form.phoneNumber}
                    onChange={handleChange}
                    pattern="[+]?[0-9]{7,15}"
                    title="Enter 7 to 15 digits, optionally starting with +"
                    required
                    disabled={submitting}
                    aria-invalid={Boolean(fieldErrors.phoneNumber)}
                />

                {fieldErrors.phoneNumber && (
                    <span className="field-error">
                        {fieldErrors.phoneNumber}
                    </span>
                )}
            </div>

            <div className="form-actions">

                <button
                    type="button"
                    className="secondary-button"
                    onClick={onCancel}
                    disabled={submitting}
                >
                    Cancel
                </button>

                <button
                    type="submit"
                    className="primary-button"
                    disabled={submitting}
                >
                    {submitting ? "Saving..." : "Save Contact"}
                </button>

            </div>

        </form>
    );
}
