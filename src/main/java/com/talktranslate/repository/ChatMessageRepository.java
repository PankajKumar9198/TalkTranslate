package com.talktranslate.repository;

import com.talktranslate.model.ChatMessage;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ChatMessageRepository extends JpaRepository<ChatMessage, String> {

    @Query("SELECT m FROM ChatMessage m WHERE " +
           "((m.senderId = :userA AND m.recipientId = :userB) OR " +
           " (m.senderId = :userB AND m.recipientId = :userA)) " +
           "ORDER BY m.createdAt ASC")
    List<ChatMessage> findChatHistoryBetween(@Param("userA") String userA, @Param("userB") String userB);

    @Query("SELECT m FROM ChatMessage m WHERE " +
           "((m.senderId = :userA AND m.recipientId = :userB) OR " +
           " (m.senderId = :userB AND m.recipientId = :userA)) " +
           "ORDER BY m.createdAt ASC")
    Page<ChatMessage> findChatHistoryBetween(@Param("userA") String userA, @Param("userB") String userB, Pageable pageable);

    List<ChatMessage> findByRoomIdOrderByCreatedAtAsc(String roomId);
}
