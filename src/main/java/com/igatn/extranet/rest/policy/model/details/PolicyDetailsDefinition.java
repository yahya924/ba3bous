package com.igatn.extranet.rest.policy.model.details;

import lombok.*;


@RequiredArgsConstructor
@Getter
@Setter
@ToString
public class PolicyDetailsDefinition {
    Boolean success;
    String[] errorMsgs;
    String[] infoMsgs;
    PolicyDetails item;
}
