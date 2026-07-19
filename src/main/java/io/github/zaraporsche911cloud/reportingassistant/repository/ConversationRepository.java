package io.github.zaraporsche911cloud.reportingassistant.repository;

import io.github.zaraporsche911cloud.reportingassistant.entity.Conversation;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ConversationRepository extends JpaRepository<Conversation, Long> {
    Page<Conversation> findByUserId(Long userId, Pageable pageable);
    Optional<Conversation> findByIdAndUserId(Long id, Long userId);
    long countByUserId(Long userId);
}
