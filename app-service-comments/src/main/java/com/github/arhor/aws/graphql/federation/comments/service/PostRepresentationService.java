package com.github.arhor.aws.graphql.federation.comments.service;

import com.github.arhor.aws.graphql.federation.comments.generated.graphql.types.Post;
import com.github.arhor.aws.graphql.federation.comments.generated.graphql.types.SwitchPostCommentsInput;

import java.util.UUID;

/**
 * Service interface for handling post representations.
 */
public interface PostRepresentationService {

    /**
     * Finds the post representation by the specified post ID.
     *
     * @param postId the UUID of the post to find the representation for
     * @return the post representation
     */
    Post findPostRepresentation(UUID postId);

    /**
     * Creates a new post representation.
     *
     * @param postId         the UUID of the post for whom the representation is to be created
     * @param idempotencyKey the UUID used to ensure idempotency of the creation operation
     */
    void createPostRepresentation(UUID postId, UUID idempotencyKey);

    /**
     * Deletes an existing post representation.
     *
     * @param postId         the UUID of the post whose representation is to be deleted
     * @param idempotencyKey the UUID used to ensure idempotency of the deletion operation
     */
    void deletePostRepresentation(UUID postId, UUID idempotencyKey);

    /**
     * Switches the comments for the post based on the provided input.
     *
     * @param input the input object containing the necessary data to switch comments
     * @return {@code true} if the switch was successful, {@code false} otherwise
     */
    boolean switchComments(SwitchPostCommentsInput input);
}
