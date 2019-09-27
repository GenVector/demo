package com.richinfoai.server.security;

import lombok.val;
import org.jasig.cas.client.validation.AssertionImpl;
import org.springframework.security.cas.authentication.CasAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ServiceAuthenticationFilter extends OncePerRequestFilter {

    private final Map<String, Authentication> map = new HashMap<>();
    private final String headerName;

    public ServiceAuthenticationFilter(String headerName, String headerValue) {
        this.headerName = headerName;
        List<GrantedAuthority> authorities = new ArrayList<>();
        val user = new User(headerValue, "", authorities);
        map.put(headerValue, new CasAuthenticationToken(headerValue, user, user, user.getAuthorities(), user, new AssertionImpl(headerValue)));
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        val authorization = request.getHeader(headerName);
        if (authorization != null) {
            val auth = map.get(authorization);
            if (auth != null) {
                SecurityContextHolder.getContext().setAuthentication(auth);
            }
        }

        filterChain.doFilter(request, response);
    }
}
