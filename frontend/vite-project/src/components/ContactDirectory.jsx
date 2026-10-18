import { useEffect, useState } from "react";
import ContactCard from "./ContactCard";
import AddContactForm from "./AddContactForm";
import EditContactForm from "./EditContactForm";



export default function ContactDirectory() {
    //Contacts returned by springboot
    const [contacts,setContacts] = useState([]);
    
    //search input
    const [search, setSearch] = useState("");

    //API request states
    const [loading,setLoading] = useState(true);
    const [error, setError] = useState(null);

    //Allows user to retry loading 
    const [reloadCount, setReloadCount] = useState(0);

    const [showAddForm, setShowAddForm] = useState(false);
    const [successMessage, setSuccessMessage] = useState("");

    const [editingContact, setEditingContact] = useState(null);

        
    const [deletingContact, setDeletingContact] = useState(null);
    const [deleting, setDeleting] = useState(false);
    const [deleteError, setDeleteError] = useState("");



        
    function handleContactCreated() {

        // Close the form
        setShowAddForm(false);

        // Clear the search so the new contact is visible
        setSearch("");

        // Show a success notification
        setSuccessMessage("Contact created successfully.");

        // Trigger the existing useEffect to reload contacts
        setReloadCount(previous => previous + 1);
    }

        
    function handleEditContact(contact) {

        // Close the Add Contact form
        setShowAddForm(false);

        // Open the Edit Contact form
        setEditingContact(contact);

        // Clear previous success messages
        setSuccessMessage("");

        setDeletingContact(null);
        setDeleteError("");
    }

        

    function handleContactUpdated() {

        // Close the Edit Contact form
        setEditingContact(null);

        // Clear any active search filter
        setSearch("");

        // Show a success message
        setSuccessMessage("Contact updated successfully.");

        // Fetch the latest contacts from Spring Boot
        setReloadCount(previous => previous + 1);
    }

        
    function handleDeleteContact(contact) {

        if (deleting) {
            return;
        }

        // Close other forms
        setShowAddForm(false);
        setEditingContact(null);

        // Clear previous messages
        setSuccessMessage("");
        setDeleteError("");

        // Show confirmation for selected contact
        setDeletingContact(contact);
    }




    function handleCancelDelete() {

        if (deleting) {
            return;
        }

        setDeletingContact(null);
        setDeleteError("");
    }

        
    async function handleConfirmDelete() {

        if (!deletingContact || deleting) {
            return;
        }

        setDeleting(true);
        setDeleteError("");
        setSuccessMessage("");

        try {

            const response = await fetch(
                `/api/v1/contacts/delete/${deletingContact.id}`,
                {
                    method: "DELETE"
                }
            );

            if (!response.ok) {

                const data = await response.json().catch(() => null);

                throw new Error(
                    data?.message ||
                    `Unable to delete contact (HTTP ${response.status})`
                );
            }

            // A successful DELETE returns 204 No Content.
            // Do not call response.json() here.

            setDeletingContact(null);

            setSearch("");

            setSuccessMessage("Contact deleted successfully.");

            // Reload the actual contacts from Spring Boot
            setReloadCount(previous => previous + 1);

        } catch (error) {

            setDeleteError(
                error.message || "Unable to delete the contact."
            );

        } finally {

            setDeleting(false);
        }
    }




    useEffect(() => {
        const controller = new AbortController();

        async function loadContacts() {
            setLoading(true);
            setError(null);

            try {

                const response = await fetch(
                    "/api/v1/contacts/fetchAll",
                    {
                        signal: controller.signal
                    }
                );

                if(!response.ok){
                    throw new Error(
                        `Failed to load Contacts. HTTP ${response.status}`
                    );
                    
                }

                const data = await response.json();

                if(!Array.isArray(data)){
                    throw new Error("Unexpected response from the server");
                }
            
                if(!controller.signal.aborted){
                    setContacts(data);
                }

            }catch(err){
                if (!controller.signal.aborted){
                    setError(err.mesaage);
                }
            }
            finally {

                if(!controller.signal.aborted) {
                    setLoading(false);
                }
            }
        }

        loadContacts();

        return () => {
            controller.abort();
        };
    },[reloadCount]);

    //filter the contacts returned by backend
    const keyword = search.trim().toLowerCase();

    const filteredContacts = contacts.filter(contact => {

        return (
            contact.fullName.toLowerCase().includes(keyword) ||
            contact.email.toLowerCase().includes(keyword) ||
            contact.phoneNumber.includes(keyword)
        );
    });

    return (
        <section className="directory-section">

            <div className="directory-header">
                <div>
                    <span className="section-label">
                        CONTACT DIRECTORY
                    </span>

                    <h2>All Contacts</h2>

                    <p>
                        Contacts stored in your Spring Boot Database.
                    </p>
                </div>
                    <span className="contact-count">
                        {loading
                            ? "Loading..."
                            : `${filteredContacts.length} contacts`
                        }
                    </span>

                    <button
                        type="button"
                        className="primary-button"
                        onClick={() => {
                            setShowAddForm(previous => !previous);
                            setEditingContact(null);
                            setSuccessMessage("");
                        }}
                    >
                        {showAddForm ? "Close Form" : "+ Add Contact"}
                    </button>
            </div>

                            
                {successMessage && (
                    <div className="success-message" role="status">
                        {successMessage}
                    </div>
                )}

                {showAddForm && (

                    <AddContactForm
                        onCreated={handleContactCreated}
                        onCancel={() => setShowAddForm(false)}
                    />

                )}

                                
                {editingContact && (

                    <EditContactForm
                        key={editingContact.id}
                        contact={editingContact}
                        onUpdated={handleContactUpdated}
                        onCancel={() => setEditingContact(null)}
                    />

                )}


                {deletingContact && (

                    <section
                        className="delete-confirmation"
                        aria-labelledby="delete-confirmation-title"
                    >

                        <div className="delete-confirmation-heading">

                            <h3 id="delete-confirmation-title">
                                Delete {deletingContact.fullName}?
                            </h3>

                            <p>
                                Are you sure you want to delete this contact?
                            </p>

                            <p className="delete-warning">
                                This action cannot be undone. Any relationships
                                involving this contact will also be removed.
                            </p>

                        </div>

                        {deleteError && (

                            <div className="form-error" role="alert">
                                {deleteError}
                            </div>

                        )}

                        <div className="form-actions">

                            <button
                                type="button"
                                className="secondary-button"
                                onClick={handleCancelDelete}
                                disabled={deleting}
                            >
                                Cancel
                            </button>

                            <button
                                type="button"
                                className="confirm-delete-button"
                                onClick={handleConfirmDelete}
                                disabled={deleting}
                            >
                                {deleting ? "Deleting..." : "Yes, Delete Contact"}
                            </button>

                        </div>

                    </section>

                )}


                <div className="directory-search">

                    <input
                        type="search"
                        placeholder="Search by name, email, phone number"
                        aria-label="Search contacts"
                        value={search}
                        onChange={(event) =>
                            setSearch(event.target.value)
                        }
                    />

                    <button
                        type="button"
                        onClick={() => setSearch("")}
                    >
                        Clear
                    </button>
                
                </div>

                {loading ? (

                    <div className="loading-state" role="status">
                        Loading contacts from the server... 
                    </div>

                ) : error ? (

                    <div className="error-state" role="alert">

                        <p>{error}</p>

                        <button
                            type="button"
                            className="retry-button"
                            onClick={() =>
                                setReloadCount(count => count + 1)
                            }
                        >
                            Try Again
                        </button>
                    </div>
                ) : filteredContacts.length === 0 ? (
                    <div className="empty-state">
                        <p>
                            {contacts.length === 0
                                ? "No contacts have been created yet."
                                : "No contacts match your search."
                            }
                        </p>
                    </div>


                ) : (
                    <div className="contact-list">
                        {filteredContacts.map(contact => (

                            <ContactCard 
                                key={contact.id}
                                contact={contact}
                                onEdit={handleEditContact}
                                onDelete={handleDeleteContact}
                            />
                        ))}

                     </div>
                )}
        </section>
    );
    
}