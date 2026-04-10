package pl.edu.uj.projzes.itl.infrastructure.web;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import pl.edu.uj.projzes.itl.domain.user.CurrentUser;
import pl.edu.uj.projzes.itl.domain.user.UserRole;

import java.io.IOException;

/**
 * Tymczasowy mechanizm identyfikacji użytkownika oparty na nagłówkach HTTP.
 * Docelowo zastąpiony walidacją tokenu JWT
 */
@Component
public class UserContextFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        try {
            String userId = request.getHeader("X-User-Id");
            String roleHeader = request.getHeader("X-User-Role");

            if (userId != null && roleHeader != null) {
                try {
                    UserRole role = UserRole.valueOf(roleHeader.toUpperCase());
                    UserContextHolder.set(new CurrentUser(userId, role));
                } catch (IllegalArgumentException ignored) {
                    // nieznana rola - request przechodzi bez kontekstu użytkownika
                }
            }

            filterChain.doFilter(request, response);
        } finally {
            UserContextHolder.clear();
        }
    }
}
