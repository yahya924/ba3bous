package com.igatn.extranet.security.providers;

import com.igatn.extranet.domainjpa.impl.domain.user.User;
import com.igatn.extranet.security.models.UsernamePasswordAuthentication;
import com.igatn.extranet.service.user.UserServiceImpl;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.stereotype.Component;

@Component
public class UsernamePasswordAuthenticationProvider implements AuthenticationProvider {

    private final UserServiceImpl userServiceImpl;

    public UsernamePasswordAuthenticationProvider(UserServiceImpl userServiceImpl) {
        this.userServiceImpl = userServiceImpl;
    }

    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
       
        String username = authentication.getName();
        String password = String.valueOf(authentication.getCredentials());
        
        User user = userServiceImpl.findByUsernameAndPassword(username, password);
        
        return new UsernamePasswordAuthentication(user.getUsername(), null, user.getAuthorities());
    }

    @Override
    public boolean supports(Class<?> aClass) {
        return UsernamePasswordAuthentication.class.isAssignableFrom(aClass);
    }
}
