package com.github.arhor.aws.graphql.federation.comments.service.impl;

import com.github.arhor.aws.graphql.federation.comments.data.entity.PostRepresentation;
import com.github.arhor.aws.graphql.federation.comments.data.repository.PostRepresentationRepository;
import com.github.arhor.aws.graphql.federation.comments.generated.graphql.DgsConstants.POST;
import com.github.arhor.aws.graphql.federation.comments.generated.graphql.types.Post;
import com.github.arhor.aws.graphql.federation.comments.generated.graphql.types.SwitchPostCommentsInput;
import com.github.arhor.aws.graphql.federation.common.exception.EntityConditionException;
import com.github.arhor.aws.graphql.federation.common.exception.EntityNotFoundException;
import com.github.arhor.aws.graphql.federation.common.exception.Operation;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.cache.concurrent.ConcurrentMapCache;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static com.github.arhor.aws.graphql.federation.comments.util.Caches.IDEMPOTENT_ID_SET;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchException;
import static org.assertj.core.api.Assertions.from;
import static org.assertj.core.api.InstanceOfAssertFactories.type;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class PostRepresentationServiceImplTest {

    private static final UUID POST_ID = UUID.randomUUID();
    private static final UUID IDEMPOTENCY_KEY = UUID.randomUUID();

    private final Cache cache = new ConcurrentMapCache(IDEMPOTENT_ID_SET.name());
    private final CacheManager cacheManager = mock();
    private final PostRepresentationRepository postRepository = mock();

    private PostRepresentationServiceImpl postService;

    @BeforeEach
    void setUp() {
        when(cacheManager.getCache(IDEMPOTENT_ID_SET.name()))
            .thenReturn(cache);

        postService = new PostRepresentationServiceImpl(cacheManager, postRepository);
        postService.initialize();
    }

    @Nested
    @DisplayName("PostService :: findPostsRepresentationsInBatch")
    class FindPostRepresentationTest {
        @Test
        void should_return_expected_post_when_it_exists_by_id() {
            // Given
            final var postRepresentation =
                PostRepresentation.builder()
                    .id(POST_ID)
                    .commentsDisabled(false)
                    .build();

            final var expectedResult = Map.of(
                POST_ID,
                Post.newBuilder()
                    .id(postRepresentation.id())
                    .commentsDisabled(postRepresentation.commentsDisabled())
                    .build()
            );
            final var expectedPostIds = Set.of(POST_ID);

            when(postRepository.findAllById(any()))
                .thenReturn(List.of(postRepresentation));

            // When
            final var result = postService.findPostsRepresentationsInBatch(expectedPostIds);

            // Then
            then(postRepository)
                .should()
                .findAllById(expectedPostIds);

            then(postRepository)
                .shouldHaveNoMoreInteractions();

            assertThat(result)
                .isNotNull()
                .isEqualTo(expectedResult);
        }

        @Test
        void should_return_post_with_commentsOperable_false_when_post_does_not_exist_by_id() {
            // Given
            final var expectedResult = Map.of(
                POST_ID,
                Post.newBuilder()
                    .id(POST_ID)
                    .build()
            );
            final var expectedPostIds = Set.of(POST_ID);

            when(postRepository.findAllById(any()))
                .thenReturn(Collections.emptyList());

            // When
            final var result = postService.findPostsRepresentationsInBatch(expectedPostIds);

            // Then
            then(postRepository)
                .should()
                .findAllById(expectedPostIds);

            then(postRepository)
                .shouldHaveNoMoreInteractions();

            assertThat(result)
                .isNotNull()
                .isEqualTo(expectedResult);
        }
    }

    @Nested
    @DisplayName("PostService :: createPostRepresentation")
    class CreatePostRepresentationTest {
        @Test
        void should_call_postRepository_save_only_once_with_the_same_idempotencyKey() {
            // Given
            final var expectedPostRepresentation =
                PostRepresentation.builder()
                    .id(POST_ID)
                    .commentsDisabled(false)
                    .shouldBePersisted(true)
                    .build();

            // When
            for (int i = 0; i < 3; i++) {
                postService.createPostRepresentation(expectedPostRepresentation.id(), IDEMPOTENCY_KEY);
            }

            // Then
            then(postRepository)
                .should()
                .save(expectedPostRepresentation);

            then(postRepository)
                .shouldHaveNoMoreInteractions();
        }
    }

    @Nested
    @DisplayName("PostService :: deletePostRepresentation")
    class DeletePostRepresentationTest {
        @Test
        void should_call_postRepository_deleteById_only_once_with_the_same_idempotencyKey() {
            // Given
            final var numberOfCalls = 3;

            // When
            for (int i = 0; i < numberOfCalls; i++) {
                postService.deletePostRepresentation(POST_ID, IDEMPOTENCY_KEY);
            }

            // Then
            then(postRepository)
                .should()
                .deleteById(POST_ID);

            then(postRepository)
                .shouldHaveNoMoreInteractions();
        }
    }

    @Nested
    @DisplayName("PostService :: switchPostComments")
    class SwitchPostCommentsTest {
        @Test
        void should_call_PostRepresentationRepository_save_when_there_is_update_applied_to_the_post() {
            // Given
            final var input =
                SwitchPostCommentsInput.newBuilder()
                    .postId(POST_ID)
                    .disabled(true)
                    .build();

            final var post =
                PostRepresentation.builder()
                    .id(POST_ID)
                    .commentsDisabled(false)
                    .build();

            given(postRepository.findById(any()))
                .willReturn(Optional.of(post));

            given(postRepository.save(any()))
                .willAnswer((call) -> call.getArgument(0));

            // When
            final var result = postService.switchPostComments(input);

            // Then
            then(postRepository)
                .should()
                .findById(POST_ID);

            then(postRepository)
                .should()
                .save(post.toBuilder().commentsDisabled(input.getDisabled()).build());

            assertThat(result)
                .isTrue();
        }

        @Test
        void should_not_call_PostRepresentationRepository_save_when_there_is_no_update_applied_to_the_post() {
            // Given
            final var input =
                SwitchPostCommentsInput.newBuilder()
                    .postId(POST_ID)
                    .disabled(true)
                    .build();

            final var post =
                PostRepresentation.builder()
                    .id(POST_ID)
                    .commentsDisabled(true)
                    .build();

            given(postRepository.findById(any()))
                .willReturn(Optional.of(post));

            // When
            final var result = postService.switchPostComments(input);

            // Then
            then(postRepository)
                .should()
                .findById(POST_ID);

            assertThat(result)
                .isFalse();
        }

        @Test
        void should_throw_EntityNotFoundException_when_there_is_no_post_found_by_the_input_id() {
            // Given
            final var input =
                SwitchPostCommentsInput.newBuilder()
                    .postId(POST_ID)
                    .disabled(true)
                    .build();

            given(postRepository.findById(any()))
                .willReturn(Optional.empty());

            // When
            final var result = catchException(() -> postService.switchPostComments(input));

            // Then
            then(postRepository)
                .should()
                .findById(POST_ID);

            assertThat(result)
                .asInstanceOf(type(EntityNotFoundException.class))
                .returns(POST.TYPE_NAME, from(EntityConditionException::getEntity))
                .returns(POST.Id + " = " + POST_ID, from(EntityConditionException::getCondition))
                .returns(Operation.UPDATE, from(EntityConditionException::getOperation));
        }
    }
}
