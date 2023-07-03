package com.github.arhor.dgs.comments.api.listener

import com.github.arhor.dgs.comments.api.listener.BaseSqsListenerTest.Companion.POST_DELETED_TEST_EVENTS_QUEUE
import com.github.arhor.dgs.comments.api.listener.BaseSqsListenerTest.Companion.POST_UPDATED_TEST_EVENTS_QUEUE
import com.github.arhor.dgs.comments.api.listener.BaseSqsListenerTest.Companion.USER_DELETED_TEST_EVENTS_QUEUE
import com.github.arhor.dgs.comments.api.listener.BaseSqsListenerTest.Companion.USER_UPDATED_TEST_EVENTS_QUEUE
import com.github.arhor.dgs.comments.service.CommentService
import com.ninjasquad.springmockk.MockkBean
import io.awspring.cloud.sqs.operations.SqsOperations
import io.awspring.cloud.test.sqs.SqsTest
import io.mockk.every
import io.mockk.just
import io.mockk.runs
import io.mockk.verify
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.test.annotation.DirtiesContext
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.context.DynamicPropertyRegistry
import org.springframework.test.context.DynamicPropertySource
import org.testcontainers.containers.localstack.LocalStackContainer
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers
import org.testcontainers.utility.DockerImageName

@DirtiesContext
@Testcontainers(disabledWithoutDocker = true)
@ContextConfiguration(classes = [BaseSqsListenerTest.Config::class])
@SqsTest(PostChangeSqsListener::class)
internal class PostChangeSqsListenerTest {

    @Autowired
    private lateinit var sqs: SqsOperations

    @MockkBean
    private lateinit var mockCommentService: CommentService

    @Test
    fun `should call deleteCommentsFromPost method on the CommentService on Post Deleted event`() {
        // Given
        println("----------------------------------------------------------------")
        println("----------------------------------------------------------------")
        println("----------------------------------------------------------------")
        println(sqs)

        val postId = 1L
        val event = PostChangeSqsListener.PostChange.Deleted(id = postId)

        every { mockCommentService.deleteCommentsFromPost(any()) } just runs

        // When
        sqs.send(POST_DELETED_TEST_EVENTS_QUEUE, event)

        // Then
        verify(exactly = 1, timeout = 10_000) { mockCommentService.deleteCommentsFromPost(postId) }

        println("----------------------------------------------------------------")
        println("----------------------------------------------------------------")
        println("----------------------------------------------------------------")
    }

    companion object {
        @JvmStatic
        @Container
        private val localstack =
            LocalStackContainer(DockerImageName.parse("localstack/localstack:1.4.0"))
                .withServices(LocalStackContainer.Service.SQS)

        @JvmStatic
        @DynamicPropertySource
        fun registerDynamicProperties(registry: DynamicPropertyRegistry) {
            with(registry) {
                add("spring.cloud.aws.credentials.access-key") { localstack.accessKey }
                add("spring.cloud.aws.credentials.secret-key") { localstack.secretKey }
                add("spring.cloud.aws.region.static") { localstack.region }
                add("spring.cloud.aws.sqs.endpoint") { localstack.getEndpointOverride(LocalStackContainer.Service.SQS) }
                add("app-props.aws.sqs.user-updates") { USER_UPDATED_TEST_EVENTS_QUEUE }
                add("app-props.aws.sqs.user-deletes") { USER_DELETED_TEST_EVENTS_QUEUE }
                add("app-props.aws.sqs.post-updates") { POST_UPDATED_TEST_EVENTS_QUEUE }
                add("app-props.aws.sqs.post-deletes") { POST_DELETED_TEST_EVENTS_QUEUE }
            }
        }

        @JvmStatic
        @BeforeAll
        fun createTestQueues() {
            with(localstack) {
                execInContainer("awslocal", "sqs", "create-queue", "--queue-name", USER_UPDATED_TEST_EVENTS_QUEUE)
                execInContainer("awslocal", "sqs", "create-queue", "--queue-name", USER_DELETED_TEST_EVENTS_QUEUE)
                execInContainer("awslocal", "sqs", "create-queue", "--queue-name", POST_UPDATED_TEST_EVENTS_QUEUE)
                execInContainer("awslocal", "sqs", "create-queue", "--queue-name", POST_DELETED_TEST_EVENTS_QUEUE)
            }
        }
    }
}
