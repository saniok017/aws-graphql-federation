package com.github.arhor.aws.graphql.federation.posts.service.mapping

import com.github.arhor.aws.graphql.federation.posts.data.entity.PostEntity
import com.github.arhor.aws.graphql.federation.posts.data.entity.TagEntity
import com.github.arhor.aws.graphql.federation.posts.data.entity.projection.PostProjection
import com.github.arhor.aws.graphql.federation.posts.generated.graphql.types.CreatePostInput
import com.github.arhor.aws.graphql.federation.posts.generated.graphql.types.Post
import com.github.arhor.aws.graphql.federation.posts.generated.graphql.types.PostPage
import org.springframework.data.domain.Page

interface PostMapper {
    fun mapToEntity(input: CreatePostInput, tags: Set<TagEntity>): PostEntity
    fun mapToPost(entity: PostEntity): Post
    fun mapToPost(projection: PostProjection): Post
    fun mapToPostPageFromEntity(page: Page<PostEntity>): PostPage
    fun mapToPostPageFromProjection(page: Page<PostProjection>): PostPage
}
