// Defines the package namespace containing Spring Data JPA repository interfaces
package com.spring.repository;

// Imports the Comment entity managed by this repository
import com.spring.entity.Comment;
// Imports JpaRepository providing full CRUD and pagination database operations
import org.springframework.data.jpa.repository.JpaRepository;
// Imports Spring stereotype repository annotation for component scanning and exception translation
import org.springframework.stereotype.Repository;

// Imports Java utility List collection interface for returning query result sets
import java.util.List;

// Marks this interface as a Spring Data repository component bean
@Repository
// Declares repository interface extending JpaRepository for Comment entity persistence
public interface CommentRepository extends JpaRepository<Comment, Long> {

    /**
     * Finds ticket comments chronologically.
     */
    // Generates dynamic query finding comments by ticket ID ordered by creation time ascending
    List<Comment> findByTicketIdOrderByCreatedAtAsc(Long ticketId);
// Closes the CommentRepository interface definition
}
