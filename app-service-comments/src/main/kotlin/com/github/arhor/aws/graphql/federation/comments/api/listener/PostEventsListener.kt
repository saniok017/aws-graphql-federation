package com.github.arhor.aws.graphql.federation.comments.api.listener

import com.github.arhor.aws.graphql.federation.comments.service.CommentService
import com.github.arhor.aws.graphql.federation.common.event.PostEvent
import io.awspring.cloud.sqs.annotation.SqsListener
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

@Component
class PostEventsListener @Autowired constructor(
    private val commentService: CommentService,
) {

    @SqsListener("\${app-props.aws.sqs.post-deleted-events}")
    fun handlePostDeletedEvent(event: PostEvent.Deleted) {
        val deletedUserId = event.id

        logger.debug("Processing post deleted event with id: {}", deletedUserId)
        commentService.deleteCommentsFromPost(postId = deletedUserId)
    }

    companion object {
        private val logger = LoggerFactory.getLogger(this::class.java.enclosingClass)
    }
}
