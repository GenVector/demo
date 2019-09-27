package com.richinfoai.server.security;

import com.richinfoai.server.model.auth.CommunityAuthority;
import lombok.Data;
import org.springframework.security.access.PermissionEvaluator;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Component;

import java.io.Serializable;
import java.util.Collection;


@Data
@Component
public class CommunityPermissionEvaluator implements PermissionEvaluator {

    /**
     *
     * 权限级别
     *
     */
    @Override
    public boolean hasPermission(Authentication authentication, Object targetDomainObject, Object permission) {
        try {
            Collection<? extends GrantedAuthority> authorities = authentication.getAuthorities();
            if (authorities != null && !authorities.isEmpty()) {
                GrantedAuthority next = authorities.iterator().next();
                int level = Integer.parseInt(((CommunityAuthority) next).getLevel());
                int per = (Integer) permission;
                if (level >= per) {
                    return true;
                }
            }
        } catch (Throwable e) {
            e.printStackTrace();
        }
        return false;
    }

    @Override
    public boolean hasPermission(Authentication authentication, Serializable targetId, String targetType, Object permission) {

        return false;
    }
}
