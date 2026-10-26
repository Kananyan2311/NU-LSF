package com.example.nulsf.repository;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.example.nulsf.model.Conversation;
import com.example.nulsf.model.Item;
import com.example.nulsf.model.User;

@Repository
public interface ConversationRepository extends JpaRepository<Conversation, Long> {

    List<Conversation> findByUserOrderByUpdatedAtDesc(User user);

    List<Conversation> findAllByOrderByUpdatedAtDesc();

    Optional<Conversation> findByUserAndItem(User user, Item item);

    List<Conversation> findByItem(Item item);
}
