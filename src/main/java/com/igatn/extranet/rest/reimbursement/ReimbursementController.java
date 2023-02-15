package com.igatn.extranet.rest.reimbursement;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.igatn.extranet.domainjpa.impl.domain.user.User;
import com.igatn.extranet.rest.exceptions.ApiError;
import com.igatn.extranet.rest.exceptions.OccuredErrorException;
import com.igatn.extranet.rest.reimbursement.model.FormattedReimbursementDetails;
import com.igatn.extranet.rest.reimbursement.model.ReimbursementDetailsListDefinition;
import com.igatn.extranet.rest.reimbursement.model.ReimbursementInternal.ReimbursementsListDefinitionInternal;
import com.igatn.extranet.rest.reimbursement.model.ReimbursementInternal.ReimbursementsListParamsInternal;
import com.igatn.extranet.rest.reimbursement.model.ReimbursementInternal.ReimbursementsListParamsInternal.ReimbursementsListParamsInternalBuilder;
import com.igatn.extranet.rest.reimbursement.service.ReimbursementService;
import com.igatn.extranet.utils.ExtranetUtils;
import com.igatn.extranet.utils.RestUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.*;

/**
 * FRE - Insured Reimbursements, rest controller 
 */
@Slf4j
@RestController
@RequestMapping(path = "/reimbursements", produces = "application/json")
public class ReimbursementController {
    
    final static String CONTROLLER_MAIN_PATH = "/reimbursements";
    
    @Autowired
    private RestUtils restUtils;

    @Autowired
    private ReimbursementService reimbursementService;
    
    /**
     * FRE - test the controller
     *
     * @return
     */
    @GetMapping
    public String helloReimbursement() {
        log.info("/reimbursements endpoint called!");

        return "Hello from /reimbursements";
    }

    /**
     * FRE - Revert reimbursement list with metadata
     *      
     * @param languageIsoCode
     * @param indexFrom
     * @param indexTo
     * @param statusCode
     * @return
     * @throws JsonProcessingException
     */
    @GetMapping(path = "/getAll")
    public ResponseEntity<ReimbursementsListDefinitionInternal> all(
            @RequestParam(value = "language", required = false, defaultValue = "fr") String languageIsoCode,
            @RequestParam(value = "skip", required = true, defaultValue = "0") Integer indexFrom,
            @RequestParam(value = "take", required = true, defaultValue = "5") Integer indexTo,
            @RequestParam(value = "status", required = false, defaultValue = "") String statusCode,
            @RequestParam(value = "sumMin", required = false) Double amountFrom,
            @RequestParam(value = "sumMax", required = false) Double amountTo,
            @RequestParam(value = "dateFrom", required = false) String dateFrom,
            @RequestParam(value = "dateTo", required = false) String dateTo,
            Authentication authentication
    ) {

        log.info("-- " + CONTROLLER_MAIN_PATH + "/getAll endpoint called! -- \n");

        try {

            log.info("Building parameters...");

            // start params builder with default params
            ReimbursementsListParamsInternalBuilder paramsBuilder = ReimbursementsListParamsInternal.builder()
                    .language(languageIsoCode)
                    .indexTo(indexTo)
                    .indexFrom(indexFrom);

            // check status params
            if (StringUtils.hasText(statusCode)) paramsBuilder.status(statusCode);

            // check amount params
            if (Objects.nonNull(amountFrom) && !Double.isNaN(amountFrom)) 
                paramsBuilder.amountFrom(amountFrom);
            
            if (Objects.nonNull(amountTo) &&!Double.isNaN(amountTo))
                paramsBuilder.amountTo(amountTo);

            // check date params

            if (ExtranetUtils.verifyDateValidity(dateFrom)) 
                paramsBuilder.dateFrom(dateFrom);

            if (ExtranetUtils.verifyDateValidity(dateTo))
                paramsBuilder.dateTo(dateTo);

            ReimbursementsListParamsInternal wsParams = paramsBuilder.build();

            ObjectMapper objectMapper = new ObjectMapper();
            ObjectWriter objectWriter = objectMapper.writer().withDefaultPrettyPrinter();
            String parametersAsJson = objectWriter.writeValueAsString(wsParams);
            
            log.info("Parameters received: \n " + parametersAsJson);
            
            User user = (User) authentication.getPrincipal();

            ReimbursementsListDefinitionInternal wsListResponse = reimbursementService.getAll(wsParams, user);
            
            ResponseEntity<ReimbursementsListDefinitionInternal> response = new ResponseEntity<>(wsListResponse, HttpStatus.OK);

            if (Objects.nonNull(response.getBody())) {
                log.info("Sending response to client-side..: \n".concat(objectWriter.writeValueAsString(response.getBody())));

                return response;
            }
            
        } catch (RuntimeException | JsonProcessingException e) {
            throw new OccuredErrorException("An error has occured while processing your request.");
        }

        return new ResponseEntity<>(new ReimbursementsListDefinitionInternal(), HttpStatus.NO_CONTENT);
    }

//    public String prepareStatus(String status) {
//        switch (status) {
//            case "PY" -> { return "PD" ;}
//            case "DP" -> { return "PN"; }
//            case "OK" -> { return "IP"; }
//            default -> { return ""; }
//        }
//    }
//    private Reimbursement[] GetFormattedItems (Reimbursement[] items){
//        Arrays.stream(items).forEach(item-> item.setStatus(prepareStatus(item.getStatus())));
//        return items;
//    }

