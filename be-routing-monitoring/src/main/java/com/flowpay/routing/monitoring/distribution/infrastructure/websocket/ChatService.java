package com.flowpay.routing.monitoring.distribution.infrastructure.websocket;

import com.flowpay.routing.monitoring.distribution.infrastructure.persistence.entity.MessageJpaEntity;
import com.flowpay.routing.monitoring.distribution.infrastructure.persistence.repository.MessageJpaRepository;
import com.flowpay.routing.monitoring.distribution.infrastructure.web.dto.MessageResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

/**
 * Stores chat messages and pushes each new one to everyone watching that interaction's thread
 * — the customer on the public screen and the agent on the dashboard both subscribe to
 * {@code /topic/chat/{interactionId}}. Delivery is best-effort (the message is already saved),
 * so a broker hiccup never loses the message: the other side catches up by re-reading the thread.
 */
@Service
public class ChatService {

    public static final String CUSTOMER = "CUSTOMER";
    public static final String AGENT = "AGENT";

    private static final Logger log = LoggerFactory.getLogger(ChatService.class);
    private static final String CHAT_TOPIC = "/topic/chat/";

    private final MessageJpaRepository messages;
    private final SimpMessagingTemplate messaging;

    public ChatService(MessageJpaRepository messages, SimpMessagingTemplate messaging) {
        this.messages = messages;
        this.messaging = messaging;
    }

    public MessageResponse post(UUID interactionId, String sender, String body) {
        MessageJpaEntity saved = messages.save(
                new MessageJpaEntity(UUID.randomUUID(), interactionId, sender, body, Instant.now()));
        MessageResponse response = MessageResponse.from(saved);
        try {
            messaging.convertAndSend(CHAT_TOPIC + interactionId, response);
        } catch (RuntimeException e) {
            log.warn("Could not push chat message for {} (will be seen on refresh): {}",
                    interactionId, e.getMessage());
        }
        return response;
    }

    public List<MessageResponse> thread(UUID interactionId) {
        return messages.findByInteractionIdOrderByCreatedAtAsc(interactionId).stream()
                .map(MessageResponse::from)
                .toList();
    }
}
