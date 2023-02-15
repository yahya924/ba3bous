package com.igatn.extranet.rest.models.ws;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class WsExternalResponse extends WsResponse{
    protected String[] errorMsgs;
    protected String[] infoMsgs;
}
