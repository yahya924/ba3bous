package com.igatn.extranet.rest.user.models;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

@RequiredArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class UpdatePasswordParameters {
    String username;
    String oldPassword;
    String newPassword;
}
