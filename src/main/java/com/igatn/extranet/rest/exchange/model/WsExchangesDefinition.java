package com.igatn.extranet.rest.exchange.model;

import lombok.Builder;
import lombok.Data;
import lombok.ToString;

public class WsExchangesDefinition {
    /**
     *  Exchange query format model
     */
    @Data
    @Builder
    public static class WsListExchangeParamsInternal {
        private String language;
        private int indexFrom;
        private int indexTo;
        private String status;
        private String dateFrom;
        private String dateTo;
        private String direction;
    }

    /**
     * Exchange model
     */
    @Data
    public static class Exchange {
        int id;
        String title;
        String sens;
        String source;
        String creationDate;
        String status;
    }

    /**
     * Exchange list definition model
     */
    @Data
    @ToString
    public static class ExchangeListDefinition {
        Boolean success;
        int totalResults;
        int totalPages;
        String[] errorMsgs;
        Exchange[] items;
    }

    /**
     *  Exchange Api Response
     */
    @Data
    public static class FormattedExchange {
        Boolean success;
        int totalResults;
        Exchange[] exchanges;
    }
}
