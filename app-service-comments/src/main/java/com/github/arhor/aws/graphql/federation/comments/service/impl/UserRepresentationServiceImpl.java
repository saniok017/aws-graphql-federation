package com.github.arhor.aws.graphql.federation.comments.service.impl;

import com.github.arhor.aws.graphql.federation.comments.data.entity.HasComments.Feature;
import com.github.arhor.aws.graphql.federation.comments.data.entity.UserRepresentation;
import com.github.arhor.aws.graphql.federation.comments.data.repository.UserRepresentationRepository;
import com.github.arhor.aws.graphql.federation.comments.generated.graphql.DgsConstants.USER;
import com.github.arhor.aws.graphql.federation.comments.generated.graphql.types.SwitchUserCommentsInput;
import com.github.arhor.aws.graphql.federation.comments.generated.graphql.types.User;
import com.github.arhor.aws.graphql.federation.comments.service.UserRepresentationService;
import com.github.arhor.aws.graphql.federation.comments.util.Caches;
import com.github.arhor.aws.graphql.federation.common.exception.EntityNotFoundException;
import com.github.arhor.aws.graphql.federation.common.exception.Operation;
import com.github.arhor.aws.graphql.federation.starter.tracing.Trace;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import static com.github.arhor.aws.graphql.federation.comments.util.CacheManagerUtils.getCache;

@Trace
@Service
@RequiredArgsConstructor
public class UserRepresentationServiceImpl implements UserRepresentationService {

    private final CacheManager cacheManager;
    private final UserRepresentationRepository userRepository;

    private Cache cache;

    @PostConstruct
    public void initialize() {
        cache = getCache(cacheManager, Caches.IDEMPOTENT_ID_SET);
    }

    @Override
    public Map<UUID, User> findUsersRepresentationsInBatch(final Set<UUID> userIds) {
        final var result = new HashMap<UUID, User>(userIds.size());
        final var users = userRepository.findAllById(userIds);

        for (final var user : users) {
            result.put(
                user.id(),
                User.newBuilder()
                    .id(user.id())
                    .commentsDisabled(user.features().check(Feature.COMMENTS_DISABLED))
                    .build()
            );
        }
        userIds.stream().filter(it -> !result.containsKey(it)).forEach((userId) ->
            result.put(
                userId,
                User.newBuilder()
                    .id(userId)
                    .build()
            )
        );
        return result;
    }

    @Override
    public void createUserRepresentation(final UUID userId, final UUID idempotencyKey) {
        cache.get(idempotencyKey, () ->
            userRepository.save(
                UserRepresentation.builder()
                    .id(userId)
                    .shouldBePersisted(true)
                    .build()
            )
        );
    }

    @Override
    public void deleteUserRepresentation(final UUID userId, final UUID idempotencyKey) {
        cache.get(idempotencyKey, () -> {
            userRepository.deleteById(userId);
            return null;
        });
    }

    @Override
    public boolean switchUserComments(final SwitchUserCommentsInput input) {
        final var userId = input.getUserId();
        final var shouldBeDisabled = input.getDisabled();

        final var user =
            userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException(
                        USER.TYPE_NAME,
                        USER.Id + " = " + userId,
                        Operation.UPDATE
                    )
                );

        if (user.features().check(Feature.COMMENTS_DISABLED) != shouldBeDisabled) {
            userRepository.save(
                user.toBuilder()
                    .features(
                        shouldBeDisabled
                            ? user.features().plus(Feature.COMMENTS_DISABLED)
                            : user.features().minus(Feature.COMMENTS_DISABLED)
                    )
                    .build()
            );
            return true;
        } else {
            return false;
        }
    }
}
