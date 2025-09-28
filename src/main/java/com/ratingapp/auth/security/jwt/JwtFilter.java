package com.ratingapp.auth.security.jwt;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import com.ratingapp.auth.security.CustomUserServiceImpl;

import java.io.IOException;

@Component
@RequiredArgsConstructor
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
                setAuthenticationToSecurityContextHolder(token, request);
            }
            
        } catch (Exception e) {
            logger.error("Cannot set user authentication: {}");
        }
        
        filterChain.doFilter(request, response);
    }

    private void setAuthenticationToSecurityContextHolder(String token, HttpServletRequest request) {
        String email = jwtService.getEmailFromToken(token);
        UserDetails userDetails = customUserService.loadUserByUsername(email);
        
        if (userDetails != null && SecurityContextHolder.getContext().getAuthentication() == null) {
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
        }
    }

    private String getTokenFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader(HttpHeaders.AUTHORIZATION);
        
        if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        
        return null;
    }
}