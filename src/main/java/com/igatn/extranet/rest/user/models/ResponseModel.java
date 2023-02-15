package com.igatn.extranet.rest.user.models;

import com.igatn.extranet.domainjpa.impl.domain.translation.Language;
import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Data
public class ResponseModel extends UserDetails {
    Boolean success;
    Language language;
    Boolean notificationEnabled;
    Boolean biometricEnabled;
}
