package com.talktranslate.repository;

import com.talktranslate.model.Friendship;
import com.talktranslate.model.FriendshipStatus;
import com.talktranslate.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FriendshipRepository extends JpaRepository<Friendship, String> {

    @Query("SELECT f FROM Friendship f WHERE " +
           "(f.requester.id = :userA AND f.addressee.id = :userB) OR " +
           "(f.requester.id = :userB AND f.addressee.id = :userA)")
    Optional<Friendship> findFriendshipBetween(@Param("userA") String userA, @Param("userB") String userB);

    @Query("SELECT f FROM Friendship f WHERE " +
           "((f.requester.id = :userA AND f.addressee.id = :userB) OR " +
           " (f.requester.id = :userB AND f.addressee.id = :userA)) AND f.status = 'ACCEPTED'")
    Optional<Friendship> findAcceptedFriendship(@Param("userA") String userA, @Param("userB") String userB);

    List<Friendship> findAllByAddresseeAndStatus(User addressee, FriendshipStatus status);

    List<Friendship> findAllByRequesterAndStatus(User requester, FriendshipStatus status);

    @Query("SELECT f FROM Friendship f WHERE " +
           "(f.requester.id = :userId OR f.addressee.id = :userId) AND f.status = 'ACCEPTED'")
    List<Friendship> findAllAcceptedFriendships(@Param("userId") String userId);

    @Query("SELECT CASE WHEN f.requester.id = :userId THEN f.addressee.id ELSE f.requester.id END " +
           "FROM Friendship f WHERE (f.requester.id = :userId OR f.addressee.id = :userId)")
    List<String> findAllConnectedUserIds(@Param("userId") String userId);
}
