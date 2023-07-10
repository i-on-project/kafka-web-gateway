package com.isel.ps.gateway.service

import com.isel.ps.gateway.db.SessionRepository
import com.isel.ps.gateway.model.Session
import com.isel.ps.gateway.utils.Result
import org.springframework.stereotype.Service

sealed class SessionDeletionError {
    object SessionNotFound : SessionDeletionError()
}

typealias SessionDeletionResult = Result<SessionDeletionError, Unit>

sealed class SessionModificationError {
    object SessionNotFound : SessionModificationError()
}

typealias SessionModificationResult = Result<SessionModificationError, Unit>

@Service
class SessionService(private val sessionRepository: SessionRepository) {
    fun createSession(session: Session) {
        sessionRepository.create(session)
    }

    fun getSessionByClientId(clientId: String): Session? {
        return sessionRepository.getByClientId(clientId)
    }

    fun deactivateSession(sessionId: String): SessionModificationResult {

        if (sessionRepository.getById(sessionId) == null) {
            Result.Error(
                SessionModificationError.SessionNotFound,
                "Session ID \"${sessionId}\" not found"
            )
        }

        sessionRepository.updateActiveSession(false, sessionId)
        return Result.Success(Unit)
    }

    fun deleteSession(sessionId: String): SessionDeletionResult {

        if (sessionRepository.getById(sessionId) == null) {
            Result.Error(
                SessionDeletionError.SessionNotFound,
                "Session ID \"${sessionId}\" not found"
            )
        }

        sessionRepository.delete(sessionId)
        return Result.Success(Unit)
    }
}