package com.isel.ps.gateway.utils


sealed class AddSubscriptionError {
    object Unknown : AddSubscriptionError()
}

sealed class AddSubscriptionSuccess {
    object Added : AddSubscriptionSuccess()
    object AlreadyExists : AddSubscriptionSuccess()
}
typealias AddSubscriptionResult = Result<AddSubscriptionError, AddSubscriptionSuccess>