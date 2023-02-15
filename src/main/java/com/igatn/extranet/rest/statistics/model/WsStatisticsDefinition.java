package com.igatn.extranet.rest.statistics.model;
import lombok.Builder;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

public class WsStatisticsDefinition {

    @Data
    public static class WsListStatisticsResponseInternal {
        List<WsStatisticsInternal> statistics = new ArrayList<>();
    }

    @Data
    @Builder
    public static class WsListStatisticsParamsInternal {
        String language;
        String status;
        boolean hasIndividualPolicies;
    }
    @Data
    public static class WsStatisticsInternal {
        int id;
        String code;
        String title;
        int totalResults;
    }
}
