package com.igatn.extranet.rest.historyActivity.models;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
public class HistoryActivityWsResponse {
    
    private Boolean success;
    private String message;
    private int totalResults;
    private List<HistoryActivity> activities;

//    public HistoryActivityWsResponse(Boolean success, String message) {
//        this.success = success;
//        this.message = message;
//    }

    public HistoryActivityWsResponse(Boolean success) {
        super();
        this.success = success;
    }

//    public static HistoryActivityWsResponse getSuccessInstance(){
//        return new HistoryActivityWsResponse(true);
//    }
//
//    public static HistoryActivityWsResponse getSuccessInstance(String msg){
//        return new HistoryActivityWsResponse(true, msg);
//    }

//    public static HistoryActivityWsResponse getErrorInstance(String msg){
//        return new HistoryActivityWsResponse(false, msg);
//    }

    public static HistoryActivityWsResponse getSuccessInstance(List<HistoryActivity> list, int totalResult){
      
        HistoryActivityWsResponse hawr = new HistoryActivityWsResponse(true);
        hawr.setActivities(list);
        hawr.setTotalResults(totalResult);
        
        return hawr;
    }
    
}
