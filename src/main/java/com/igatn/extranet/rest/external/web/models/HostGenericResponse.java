package com.igatn.extranet.rest.external.web.models;

import lombok.Data;

/*
    This class was implemented in order to handle the response from SAGILEA API. This idea was abandoned but we can comeback to this class if the idea resurfaced again.
 */
@Data
public class HostGenericResponse {
    
    private Boolean success;
    private int status;
    private String message;
    private String info;
    private Object[] data;
}
