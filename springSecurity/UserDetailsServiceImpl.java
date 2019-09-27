package com.richinfoai.server.security;

import com.richinfoai.server.model.auth.CommunityUser;
import com.richinfoai.server.service.AuthService;
import org.springframework.security.cas.authentication.CasAssertionAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.AuthenticationUserDetailsService;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.List;

public class UserDetailsServiceImpl implements AuthenticationUserDetailsService<CasAssertionAuthenticationToken> {

    private AuthService service;

    public UserDetailsServiceImpl(AuthService service) {
        this.service = service;
    }

    @Override
    public UserDetails loadUserDetails(CasAssertionAuthenticationToken token) throws UsernameNotFoundException {
        List<GrantedAuthority> authorities = service.getAuthoritiesByUserName(token.getName());
        return new CommunityUser(token.getName(), token.getCredentials().toString(), authorities,"平台");
    }

}
