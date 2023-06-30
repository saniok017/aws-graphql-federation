@file:Suppress("ClassName", "SameParameterValue")

package com.github.arhor.dgs.users.service

import com.github.arhor.dgs.lib.exception.EntityDuplicateException
import com.github.arhor.dgs.lib.exception.Operation
import com.github.arhor.dgs.users.data.entity.UserEntity
import com.github.arhor.dgs.users.data.repository.UserRepository
import com.github.arhor.dgs.users.generated.graphql.DgsConstants.USER
import com.github.arhor.dgs.users.generated.graphql.types.CreateUserInput
import com.github.arhor.dgs.users.generated.graphql.types.UpdateUserInput
import com.github.arhor.dgs.users.generated.graphql.types.User
import com.github.arhor.dgs.users.service.impl.UserServiceImpl
import io.mockk.Call
import io.mockk.MockKAnswerScope
import io.mockk.confirmVerified
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.catchException
import org.assertj.core.api.Assertions.from
import org.assertj.core.api.InstanceOfAssertFactories.throwable
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.Arguments.arguments
import org.junit.jupiter.params.provider.MethodSource
import org.springframework.data.repository.findByIdOrNull
import org.springframework.security.crypto.password.PasswordEncoder
import java.util.stream.Stream

internal class UserServiceTest {

    private val mockUserMapper: UserMapper = mockk()
    private val mockUserRepository: UserRepository = mockk()
    private val mockPasswordEncoder: PasswordEncoder = mockk()

    private val userService: UserService = UserServiceImpl(
        mockUserMapper,
        mockUserRepository,
        mockPasswordEncoder,
    )

    @Nested
    inner class `UserService # createUser` {
        @Test
        fun `should correctly create new user entity and return DTO with assigned id`() {
            // Given
            val expectedId = 1L
            val expectedUsername = "test@email.com"
            val expectedPassword = "TestPassword123"

            val input = CreateUserInput(
                username = expectedUsername,
                password = expectedPassword,
            )

            every { mockUserRepository.existsByUsername(any()) } returns false
            every { mockPasswordEncoder.encode(any()) } answers { firstArg() }
            every { mockUserMapper.mapToEntity(any()) } answers convertingDtoToUser
            every { mockUserRepository.save(any()) } answers copyingUserWithAssignedId(id = expectedId)
            every { mockUserMapper.mapToDTO(any()) } answers convertingUserToDto

            // When
            val result = userService.createUser(input)

            // Then
            assertThat(result)
                .returns(expectedId, from { it.id })
                .returns(expectedUsername, from { it.username })

            verify(exactly = 1) { mockUserRepository.existsByUsername(any()) }
            verify(exactly = 1) { mockUserMapper.mapToEntity(any()) }
            verify(exactly = 1) { mockUserRepository.save(any()) }
            verify(exactly = 1) { mockUserMapper.mapToDTO(any()) }
        }

        @Test
        fun `should throw EntityDuplicateException creating user with already taken username`() {
            // Given
            val input = CreateUserInput(
                username = "test-username",
                password = "test-password",
            )

            val expectedEntity = USER.TYPE_NAME
            val expectedOperation = Operation.CREATE
            val expectedCondition = "${USER.Username} = ${input.username}"
            val expectedExceptionType = EntityDuplicateException::class.java

            val username = slot<String>()

            every { mockUserRepository.existsByUsername(capture(username)) } returns true

            // When
            val result = catchException { userService.createUser(input) }

            // Then
            assertThat(username)
                .returns(true, from { it.isCaptured })
                .returns(input.username, from { it.captured })

            assertThat(result)
                .asInstanceOf(throwable(expectedExceptionType))
                .satisfies(
                    { assertThat(it.entity).describedAs("entity").isEqualTo(expectedEntity) },
                    { assertThat(it.operation).describedAs("operation").isEqualTo(expectedOperation) },
                    { assertThat(it.condition).describedAs("condition").isEqualTo(expectedCondition) },
                )
        }
    }

    @Nested
    inner class `UserService # updateUser` {

        @Test
        fun `should save updated user state to repository when there are actual changes`() {
            // Given
            val user = UserEntity(
                id = 1,
                username = "test-username",
                password = "test-password",
            )

            every { mockUserRepository.findByIdOrNull(any()) } returns user
            every { mockUserRepository.save(any()) } answers { firstArg() }
            every { mockUserMapper.mapToDTO(any()) } answers convertingUserToDto
            every { mockPasswordEncoder.encode(any()) } answers { firstArg() }

            // When
            userService.updateUser(
                input = UpdateUserInput(
                    id = user.id!!,
                    password = "${user.password}-updated",
                )
            )

            // Then
            verify(exactly = 1) { mockUserRepository.save(any()) }
        }

        @Test
        fun `should not call save method on repository when there are no changes in user state`() {
            // Given
            val user = UserEntity(
                id = 1,
                username = "test-username",
                password = "test-password",
            )

            every { mockUserRepository.findByIdOrNull(any()) } returns user
            every { mockUserRepository.save(any()) } answers { firstArg() }
            every { mockUserMapper.mapToDTO(any()) } answers convertingUserToDto
            every { mockPasswordEncoder.encode(any()) } answers { firstArg() }

            // When
            userService.updateUser(
                input = UpdateUserInput(
                    id = user.id!!,
                    password = user.password,
                )
            )

            // Then
            verify(exactly = 0) { mockUserRepository.save(any()) }
        }
    }

    @Nested
    inner class `UserService # deleteUser` {

        @MethodSource("com.github.arhor.dgs.users.service.UserServiceTest#delete user positive test data factory")
        @ParameterizedTest
        fun `should return expected result deleting user`(
            // Given
            affectedRowNum: Int,
            expectedResult: Boolean,
        ) {
            val expectedId = 1L

            every { mockUserRepository.deleteUserById(any()) } returns affectedRowNum

            // When
            val result = userService.deleteUser(expectedId)

            // Then
            assertThat(result)
                .isEqualTo(expectedResult)

            verify(exactly = 1) { mockUserRepository.deleteUserById(expectedId) }

            confirmVerified(mockUserMapper, mockUserRepository, mockPasswordEncoder)
        }

        @Test
        fun `should throw EntityDuplicateException trying to delete more then one user by id`() {
            // Given
            val id = 1L
            val numberOfDeletedUsers = 2
            val expectedExceptionType = IllegalStateException::class.java

            every { mockUserRepository.deleteUserById(any()) } returns numberOfDeletedUsers

            // When
            val result = catchException { userService.deleteUser(id) }

            // Then
            assertThat(result)
                .isInstanceOf(expectedExceptionType)
        }
    }

    private val convertingDtoToUser: MockKAnswerScope<UserEntity, *>.(Call) -> UserEntity
        get() = {
            firstArg<CreateUserInput>().let {
                UserEntity(
                    username = it.username,
                    password = it.password,
                )
            }
        }

    private val convertingUserToDto: MockKAnswerScope<User, *>.(Call) -> User
        get() = {
            firstArg<UserEntity>().let {
                User(
                    id = it.id!!,
                    username = it.username,
                )
            }
        }

    private fun copyingUserWithAssignedId(id: Long): MockKAnswerScope<UserEntity, *>.(Call) -> UserEntity = {
        firstArg<UserEntity>().copy(id = id)
    }

    companion object {
        @JvmStatic
        fun `delete user positive test data factory`(): Stream<Arguments> =
            Stream.of(
                arguments(1, true),
                arguments(0, false),
            )
    }
}
