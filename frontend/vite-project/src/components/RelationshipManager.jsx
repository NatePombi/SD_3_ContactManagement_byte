
import { useEffect, useState } from "react";
import { apiUrl } from "../config/api.js";


export default function RelationshipManager( {onCreated} ) {

    // Contacts loaded from Spring Boot
    const [contacts, setContacts] = useState([]);

    // Form state
    const [sourceId, setSourceId] = useState("");
    const [targetId, setTargetId] = useState("");
    const [relationshipType, setRelationshipType] =
        useState("MANAGES");

    // Request states
    const [loading, setLoading] = useState(true);
    const [submitting, setSubmitting] = useState(false);

    const [error, setError] = useState("");
    const [successMessage, setSuccessMessage] = useState("");

    // Used when manually refreshing the contact choices
    const [reloadCount, setReloadCount] = useState(0);

    useEffect(() => {

        const controller = new AbortController();

        async function loadContacts() {

            setLoading(true);
            setError("");

            try {

                const response = await fetch(apiUrl(
                    "/api/v1/contacts/fetchAll"),
                    {
                        signal: controller.signal
                    }
                );

                if (!response.ok) {
                    throw new Error(
                        `Unable to load contacts (HTTP ${response.status})`
                    );
                }

                const data = await response.json();

                if (!Array.isArray(data)) {
                    throw new Error(
                        "Unexpected response from the server."
                    );
                }

                if (!controller.signal.aborted) {
                    setContacts(data);
                }

            } catch (err) {

                if (!controller.signal.aborted) {
                    setError(err.message);
                }

            } finally {

                if (!controller.signal.aborted) {
                    setLoading(false);
                }
            }
        }

        loadContacts();

        return () => controller.abort();

    }, [reloadCount]);

    // Targets must be different from the source
    const availableTargets = contacts.filter(
        contact => String(contact.id) !== sourceId
    );

    async function handleSubmit(event) {

        event.preventDefault();

        if (submitting) {
            return;
        }

        // Client-side business validation
        if (!sourceId || !targetId) {
            setError("Please select both contacts.");
            return;
        }

        if (sourceId === targetId) {
            setError(
                "A contact cannot have a relationship with itself."
            );
            return;
        }

        setSubmitting(true);
        setError("");
        setSuccessMessage("");

        try {

            const requestBody = {
                sourceContactId: Number(sourceId),
                targetContactId: Number(targetId),
                relationshipType: relationshipType
            };

            const response = await fetch(apiUrl(
                "/api/v1/relationships"),
                {
                    method: "POST",

                    headers: {
                        "Content-Type": "application/json"
                    },

                    body: JSON.stringify(requestBody)
                }
            );

            const data = await response.json().catch(() => null);

            if (!response.ok) {
                throw new Error(
                    data?.message ||
                    `Unable to create relationship (HTTP ${response.status})`
                );
            }

            setSuccessMessage(
                `${data.sourceContactName} ${data.relationshipType} ${data.targetContactName}`
            );

            onCreated?.(data);

            // Clear selection for another relationship
            setSourceId("");
            setTargetId("");
            setRelationshipType("MANAGES");

        } catch (err) {

            setError(
                err.message || "Unable to create relationship."
            );

        } finally {

            setSubmitting(false);
        }
    }

    return (
        <section
            className="relationship-section"
            id="relationships"
        >

            <div className="relationship-header">

                <div>
                    <span className="section-label">
                        SIX DEGREES NETWORK
                    </span>

                    <h2>Contact Relationships</h2>

                    <p>
                        Link contacts together to build your network.
                    </p>
                </div>

                <button
                    type="button"
                    className="secondary-button"
                    onClick={() =>
                        setReloadCount(previous => previous + 1)
                    }
                    disabled={loading || submitting}
                >
                    Refresh Contacts
                </button>

            </div>

            {loading ? (

                <div className="loading-state" role="status">
                    Loading available contacts...
                </div>

            ) : (

                <form
                    className="relationship-form"
                    onSubmit={handleSubmit}
                >

                    {error && (
                        <div className="form-error" role="alert">
                            {error}
                        </div>
                    )}

                    {successMessage && (
                        <div
                            className="success-message"
                            role="status"
                        >
                            Relationship created: {successMessage}
                        </div>
                    )}

                    {contacts.length < 2 && (

                        <p className="relationship-notice">
                            You need at least two contacts to
                            create a relationship.
                        </p>

                    )}

                    <div className="relationship-grid">

                        <div className="form-field">

                            <label htmlFor="sourceContact">
                                Source Contact
                            </label>

                            <select
                                id="sourceContact"
                                value={sourceId}
                                onChange={(event) => {
                                    setSourceId(event.target.value);
                                    setTargetId("");
                                    setError("");
                                }}
                                required
                                disabled={submitting}
                            >
                                <option value="">
                                    Select a contact
                                </option>

                                {contacts.map(contact => (
                                    <option
                                        key={contact.id}
                                        value={contact.id}
                                    >
                                        {contact.fullName}
                                    </option>
                                ))}

                            </select>

                        </div>

                        <div className="form-field">

                            <label htmlFor="relationshipType">
                                Relationship Type
                            </label>

                            <select
                                id="relationshipType"
                                value={relationshipType}
                                onChange={(event) =>
                                    setRelationshipType(
                                        event.target.value
                                    )
                                }
                                disabled={submitting}
                            >
                                <option value="MANAGES">
                                    Manages
                                </option>

                                <option value="MENTORS">
                                    Mentors
                                </option>

                                <option value="KNOWS">
                                    Knows
                                </option>
                            </select>

                        </div>

                        <div className="form-field">

                            <label htmlFor="targetContact">
                                Target Contact
                            </label>

                            <select
                                id="targetContact"
                                value={targetId}
                                onChange={(event) => {
                                    setTargetId(event.target.value);
                                    setError("");
                                }}
                                required
                                disabled={!sourceId || submitting}
                            >
                                <option value="">
                                    Select a contact
                                </option>

                                {availableTargets.map(contact => (
                                    <option
                                        key={contact.id}
                                        value={contact.id}
                                    >
                                        {contact.fullName}
                                    </option>
                                ))}

                            </select>

                        </div>

                    </div>

                    <div className="relationship-actions">

                        <button
                            type="submit"
                            className="primary-button"
                            disabled={
                                submitting ||
                                contacts.length < 2
                            }
                        >
                            {submitting
                                ? "Creating..."
                                : "Create Relationship"}
                        </button>

                    </div>

                </form>

            )}

        </section>
    );
}
