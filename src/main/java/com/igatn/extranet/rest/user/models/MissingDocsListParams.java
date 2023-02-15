package com.igatn.extranet.rest.user.models;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class MissingDocsListParams {
    private String language;
    private String status;
    private String type;
    private int indexFrom;
    private int indexTo;
}
