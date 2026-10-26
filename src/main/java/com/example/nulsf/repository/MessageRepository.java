package com.example.nulsf.repository;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.example.nulsf.model.Conversation;
import com.example.nulsf.model.Message;
import com.example.nulsf.model.User;

@Repository
public interface MessageRepository extends JpaRepository<Message, Long> {

    List<Message> findByConversationOrderBySentAtAsc(Conversation conversation);

    long countByConversationAndSenderNotAndIsReadFalse(Conversation conversation, User notSender);

    long countBySenderNotAndIsReadFalse(User notSender);
}
