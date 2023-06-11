package com.isel.ps.gateway.logging

import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.http.HttpMethod
import org.springframework.stereotype.Component
import org.springframework.web.servlet.HandlerInterceptor

@Component
class LogInterceptor(val logService: LoggingService) : HandlerInterceptor {

    override fun preHandle(request: HttpServletRequest, response: HttpServletResponse, handler: Any): Boolean {
        if (request.method == HttpMethod.GET.name() || request.method == HttpMethod.DELETE.name() || request.method == HttpMethod.PUT.name()) {
            logService.displayReq(request, null)
        }
        return true
    }
}