    @GetMapping(path = "/getDetails")
    public ResponseEntity<?> getOneById(
            // currently unused
            @RequestParam(value = "language", required = false, defaultValue = "fr") String languageIsoCode,
            @RequestParam(value = "id", required = true) String reimbursementId,
            @RequestParam(value = "skip", required = false, defaultValue = "0") Integer indexFrom,
            // the amount not specified in API docs, instead its mentioned in the element #58520
            @RequestParam(value = "take", required = false, defaultValue = "5") Integer indexTo,
            // currently unused
            @RequestParam(value = "text", required = false) String textToSearch
    ) {
        
        log.info("-- /reimbursements/getDetails endpont called -- \n");
        
        ApiError apierror = new ApiError();
        
        String url = ("http://192.168.216.17:5700/api/reimbursements/getDetails");
        
        Map<String, String> queryParams = new HashMap<>();
//        query.append("http://192.168.216.17:5700/api/reimbursements/getDetails");
//        query.append("?"); // start params
        queryParams.put("id", reimbursementId.toString());
        queryParams.put("language", languageIsoCode);
        // 1sd: index from
        if(indexFrom != null) queryParams.put("skip", indexFrom.toString());
        // 2nd: index to
        if(indexTo != null) queryParams.put("take", indexTo.toString());
        log.info(queryParams.toString());
        // using rest template from spring
        // check this: https://www.baeldung.com/rest-template
        ResponseEntity<ReimbursementDetailsListDefinition> responseReimbursementsDetails = restUtils.prepareGetWS(url, queryParams, ReimbursementDetailsListDefinition.class);
        log.info(String.valueOf(responseReimbursementsDetails));
        ReimbursementDetailsListDefinition response = responseReimbursementsDetails.getBody();
        
        if (response == null || !response.getSuccess()) {
            apierror.setCode(500);

            String errorMessage = "The reimbursement details third party API is not returning a valid response.";
            
            if (response != null) {
                Optional<String> apiErrorMessage = Arrays.stream(response.getErrorMsgs()).findFirst();
                
                if (apiErrorMessage.isPresent()) { errorMessage = apiErrorMessage.get(); }
            }
            
            apierror.setMessage(errorMessage);
            
            return new ResponseEntity<>(apierror,null, apierror.getCode());
        }
        
        FormattedReimbursementDetails formattedReimbursementDetails = new FormattedReimbursementDetails();
        formattedReimbursementDetails.setSuccess(response.getSuccess());
        formattedReimbursementDetails.setTotalResults(response.getTotalResults());
        formattedReimbursementDetails.setDetails(response.getItems());
//        log.info("aaaa"+ responseReimbursementsDetails);
        return ResponseEntity.ok(formattedReimbursementDetails);
    }
//    public String prepareStatusDetails(String status) {
//        switch (status) {
//            case "PY" -> { return "PD" ;}
//            case "DP" -> { return "PN"; }
//            case "OK" -> { return "IP"; }
//            default -> { return ""; }
//        }
//    }
//    private ReimbursementDetails[] GetFormattedItems (ReimbursementDetails[] items){
//        Arrays.stream(items).forEach(item-> item.setStatus(prepareStatusDetails(item.getStatus())));
//        return items;
//    }
}



