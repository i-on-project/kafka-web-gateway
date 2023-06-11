package com.isel.ps.gateway.logging

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import java.util.*
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

@Service
class LoggingService {
    var logger: Logger = LoggerFactory.getLogger(LoggingService::class.java)

    fun displayReq(request: HttpServletRequest, body: Any?) {
        val reqMessage = StringBuilder()
        val parameters = getParameters(request)
        reqMessage.append("method = [").append(request.method).append("]")
        reqMessage.append(" path = [").append(request.requestURI).append("] ")
        logger.info("REQUEST: {}", reqMessage)
        if (parameters.isNotEmpty()) {
            logger.info("Parameters = [$parameters] ")
        }
        if (!Objects.isNull(body)) {
            logger.info("Body = [$body] ")
        }
    }

    fun displayResp(request: HttpServletRequest, response: HttpServletResponse, body: Any?) {
        val respMessage = StringBuilder()
        val headers = getHeaders(response)
        respMessage.append(" status = [").append(response.status).append("]")
        logger.info("RESPONSE: {}", respMessage)
        if (headers.isNotEmpty()) {
            logger.info("ResponseHeaders = [$headers] ")
        }
        logger.info("ResponseBody = [$body] ")
    }

    private fun getHeaders(response: HttpServletResponse): Map<String, String> {
        val headers: MutableMap<String, String> = HashMap()
        val headerMap = response.headerNames
        for (str in headerMap) {
            headers[str] = response.getHeader(str)
        }
        return headers
    }

    private fun getParameters(request: HttpServletRequest): Map<String, String> {
        val parameters: MutableMap<String, String> = HashMap()
        val params = request.parameterNames
        while (params.hasMoreElements()) {
            val paramName = params.nextElement()
            val paramValue = request.getParameter(paramName)
            parameters[paramName] = paramValue
        }
        return parameters
    }
}
