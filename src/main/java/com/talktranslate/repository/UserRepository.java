package com.talktranslate.repository;

import com.talktranslate.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, String> {

    Optional<User> findByUsername(String username);

    Optional<User> findByEmail(String email);

    Optional<User> findByUsernameOrEmail(String username, String email);

    @Query("SELECT u FROM User u WHERE u.username = :identifier OR u.email = :identifier")
    Optional<User> findByIdentifier(@Param("identifier") String identifier);

    @Query("SELECT u FROM User u WHERE u.id != :currentUserId AND u.id NOT IN " +
           "(SELECT CASE WHEN f.requester.id = :currentUserId THEN f.addressee.id ELSE f.requester.id END " +
           "FROM Friendship f WHERE f.requester.id = :currentUserId OR f.addressee.id = :currentUserId)")
    List<User> findSuggestedUsers(@Param("currentUserId") String currentUserId);

    boolean existsByUsername(String username);

    boolean existsByEmail(String email);
}
