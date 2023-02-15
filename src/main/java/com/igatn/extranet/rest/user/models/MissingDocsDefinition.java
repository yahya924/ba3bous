package com.igatn.extranet.rest.user.models;
import lombok.Data;

import java.util.List;


@Data
public class MissingDocsDefinition {
    @Data
    public static class WsMissingDocsResponse {
        boolean success;
        int totalResults;
        List<WsMissingDocsInternal> items;

    }
    @Data
    public static class WsMissingDocsInternal {
        String type;
        String entityId;
        String beneficiaryFirstName;
        String beneficiaryLastName;
        String date;
        String label;
        String status;
        List<ListMissingDocs> missingDocs;
        List<ListAttachmentDocs> attachments;
    }
    @Data
    public static class ListMissingDocs {
        int id;
        String name;
        Boolean mandatory;
    }
    @Data
    public static class ListAttachmentDocs {
        int id;
        String fileName;
        String fileUrl;
        String createdAt;
    }
}


