package com.playmatsec.app.config;

import java.util.UUID;

import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

import com.playmatsec.app.repository.UserRepository;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Interceptor minimalista que valida la existencia del usuario indicado en X-User-Id
 * sólo cuando el handler (método o clase) está anotado con @Authorized.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class AuthorizedInterceptor implements HandlerInterceptor {

    private static final String HEADER = "X-User-Id";
    private final UserRepository userRepository;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        if (!(handler instanceof HandlerMethod hm)) {
            return true; // No aplicable (recursos estáticos u otros)
        }
        boolean required = hm.getMethodAnnotation(Authorized.class) != null || hm.getBeanType().getAnnotation(Authorized.class) != null;
        if (!required) {
            return true; // No requiere autorización
        }
        String userId = request.getHeader(HEADER);
        // Bypass especial para UsersController cuando el header es 'system-clerk'
        if ("system-clerk".equals(userId) && hm.getBeanType().getSimpleName().equals("UsersController")) {
            return true; // acceso permitido sin validar en DB
        }
        if (userId == null || userId.isBlank()) {
            deny(response, 401, "Access denied");
            return false;
        }
        try {
            UUID uuid = UUID.fromString(userId.trim());
            if (userRepository.getById(uuid) == null) {
                deny(response, 401, "User not found");
                return false;
            }
            return true;
        } catch (IllegalArgumentException e) {
            deny(response, 400, "Invalid data format");
            return false;
        }
    }

    private void deny(HttpServletResponse response, int status, String message) throws Exception {
        log.debug("Authorization failed: {} - {}", status, message);
        response.setStatus(status);
        response.setContentType("application/json");
        response.getWriter().write("{\"error\":\"" + message + "\"}");
    }
}
