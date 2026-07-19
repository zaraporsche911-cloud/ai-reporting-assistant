package io.github.zaraporsche911cloud.reportingassistant.conversation;

import io.github.zaraporsche911cloud.reportingassistant.audit.AuditService;
import io.github.zaraporsche911cloud.reportingassistant.dto.common.PageResponse;
import io.github.zaraporsche911cloud.reportingassistant.dto.conversation.ConversationDtos;
import io.github.zaraporsche911cloud.reportingassistant.entity.AppUser;
import io.github.zaraporsche911cloud.reportingassistant.entity.Conversation;
import io.github.zaraporsche911cloud.reportingassistant.entity.Message;
import io.github.zaraporsche911cloud.reportingassistant.entity.MessageAuthor;
import io.github.zaraporsche911cloud.reportingassistant.exception.ResourceNotFoundException;
import io.github.zaraporsche911cloud.reportingassistant.repository.ConversationRepository;
import io.github.zaraporsche911cloud.reportingassistant.repository.MessageRepository;
import io.github.zaraporsche911cloud.reportingassistant.service.CurrentUserService;
import io.github.zaraporsche911cloud.reportingassistant.service.PageRequestFactory;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;

@Service
@Transactional(readOnly = true)
public class ConversationService {

    private final ConversationRepository conversations;
    private final MessageRepository messages;
    private final CurrentUserService currentUserService;
    private final PageRequestFactory pageRequestFactory;
    private final AuditService auditService;

    public ConversationService(
            ConversationRepository conversations,
            MessageRepository messages,
            CurrentUserService currentUserService,
            PageRequestFactory pageRequestFactory,
            AuditService auditService
    ) {
        this.conversations = conversations;
        this.messages = messages;
        this.currentUserService = currentUserService;
        this.pageRequestFactory = pageRequestFactory;
        this.auditService = auditService;
    }

    @Transactional
    public Conversation create(AppUser user, String title) {
        return conversations.save(new Conversation(user, title));
    }

    public Conversation findOwned(Long id, AppUser user) {
        return conversations.findByIdAndUserId(id, user.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Conversation", id));
    }

    public PageResponse<ConversationDtos.Response> list(int page, int size) {
        AppUser user = currentUserService.requireCurrentUser();
        var pageable = pageRequestFactory.create(page, size, "updatedAt", Sort.Direction.DESC, Set.of("updatedAt"));
        return PageResponse.from(conversations.findByUserId(user.getId(), pageable).map(this::response));
    }

    public ConversationDtos.Details details(Long id) {
        AppUser user = currentUserService.requireCurrentUser();
        Conversation conversation = findOwned(id, user);
        return new ConversationDtos.Details(response(conversation),
                messages.findByConversationIdOrderByCreatedAtAsc(id).stream().map(this::messageResponse).toList());
    }

    @Transactional
    public ConversationDtos.Response rename(Long id, ConversationDtos.RenameRequest request) {
        AppUser user = currentUserService.requireCurrentUser();
        Conversation conversation = findOwned(id, user);
        conversation.rename(request.title());
        auditService.record(user.getEmail(), "CONVERSATION_RENAMED", "CONVERSATION", id, null);
        return response(conversation);
    }

    @Transactional
    public void delete(Long id) {
        AppUser user = currentUserService.requireCurrentUser();
        Conversation conversation = findOwned(id, user);
        conversations.delete(conversation);
        auditService.record(user.getEmail(), "CONVERSATION_DELETED", "CONVERSATION", id, null);
    }

    @Transactional
    public Message addMessage(Conversation conversation, MessageAuthor author, String content, Long reportId) {
        return messages.save(new Message(conversation, author, content, reportId));
    }

    public ConversationDtos.Response response(Conversation conversation) {
        return new ConversationDtos.Response(conversation.getId(), conversation.getTitle(), conversation.getCreatedAt(), conversation.getUpdatedAt());
    }

    public ConversationDtos.MessageResponse messageResponse(Message message) {
        return new ConversationDtos.MessageResponse(
                message.getId(), message.getAuthor().name(), message.getContent(), message.getGeneratedReportId(), message.getCreatedAt());
    }
}
