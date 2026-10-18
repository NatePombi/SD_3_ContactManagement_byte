
export default function ContactCard({
    contact,
    onEdit,
    onDelete
}) {

    const initials = contact.fullName
        .trim()
        .split(/\s+/)
        .filter(Boolean)
        .slice(0, 2)
        .map(name => name[0])
        .join("")
        .toUpperCase();

    return (
        <article className="contact-card">

            <div className="contact-avatar">
                {initials}
            </div>

            <div className="contact-details">

                <h3>{contact.fullName}</h3>

                <p>{contact.email}</p>

                <p>{contact.phoneNumber}</p>

            </div>

            <div className="contact-actions">

                <button
                    type="button"
                    className="edit-contact-button"
                    onClick={() => onEdit(contact)}
                    aria-label={`Edit ${contact.fullName}`}
                >
                    Edit
                </button>

                <button
                    type="button"
                    className="delete-contact-button"
                    onClick={() => onDelete(contact)}
                    aria-label={`Delete ${contact.fullName}`}
                >
                    Delete
                </button>

            </div>

        </article>
    );
}
