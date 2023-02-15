package com.igatn.extranet.service.user;

import com.igatn.extranet.domainjpa.impl.domain.device.Device;
import com.igatn.extranet.domainjpa.impl.domain.tracking.AppAuthHistory;
import com.igatn.extranet.domainjpa.impl.domain.user.User;
import com.igatn.extranet.rest.user.models.BasicAuthResponse;
import com.igatn.extranet.rest.user.models.BasicCredentials;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import javax.annotation.Nullable;
import javax.validation.constraints.NotNull;

public interface UserService extends UserDetailsService {
    
    @NotNull User findByUsernameAndPassword(String username, String password) throws UsernameNotFoundException;

    AppAuthHistory externalSignIn(User user);

    @NotNull BasicAuthResponse buildBasicAuthResponse(BasicCredentials credentials);

    @NotNull User getByDevice(Device device);
    
    @Nullable User findByUsername(String username);
}
