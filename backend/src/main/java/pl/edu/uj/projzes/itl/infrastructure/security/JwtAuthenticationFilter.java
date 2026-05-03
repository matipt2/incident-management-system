package pl.edu.uj.projzes.itl.infrastructure.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;
import pl.edu.uj.projzes.itl.domain.user.CurrentUser;
import pl.edu.uj.projzes.itl.domain.user.UserRole;
import pl.edu.uj.projzes.itl.infrastructure.web.UserContextHolder;

import java.io.IOException;
import java.util.List;

public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;

    public JwtAuthenticationFilter(JwtService jwtService) {
        this.jwtService = jwtService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        try {
            String token = extractToken(request);
            if (token != null && jwtService.isValid(token)) {
                String userId = jwtService.extractUserId(token);
                String username = jwtService.extractUsername(token);
                UserRole role = jwtService.extractRole(token);

                UserContextHolder.set(new CurrentUser(userId, username, role));

                List<SimpleGrantedAuthority> authorities = role.getPermissions().stream()
                        .map(p -> new SimpleGrantedAuthority(p.name()))
                        .toList();

                UsernamePasswordAuthenticationToken authentication =
                        new UsernamePasswordAuthenticationToken(username, null, authorities);
                SecurityContextHolder.getContext().setAuthentication(authentication);
            }
            filterChain.doFilter(request, response);
        } finally {
            UserContextHolder.clear();
            SecurityContextHolder.clearContext();
        }
    }

    private String extractToken(HttpServletRequest request) {
        String header = request.getHeader("Authorization");
        if (header != null && header.startsWith("Bearer ")) {
            return header.substring(7);
        }
        return null;
    }
}
