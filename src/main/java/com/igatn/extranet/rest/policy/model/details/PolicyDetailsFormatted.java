package com.igatn.extranet.rest.policy.model.details;

import lombok.*;

import java.util.List;

@RequiredArgsConstructor
@Getter
@Setter
@ToString
public class PolicyDetailsFormatted {
    Boolean success;
//    String[] errorMsgs;
//    String[] infoMsgs;
    List<?> beneficiaries;
    List<?> bankAccounts;
    List<?> guarantees;
//    List<?> documents;
}
