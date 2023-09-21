package com.github.arhor.dgs.users.data.converter

import com.github.arhor.dgs.users.test.TestObjectMapperConfig
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig

@SpringJUnitConfig(
    classes = [
        JsonWritingConverter::class,
        JsonReadingConverter::class,
        TestObjectMapperConfig::class,
    ]
)
class JsonConverterTest {

    @Autowired
    private lateinit var writingConverter: JsonWritingConverter

    @Autowired
    private lateinit var readingConverter: JsonReadingConverter

    @Test
    fun `should return the result equal to initial object using writing then reading converter`() {
        // given
        val source = buildMap {
            put("id", 1)
            put("name", "test-date")
            put("tags", listOf("tag-1", "tag-2", "tag-1"))
        }

        // when
        val result = source
            .let(writingConverter::convert)
            .let(readingConverter::convert)

        // then
        assertThat(result)
            .isNotNull
            .isEqualTo(source)
    }
}
