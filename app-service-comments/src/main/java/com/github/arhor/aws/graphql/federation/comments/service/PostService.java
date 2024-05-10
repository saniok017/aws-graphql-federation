package com.github.arhor.aws.graphql.federation.comments.service;

import com.github.arhor.aws.graphql.federation.comments.generated.graphql.types.Post;

import java.util.UUID;

public interface PostService {

    Post findInternalPostRepresentation(UUID postId);

    void createInternalPostRepresentation(UUID postIds);

    void deleteInternalPostRepresentation(UUID postIds);
}
