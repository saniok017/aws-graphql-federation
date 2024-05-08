package com.github.arhor.aws.graphql.federation.posts.api.graphql.datafetcher;

import com.github.arhor.aws.graphql.federation.posts.generated.graphql.DgsConstants.USER
import com.github.arhor.aws.graphql.federation.posts.generated.graphql.types.User
import com.netflix.graphql.dgs.DgsQueryExecutor
import com.netflix.graphql.dgs.autoconfig.DgsAutoConfiguration
import com.netflix.graphql.dgs.autoconfig.DgsExtendedScalarsAutoConfiguration
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.from
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import java.util.UUID

@SpringBootTest(
    classes = [
        DgsAutoConfiguration::class,
        DgsExtendedScalarsAutoConfiguration::class,
        FederatedEntityFetcher::class,
    ]
)
class FederatedEntityFetcherTest {

    @Autowired
    private lateinit var dgsQueryExecutor: DgsQueryExecutor

    @Test
    fun `should create new user representation for the given id`() {
        // Given
        val userId = UUID.randomUUID()

        // When
        val result = dgsQueryExecutor.executeAndExtractJsonPathAsObject(
            """
                query (${'$'}representations: [_Any!]!) {
                    _entities(representations: ${'$'}representations) {
                        ... on User {
                            id
                        }
                    }
                }""".trimIndent(),
            "$.data._entities[0]",
            mapOf("representations" to listOf(mapOf("__typename" to USER.TYPE_NAME, USER.Id to userId))),
            User::class.java
        )

        // Then
        assertThat(result)
            .returns(userId, from(User::id));
    }
}
