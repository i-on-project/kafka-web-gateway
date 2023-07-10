package com.isel.ps.gateway.service

import com.isel.ps.gateway.db.ClientRepository
import com.isel.ps.gateway.model.Client
import com.isel.ps.gateway.utils.Result
import org.springframework.stereotype.Service

sealed class ClientCreationError {
    object ClientAlreadyExists : ClientCreationError()
}

typealias ClientCreationResult = Result<ClientCreationError, Unit>

@Service
class ClientService(
    private val clientRepository: ClientRepository,
) {
    fun createClient(
        client: Client
    ): ClientCreationResult {

        if (!clientRepository.exists(client.clientId)) {
            Result.Error(
                ClientCreationError.ClientAlreadyExists,
                "Client ID \"${client.clientId}\" already exists."
            )
        }

        clientRepository.create(client)

        return Result.Success(Unit)
    }

    fun createClientIfNotExists(
        client: Client
    ): ClientCreationResult {

        if (!clientRepository.exists(client.clientId)) {
            clientRepository.create(client)
        }

        return Result.Success(Unit)
    }
}