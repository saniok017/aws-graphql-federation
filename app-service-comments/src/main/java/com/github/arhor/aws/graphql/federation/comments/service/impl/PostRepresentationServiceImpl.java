package com.github.arhor.aws.graphql.federation.comments.service.impl;

import com.github.arhor.aws.graphql.federation.comments.data.entity.PostRepresentationEntity;
import com.github.arhor.aws.graphql.federation.comments.data.repository.PostRepresentationRepository;
import com.github.arhor.aws.graphql.federation.comments.generated.graphql.DgsConstants.POST;
import com.github.arhor.aws.graphql.federation.comments.generated.graphql.types.Post;
import com.github.arhor.aws.graphql.federation.comments.service.PostRepresentationService;
import com.github.arhor.aws.graphql.federation.comments.util.Caches;
import com.github.arhor.aws.graphql.federation.common.exception.EntityNotFoundException;
import com.github.arhor.aws.graphql.federation.common.exception.Operation;
import com.github.arhor.aws.graphql.federation.tracing.Trace;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.stereotype.Service;

import java.util.UUID;

import static com.github.arhor.aws.graphql.federation.comments.util.CacheManagerUtils.getCache;

@Trace
@Service
@RequiredArgsConstructor
public class PostRepresentationServiceImpl implements PostRepresentationService {

    private final CacheManager cacheManager;
    private final PostRepresentationRepository postRepresentationRepository;

    private Cache cache;

    @PostConstruct
    public void initialize() {
        cache = getCache(cacheManager, Caches.IDEMPOTENT_ID_SET);
    }

    @Override
    public Post findPostRepresentation(final UUID postId) {
        return postRepresentationRepository.findById(postId)
            .map(this::mapEntityToPost)
            .orElseThrow(() -> new EntityNotFoundException(
                POST.TYPE_NAME,
                POST.Id + " = " + postId,
                Operation.LOOKUP
            ));
    }

    @Override
    public void createPostRepresentation(final UUID postId, final UUID idempotencyKey) {
        cache.get(idempotencyKey, () ->
            postRepresentationRepository.save(
                new PostRepresentationEntity(postId)
            )
        );
    }

    @Override
    public void deletePostRepresentation(final UUID postId, final UUID idempotencyKey) {
        cache.get(idempotencyKey, () -> {
            postRepresentationRepository.deleteById(postId);
            return null;
        });
    }

    private Post mapEntityToPost(final PostRepresentationEntity entity) {
        return Post.newBuilder()
            .id(entity.id())
            .build();
    }
}