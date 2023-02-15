package com.igatn.extranet.rest.user.models;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

@RequiredArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class UpdateForgottenPasswordParameters {
    String email;
    String password;
    
    @Override
    public String toString() {
        return "{ \"email\": \"" + this.email + "\", \"password\": \"" + this.password + "\" }";
    }
}
