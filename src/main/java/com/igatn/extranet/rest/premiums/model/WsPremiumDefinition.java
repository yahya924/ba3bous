package com.igatn.extranet.rest.premiums.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

public class WsPremiumDefinition {

    @Data
    public static class WsListPremiumsResponseInternal {
        int totalResults = 0;
        List<WsPremiumItemInternal> premiums = new ArrayList<>();
        double totalAmountToPay = 0;
    }
    
    @Data
    @Builder
    public static class WsListPremiumsParamsInternal {
        String language;
        String status;
        int indexFrom;
        int indexTo;
    }
    
    @Data
    @AllArgsConstructor
    public static class ItemIntervalExpressionElement {
        String label;
        Boolean toTranslate;
    }
    
    /**
     * Premiums model
     */
    @Data
    public static class WsPremiumItemInternal {
        long id;
        double amount;
        double amountPending;
        String from;
        String to;
        List<ItemIntervalExpressionElement> interval;
        String due;
        String paymentDate;
        String status;
    }
}
