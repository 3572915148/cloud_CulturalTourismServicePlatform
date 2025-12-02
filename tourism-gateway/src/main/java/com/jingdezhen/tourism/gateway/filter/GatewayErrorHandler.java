package com.jingdezhen.tourism.gateway.filter;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.web.reactive.error.ErrorWebExceptionHandler;
import org.springframework.cloud.gateway.support.NotFoundException;
import org.springframework.core.annotation.Order;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;

/**
 * 网关全局错误处理器
 */
@Slf4j
@Component
@Order(-1)
public class GatewayErrorHandler implements ErrorWebExceptionHandler {

    @Override
    public Mono<Void> handle(ServerWebExchange exchange, Throwable ex) {
        ServerHttpResponse response = exchange.getResponse();
        
        if (response.isCommitted()) {
            return Mono.error(ex);
        }

        HttpStatus status = HttpStatus.INTERNAL_SERVER_ERROR;
        String message = "系统异常，请联系管理员";

        if (ex instanceof NotFoundException) {
            status = HttpStatus.NOT_FOUND;
            message = "服务未找到，请检查服务是否已注册到注册中心";
            log.error("服务未找到: {}", ex.getMessage());
        } else if (ex instanceof ResponseStatusException) {
            ResponseStatusException responseStatusException = (ResponseStatusException) ex;
            HttpStatus responseStatus = HttpStatus.resolve(responseStatusException.getStatusCode().value());
            if (responseStatus != null) {
                status = responseStatus;
            }
            message = responseStatusException.getReason() != null ? responseStatusException.getReason() : message;
            log.error("响应状态异常: {}", ex.getMessage(), ex);
        } else if (ex instanceof org.springframework.web.reactive.resource.NoResourceFoundException) {
            status = HttpStatus.NOT_FOUND;
            message = "路由未找到，请检查请求路径是否正确。正确路径应为：/api/user/login";
            log.error("路由未找到: {}", ex.getMessage());
        } else {
            log.error("网关异常: {}", ex.getMessage(), ex);
        }

        response.setStatusCode(status);
        response.getHeaders().setContentType(MediaType.APPLICATION_JSON);

        String errorJson = String.format(
            "{\"timestamp\":\"%s\",\"path\":\"%s\",\"status\":%d,\"error\":\"%s\",\"message\":\"%s\"}",
            java.time.Instant.now(),
            exchange.getRequest().getPath(),
            status.value(),
            status.getReasonPhrase(),
            message
        );

        DataBuffer buffer = response.bufferFactory().wrap(errorJson.getBytes(StandardCharsets.UTF_8));
        return response.writeWith(Mono.just(buffer));
    }
}

