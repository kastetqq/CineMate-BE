package com.ratingapp.auth.security.jwt;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import com.ratingapp.auth.entity.User;
import com.ratingapp.auth.security.CustomUserDetails;
import com.ratingapp.auth.security.CustomUserServiceImpl;

import java.io.IOException;

@Component
@RequiredArgsConstructor
@Slf4j
public class JwtFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final CustomUserServiceImpl customUserService;

    @Override
    protected void doFilterInternal(HttpServletRequest request, 
                                    HttpServletResponse response, 
                                    FilterChain filterChain) throws ServletException, IOException {
        
        try {
            String token = getTokenFromRequest(request);
            
            if (token != null && jwtService.validateJwtToken(token) && jwtService.isAccessToken(token)) {
                String email = jwtService.getEmailFromToken(token);
                
                if (email != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                    UserDetails userDetails = customUserService.loadUserByUsername(email);
                    
                    if (userDetails != null) {
                        UsernamePasswordAuthenticationToken authentication = 
                            new UsernamePasswordAuthenticationToken(
                                userDetails,
                                null, 
                                userDetails.getAuthorities()
                            );
                        
                        authentication.setDetails(
                            new WebAuthenticationDetailsSource().buildDetails(request)
                        );
                        
                        SecurityContextHolder.getContext().setAuthentication(authentication);

                        if (userDetails instanceof CustomUserDetails) {
                            User user = ((CustomUserDetails) userDetails).getUser();
                            request.setAttribute("userId", user.getId());
                            log.debug("Authenticated user: {} with ID: {}", email, user.getId());
                        }
                    }
                }
            }
        } catch (Exception e) {
            log.error("Cannot set user authentication: {}", e.getMessage());
        }
        
        filterChain.doFilter(request, response);
    }

    private String getTokenFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader(HttpHeaders.AUTHORIZATION);
        
        if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        
        return null;
    }

    @Override
    protected boolean shouldNotFilterAsyncDispatch() {
        return false;
    }
}