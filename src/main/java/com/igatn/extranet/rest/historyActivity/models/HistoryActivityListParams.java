package com.igatn.extranet.rest.historyActivity.models;

import com.igatn.extranet.domainjpa.impl.domain.user.User;
import lombok.Builder;
import lombok.Data;

@Data
@Builder(
    buildMethodName = "create",
    builderClassName = "HistoryActivityParamsBuilder",
    setterPrefix = "set"
)
public class HistoryActivityListParams {
    final private User user;
    final private int indexFrom;
    final private int indexTo;
    private String keyword;
    private String dateFrom;
    private String dateTo;
    private String type;
}



