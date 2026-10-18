
import { useState } from "react";

export default function EditContactForm({
    contact,
    onUpdated,
    onCancel
}) {

    // Prepopulate the form with existing contact details
    const [form, setForm] = useState(() => ({
        fullName: contact.fullName,
        email: contact.email,
        phoneNumber: contact.phoneNumber
    }));

    const [submitting, setSubmitting] = useState(false);

    const [generalError, setGeneralError] = useState("");

    const [fieldErrors, setFieldErrors] = useState({});

    function handleChange(event) {

        const { name, value } = event.target;

        setForm(previous => ({
            ...previous,
            [name]: value
        }));

        setFieldErrors(previous => ({
            ...previous,
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

            const response = await fetch(
                `/api/v1/contacts/update/${contact.id}`,
                {
                    method: "PUT",

                    headers: {
                        "Content-Type": "application/json"
                    },

                    body: JSON.stringify(requestBody)
                }
            );

            const data = await response.json().catch(() => null);

            if (!response.ok) {

                if (data?.errors) {
                    setFieldErrors(data.errors);
                }

                setGeneralError(
                    data?.message ||
                    `Unable to update contact (HTTP ${response.status})`
                );

                return;
            }

            // Notify the parent component
            onUpdated(data);

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
            className="edit-contact-form"
            onSubmit={handleSubmit}
        >

            <div className="form-heading">
                <h3>Edit Contact</h3>

                <p>
                    Update the details for {contact.fullName}.
                </p>
            </div>

            {generalError && (
                <div className="form-error" role="alert">
                    {generalError}
                </div>
            )}

            <div className="form-field">
                <label htmlFor="edit-fullName">
                    Full Name
                </label>

                <input
                    id="edit-fullName"
                    name="fullName"
                    type="text"
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
                <label htmlFor="edit-email">
                    Email Address
                </label>

                <input
                    id="edit-email"
                    name="email"
                    type="email"
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
                <label htmlFor="edit-phoneNumber">
                    Phone Number
                </label>

                <input
                    id="edit-phoneNumber"
                    name="phoneNumber"
                    type="tel"
                    inputMode="tel"
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
                    {submitting ? "Updating..." : "Save Changes"}
                </button>

            </div>

        </form>
    );
}
