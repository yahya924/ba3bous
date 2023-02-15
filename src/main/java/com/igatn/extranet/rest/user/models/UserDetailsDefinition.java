package com.igatn.extranet.rest.user.models;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Data
@ToString
@Getter
@Setter
public class UserDetailsDefinition {
    Boolean success;
    String[] errorMsgs;
    String[] infoMsgs;
    ResponseModel item;
}
