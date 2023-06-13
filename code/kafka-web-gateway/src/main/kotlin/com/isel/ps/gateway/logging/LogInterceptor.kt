package com.isel.ps.gateway.logging

import org.springframework.http.HttpMethod
import org.springframework.stereotype.Component
import org.springframework.web.servlet.HandlerInterceptor
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

@Component
class LogInterceptor(val logService: LoggingService) : HandlerInterceptor {

    override fun preHandle(request: HttpServletRequest, response: HttpServletResponse, handler: Any): Boolean {
        if (request.method == HttpMethod.GET.name || request.method == HttpMethod.DELETE.name || request.method == HttpMethod.PUT.name) {
            logService.displayReq(request, null)
        }
        return true
    }
}
