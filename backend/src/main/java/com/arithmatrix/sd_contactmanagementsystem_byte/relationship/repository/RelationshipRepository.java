package com.arithmatrix.sd_contactmanagementsystem_byte.relationship.repository;

import com.arithmatrix.sd_contactmanagementsystem_byte.relationship.entity.ContactRelationship;
import com.arithmatrix.sd_contactmanagementsystem_byte.relationship.enums.RelationshipType;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface RelationshipRepository extends JpaRepository<ContactRelationship,Long> {

    boolean existsBySourceContact_IdAndTargetContact_IdAndRelationshipType(
            Long id,
            Long targetContactId,
            RelationshipType relationshipType
    );

    @EntityGraph(attributePaths = {
            "sourceContact",
            "targetContact"
    })
    List<ContactRelationship> findBySourceContact_IdOrTargetContact_Id(
            Long sourceContactId,
            Long targetContactId
    );
}
