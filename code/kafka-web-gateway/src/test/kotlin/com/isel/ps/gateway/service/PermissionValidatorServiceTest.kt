package com.isel.ps.gateway.service

import com.isel.ps.gateway.db.ClientPermissionRepository
import com.isel.ps.gateway.db.ClientRoleRepository
import com.isel.ps.gateway.db.RolePermissionRepository
import com.isel.ps.gateway.model.ClientRole
import com.isel.ps.gateway.model.TopicType
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mock
import org.mockito.Mockito.`when`
import org.mockito.MockitoAnnotations
import org.mockito.junit.jupiter.MockitoExtension

@ExtendWith(MockitoExtension::class)
class PermissionValidatorServiceTest {

    @Mock
    private lateinit var clientPermissionRepository: ClientPermissionRepository

    @Mock
    private lateinit var rolePermissionRepository: RolePermissionRepository

    @Mock
    private lateinit var clientRoleRepository: ClientRoleRepository

    private lateinit var permissionValidatorService: PermissionValidatorService

    @BeforeEach
    fun setup() {
        MockitoAnnotations.openMocks(this)
        permissionValidatorService = PermissionValidatorService(
            clientPermissionRepository,
            rolePermissionRepository,
            clientRoleRepository
        )
    }

    @Test
    fun testHasPermission_WithDirectPermission_ReturnsTrue() {
        // Arrange
        val topic = "example-topic"
        val key = "example-key"
        val clientId = "example-client-id"
        val readPermission = true
        `when`(clientPermissionRepository.hasPermission(clientId, topic, key, readPermission)).thenReturn(true)

        // Act
        val hasPermission = permissionValidatorService.hasPermission(topic, key, clientId, readPermission)

        // Assert
        assertTrue(hasPermission)
    }

    @Test
    fun testHasPermission_WithRolePermission_ReturnsTrue() {
        // Arrange
        val topic = "example-topic"
        val key = "example-key"
        val clientId = "example-client-id"
        val readPermission = true
        val roleId = 1
        val clientRole = ClientRole(clientId, roleId)

        `when`(clientPermissionRepository.hasPermission(clientId, topic, key, readPermission)).thenReturn(false)
        `when`(clientRoleRepository.getAll()).thenReturn(listOf(clientRole))
        `when`(rolePermissionRepository.hasPermission(roleId, topic, key, readPermission)).thenReturn(true)

        // Act
        val hasPermission = permissionValidatorService.hasPermission(topic, key, clientId, readPermission)

        // Assert
        assertTrue(hasPermission)
    }

    @Test
    fun testHasPermission_NoPermission_ReturnsFalse() {
        // Arrange
        val topic = "example-topic"
        val key = "example-key"
        val clientId = "example-client-id"
        val readPermission = true

        `when`(clientPermissionRepository.hasPermission(clientId, topic, key, readPermission)).thenReturn(false)
        `when`(clientRoleRepository.getAll()).thenReturn(emptyList())

        // Act
        val hasPermission = permissionValidatorService.hasPermission(topic, key, clientId, readPermission)

        // Assert
        assertFalse(hasPermission)
    }

    @Test
    fun testHasPermissionForAll_AllPermissionsExist_ReturnsTrue() {
        // Arrange
        val topics = listOf(
            TopicType("topic1", "key1"),
            TopicType("topic2", "key2")
        )
        val clientId = "example-client-id"
        val readPermission = true

        `when`(
            clientPermissionRepository.hasPermission(
                clientId,
                topics[0].topic,
                topics[0].key,
                readPermission
            )
        ).thenReturn(true)
        `when`(
            clientPermissionRepository.hasPermission(
                clientId,
                topics[1].topic,
                topics[1].key,
                readPermission
            )
        ).thenReturn(true)

        // Act
        val hasPermissionForAll = permissionValidatorService.hasPermissionForAll(topics, clientId, readPermission)

        // Assert
        assertTrue(hasPermissionForAll)
    }

    @Test
    fun testHasPermissionForAll_NotAllPermissionsExist_ReturnsFalse() {
        // Arrange
        val topics = listOf(
            TopicType("topic1", "key1"),
            TopicType("topic2", "key2")
        )
        val clientId = "example-client-id"
        val readPermission = true

        `when`(
            clientPermissionRepository.hasPermission(
                clientId,
                topics[0].topic,
                topics[0].key,
                readPermission
            )
        ).thenReturn(true)
        `when`(
            clientPermissionRepository.hasPermission(
                clientId,
                topics[1].topic,
                topics[1].key,
                readPermission
            )
        ).thenReturn(false)

        // Act
        val hasPermissionForAll = permissionValidatorService.hasPermissionForAll(topics, clientId, readPermission)

        // Assert
        assertFalse(hasPermissionForAll)
    }
}