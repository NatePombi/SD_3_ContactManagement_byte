package com.arithmatrix.sd_contactmanagementsystem_byte.relationship.entity;

import com.arithmatrix.sd_contactmanagementsystem_byte.contact.entity.Contact;
import com.arithmatrix.sd_contactmanagementsystem_byte.relationship.enums.RelationshipType;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "contact_relationships",
        uniqueConstraints =  {
        @UniqueConstraint(name = "uk_contact_relationship",
        columnNames = {"source_contact_id","target_contact_id","relationship_type"})
        } )
public class ContactRelationship {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne(fetch = FetchType.LAZY,optional = false)
    @JoinColumn(name = "source_contact_id", nullable = false)
    private Contact sourceContact;
    @ManyToOne(fetch = FetchType.LAZY,optional = false)
    @JoinColumn(name = "target_contact_id", nullable = false)
    private Contact targetContact;
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    @Enumerated(EnumType.STRING)
    @Column(name = "relationship_type",nullable = false,length = 30)
    private RelationshipType relationshipType;


    public static ContactRelationship create(Contact sourceContact,Contact targetContact,RelationshipType relationshipType){
        ContactRelationship relationship = new ContactRelationship();
        relationship.sourceContact = sourceContact;
        relationship.targetContact = targetContact;
        relationship.relationshipType = relationshipType;
        return relationship;
    }

    ContactRelationship(Long id, Contact sourceContact,Contact targetContact,RelationshipType relationshipType){
        this.id = id;
        this.sourceContact = sourceContact;
        this.targetContact = targetContact;
        this.relationshipType = relationshipType;
    }
}
