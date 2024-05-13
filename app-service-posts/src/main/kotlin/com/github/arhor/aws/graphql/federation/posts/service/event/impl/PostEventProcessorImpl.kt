package com.github.arhor.aws.graphql.federation.posts.service.event.impl

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.convertValue
import com.github.arhor.aws.graphql.federation.common.event.PostEvent
import com.github.arhor.aws.graphql.federation.posts.data.repository.OutboxMessageRepository
import com.github.arhor.aws.graphql.federation.posts.service.event.PostEventProcessor
import com.github.arhor.aws.graphql.federation.posts.service.event.PostEventPublisher
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Propagation
import org.springframework.transaction.annotation.Transactional

@Component
class PostEventProcessorImpl(
    private val objectMapper: ObjectMapper,
    private val outboxMessageRepository: OutboxMessageRepository,
    private val outboxEventPublisher: PostEventPublisher,
) : PostEventProcessor {

    @Scheduled(cron = "\${app-props.outbox-messages-processing-cron:}")
    @Transactional(propagation = Propagation.REQUIRED)
    override fun processPostCreatedEvents() {
        dequeueAndPublishEvents<PostEvent.Created>(PostEvent.Type.POST_EVENT_CREATED)
    }

    @Scheduled(cron = "\${app-props.outbox-messages-processing-cron:}")
    @Transactional(propagation = Propagation.REQUIRED)
    override fun processPostDeletedEvents() {
        dequeueAndPublishEvents<PostEvent.Deleted>(PostEvent.Type.POST_EVENT_DELETED)
    }

    private inline fun <reified T : PostEvent> dequeueAndPublishEvents(eventType: PostEvent.Type) {
        val outboxMessages =
            outboxMessageRepository.dequeueOldest(
                messageType = eventType.code,
                messagesNum = DEFAULT_EVENTS_BATCH_SIZE,
            )

        for (message in outboxMessages) {
            val event = objectMapper.convertValue<T>(message.data)

            outboxEventPublisher.publish(event, message.traceId)
        }
    }

    companion object {
        private const val DEFAULT_EVENTS_BATCH_SIZE = 50
    }
}
