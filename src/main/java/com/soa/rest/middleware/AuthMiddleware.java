package com.soa.rest.middleware;

import com.soa.rest.client.SoapAuthClient;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * Authentication Middleware.
 *
 * Лабын шаардлага:
 *   "JSON service доторх middleware нь SOAP service руу request илгээж
 *    authentication-г баталгаажуулна."
 *
 * Flow: Request -> AuthMiddleware -> SoapAuthClient.validateToken()
 *       -> SOAP ValidateToken -> true  -> allow
 *                             -> false -> 401 Unauthorized
 *
 * Token: Authorization header-ээс авна.
 */
@Component
public class AuthMiddleware extends OncePerRequestFilter {

    private final SoapAuthClient soapAuthClient;

    public AuthMiddleware(SoapAuthClient soapAuthClient) {
        this.soapAuthClient = soapAuthClient;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        // OPTIONS (CORS preflight) хүсэлтийг нэвтрүүлэх
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            filterChain.doFilter(request, response);
            return;
        }

        String token = request.getHeader("Authorization");

        if (token == null || token.isBlank()) {
            sendUnauthorized(response, "Authorization token required");
            return;
        }

        // Bearer prefix байвал хасах
        if (token.startsWith("Bearer ")) {
            token = token.substring(7);
        }

        // SOAP ValidateToken дуудах
        SoapAuthClient.ValidateResult result = soapAuthClient.validateToken(token);

        if (!result.valid()) {
            sendUnauthorized(response, "Invalid or expired token");
            return;
        }

        // userId-г request attribute-д хадгалах (controller-д ашиглахад)
        if (result.userId() != null) {
            request.setAttribute("authUserId", result.userId());
        }

        filterChain.doFilter(request, response);
    }

    private void sendUnauthorized(HttpServletResponse response, String message) throws IOException {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType("application/json;charset=UTF-8");
        response.getWriter().write("{\"error\":\"" + message + "\"}");
    }
}
