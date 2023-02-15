package com.igatn.extranet.rest.historyActivity;

import com.google.common.collect.Lists;
import com.igatn.extranet.domainjpa.api.data.demand.TypeDemandRepository;
import com.igatn.extranet.domainjpa.impl.domain.demand.TypeDemand;
import com.igatn.extranet.domainjpa.impl.domain.user.User;
import com.igatn.extranet.rest.historyActivity.models.DemandTypesResponseModel;
import com.igatn.extranet.rest.historyActivity.models.HistoryActivityListParams;
import com.igatn.extranet.rest.historyActivity.models.HistoryActivityListParams.HistoryActivityParamsBuilder;
import com.igatn.extranet.rest.historyActivity.models.HistoryActivityWsResponse;
import com.igatn.extranet.rest.historyActivity.service.HistoryActivityService;
import com.igatn.extranet.utils.ExtranetUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * ASGHIR - Insured Reimbursements, rest controller
 */
@Slf4j
@RestController
@RequestMapping(path = "/historyActivity", produces = "application/json")
public class HistoryActivityController {

    @Autowired
    private TypeDemandRepository typeDemandRepository;
    
    @Autowired
    private HistoryActivityService historyActivityService;

    /**
     * test the controller
     *
     * @return
     */
    @GetMapping
    public String helloHistoryActivity() {
        log.info("/history activity endpoint called!");

        return "Hello from /history activity";
    }

    /**
     * FRE - Revert history activity with search
     * 
     * @param languageIsoCode
     * @param indexFrom
     * @param indexTo
     * @param keyword
     * @param auth
     * @return
     */
    @GetMapping(path = "/getAll")
    public ResponseEntity<HistoryActivityWsResponse> all(
            @RequestParam(value = "language", required = false) String languageIsoCode,
            @RequestParam(value = "skip", defaultValue = "0") Integer indexFrom,
            @RequestParam(value = "take", defaultValue = "5") Integer indexTo,
            @RequestParam(value = "keyword", required = false, defaultValue = "") String keyword,
            @RequestParam(value = "dateFrom", required = false, defaultValue = "") String dateFrom,
            @RequestParam(value = "dateTo", required = false, defaultValue = "") String dateTo,
            @RequestParam(value = "type", required = false, defaultValue = "") String type,
            Authentication auth
    )
    {
        log.info("-- /historyActivity/getAll endpoint called! --");

        User user = (User) auth.getPrincipal();

        HistoryActivityParamsBuilder params = HistoryActivityListParams.builder()
            .setUser(user)
            .setIndexFrom(indexFrom)
            .setIndexTo(indexTo);
        
        if (ExtranetUtils.verifyDateValidity(dateFrom))
            params.setDateFrom(dateFrom);
        
        if (ExtranetUtils.verifyDateValidity(dateTo))
            params.setDateTo(dateTo);

        if (StringUtils.hasText(keyword))
            params.setKeyword(keyword);

        if (StringUtils.hasText(type))
            params.setType(type);
        
        HistoryActivityListParams paramsToUse = params.create();
        
        HistoryActivityWsResponse result = historyActivityService.search(paramsToUse);

//        log.info("-- Sending response to client-side! --");

        return ResponseEntity.ok(result);
        
    }
    
//    private List<HistoryActivity> formatHistoryActivities(List<Demand> demands, int skip, int take) {
//        
//        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm").withZone(ZoneId.of("GMT"));
//
//        return demands.subList(skip, take)
//                .stream()
//                .map(demand -> {
//                    HistoryActivity historyActivity = new HistoryActivity();
//                    historyActivity.setId(demand.getId());
//                    historyActivity.setSubject(demand.getSubject());
//                    historyActivity.setMessage(demand.getMessage());
//                    historyActivity.setCreatedAt(formatter.format(demand.getCreatedAt()));
//                    historyActivity.setType(demand.getType().getCode());
//                    historyActivity.setAttachments(demand.getAttachments());
//
//                    return historyActivity;
//                })
//                .collect(Collectors.toList());
//    }

    /**
     * OJA - Return all the history activity types
     *
     * @return
     */
    @GetMapping(path = "/getTypes")
    public ResponseEntity<DemandTypesResponseModel> allTypes() {

        log.info("-- /historyActivity/getTypes endpoint reached! -- \n");

        List<TypeDemand> types = Lists.newArrayList(typeDemandRepository.findAll());

        DemandTypesResponseModel response = new DemandTypesResponseModel();

        response.setSuccess(true);
        response.setTypes(types);

        return ResponseEntity.ok(response);
    }
}
