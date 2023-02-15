package com.igatn.extranet.rest.notifications.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class APNSandboxResult {
    String registration_token;
    String apns_token;
    String status;
}
