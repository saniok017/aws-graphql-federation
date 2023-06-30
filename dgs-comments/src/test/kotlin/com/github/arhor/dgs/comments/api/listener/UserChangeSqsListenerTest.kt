package com.github.arhor.dgs.comments.api.listener

import com.github.arhor.dgs.comments.service.CommentService
import com.ninjasquad.springmockk.MockkBean
import io.awspring.cloud.sqs.operations.SqsOperations
import io.awspring.cloud.test.sqs.SqsTest
import io.mockk.verify
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.messaging.support.GenericMessage
import org.testcontainers.junit.jupiter.Testcontainers

@SqsTest(UserChangeSqsListener::class)
@Testcontainers(disabledWithoutDocker = true)
internal class UserChangeSqsListenerTest : BaseSqsListenerTest() {

    @Autowired
    private lateinit var sqs: SqsOperations

    @MockkBean
    private lateinit var mockCommentService: CommentService

    @Test
    fun `should call unlinkCommentsFromUser method on the CommentService on User Deleted event`() {
        // Given
        val userId = 1L
        val event = UserChangeSqsListener.UserChange.Deleted(id = userId)

        // When
        sqs.send(USER_DELETED_TEST_EVENTS_QUEUE, GenericMessage(event))

        // Then
        verify(exactly = 1, timeout = 3000) { mockCommentService.unlinkCommentsFromUser(userId) }
    }
}
