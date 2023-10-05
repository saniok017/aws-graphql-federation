package com.github.arhor.dgs.comments.service

import com.github.arhor.dgs.comments.data.repository.CommentRepository
import com.github.arhor.dgs.comments.service.mapper.CommentMapper
import com.ninjasquad.springmockk.MockkBean
import io.mockk.clearAllMocks
import io.mockk.confirmVerified
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.ComponentScan
import org.springframework.context.annotation.ComponentScan.Filter
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.FilterType.ASSIGNABLE_TYPE
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig

@SpringJUnitConfig
internal class CommentServiceTest {

    @MockkBean
    private lateinit var commentRepository: CommentRepository

    @MockkBean
    private lateinit var commentMapper: CommentMapper

    @Autowired
    private lateinit var commentService: CommentService

    @Test
    fun `getCommentsByUserIds should not interact with repository if userIds argument is empty list`() {
        // Given
        val userIds = emptyList<Long>()

        // When
        val result = commentService.getCommentsByUserIds(userIds)

        // Then
        assertThat(result)
            .isNotNull()
            .isEmpty()

        confirmVerified(commentRepository, commentMapper)
    }

    @AfterEach
    fun tearDown() {
        clearAllMocks()
    }

    @Configuration
    @ComponentScan(
        includeFilters = [Filter(type = ASSIGNABLE_TYPE, classes = [CommentService::class])],
        useDefaultFilters = false,
    )
    class Config
}