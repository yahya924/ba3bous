package com.igatn.extranet.service.otp;

import com.igatn.extranet.domainjpa.impl.domain.auth.OtpCode;
import com.igatn.extranet.domainjpa.impl.domain.user.User;

import javax.validation.constraints.NotNull;

public interface OtpService {
    void sendOtpCodeEmail(User user, String recipient);
    boolean checkCodeByUser(String code, User user);
    @NotNull OtpCode getLatestUsedByUser(User user);
}
