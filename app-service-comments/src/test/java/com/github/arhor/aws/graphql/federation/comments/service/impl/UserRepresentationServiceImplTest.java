package com.github.arhor.aws.graphql.federation.comments.service.impl;

import com.github.arhor.aws.graphql.federation.comments.data.entity.UserRepresentation;
import com.github.arhor.aws.graphql.federation.comments.data.repository.UserRepresentationRepository;
import com.github.arhor.aws.graphql.federation.comments.generated.graphql.types.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.cache.concurrent.ConcurrentMapCache;

import java.util.Optional;
import java.util.UUID;

import static com.github.arhor.aws.graphql.federation.comments.util.Caches.IDEMPOTENT_ID_SET;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class UserRepresentationServiceImplTest {

    private final Cache cache = new ConcurrentMapCache(IDEMPOTENT_ID_SET.name());
    private final CacheManager cacheManager = mock();
    private final UserRepresentationRepository userRepresentationRepository = mock();

    private UserRepresentationServiceImpl userService;

    @BeforeEach
    void setUp() {
        when(cacheManager.getCache(IDEMPOTENT_ID_SET.name()))
            .thenReturn(cache);

        userService = new UserRepresentationServiceImpl(cacheManager, userRepresentationRepository);
        userService.initialize();
    }


    @Nested
    @DisplayName("UserService :: findUserRepresentation")
    class FindUserRepresentationTest {
        @Test
        void should_return_expected_user_when_it_exists_by_id() {
            // Given
            final var userRepresentation =
                UserRepresentation.builder()
                    .id(UUID.randomUUID())
                    .commentsDisabled(false)
                    .build();

            final var expectedUser =
                User.newBuilder()
                    .id(userRepresentation.id())
                    .commentsDisabled(userRepresentation.commentsDisabled())
                    .commentsOperable(true)
                    .build();

            when(userRepresentationRepository.findById(any()))
                .thenReturn(Optional.of(userRepresentation));

            // When
            final var result = userService.findUserRepresentation(userRepresentation.id());

            // Then
            then(userRepresentationRepository)
                .should()
                .findById(userRepresentation.id());

            then(userRepresentationRepository)
                .shouldHaveNoMoreInteractions();

            assertThat(result)
                .isNotNull()
                .isEqualTo(expectedUser);
        }

        @Test
        void should_return_user_with_commentsOperable_false_when_user_does_not_exist_by_id() {
            // Given
            final var expectedUser =
                User.newBuilder()
                    .id(UUID.randomUUID())
                    .commentsOperable(false)
                    .build();

            when(userRepresentationRepository.findById(any()))
                .thenReturn(Optional.empty());

            // When
            final var result = userService.findUserRepresentation(expectedUser.getId());

            // Then
            then(userRepresentationRepository)
                .should()
                .findById(expectedUser.getId());

            then(userRepresentationRepository)
                .shouldHaveNoMoreInteractions();

            assertThat(result)
                .isNotNull()
                .isEqualTo(expectedUser);
        }
    }

    @Nested
    @DisplayName("UserService :: createUserRepresentation")
    class CreateUserRepresentationTest {
        @Test
        void should_call_userRepository_save_only_once_with_the_same_idempotencyKey() {
            // Given
            final var idempotencyKey = UUID.randomUUID();
            final var expectedUserRepresentation =
                UserRepresentation.builder()
                    .id(UUID.randomUUID())
                    .commentsDisabled(false)
                    .shouldBePersisted(true)
                    .build();

            // When
            for (int i = 0; i < 3; i++) {
                userService.createUserRepresentation(expectedUserRepresentation.id(), idempotencyKey);
            }

            // Then
            then(userRepresentationRepository)
                .should()
                .save(expectedUserRepresentation);

            then(userRepresentationRepository)
                .shouldHaveNoMoreInteractions();
        }
    }

    @Nested
    @DisplayName("UserService :: deleteUserRepresentation")
    class DeleteUserRepresentationTest {
        @Test
        void should_call_userRepository_deleteById_only_once_with_the_same_idempotencyKey() {
            // Given
            final var idempotencyKey = UUID.randomUUID();
            final var userId = UUID.randomUUID();

            // When
            for (int i = 0; i < 3; i++) {
                userService.deleteUserRepresentation(userId, idempotencyKey);
            }

            // Then
            then(userRepresentationRepository)
                .should()
                .deleteById(userId);

            then(userRepresentationRepository)
                .shouldHaveNoMoreInteractions();
        }
    }
}
