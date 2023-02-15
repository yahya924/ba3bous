package com.igatn.extranet.rest.documents.model;

import lombok.Builder;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * Document list definition model
 */
public class WsDocDefinition {

    @Data
    public static class WsListDocResponse {
        int totalResults = 0;
        List<WsListDocItem> documents = new ArrayList<>();
    }

    @Data
    @Builder
    public static class WsListDocParams {
        String language;
        int indexFrom;
        int indexTo;
    }

    /**
     * The target doc item
     */
    @Data
    public static class WsListDocItem {
        long id;
        String name, beneficiaryName, url;
    }
}