package com.igatn.extranet.rest.policy;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.igatn.extranet.rest.exceptions.ApiError;
import com.igatn.extranet.rest.exceptions.ws.ExternalWsNoResponseException;
import com.igatn.extranet.rest.policy.model.FormattedPolicies;
import com.igatn.extranet.rest.policy.model.PoliciesDefinition;
import com.igatn.extranet.rest.policy.model.details.PolicyDetailsDefinition;
import com.igatn.extranet.rest.policy.model.details.PolicyDetailsFormatted;
import com.igatn.extranet.utils.RestUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * FRE - Insured Policy controller
 */
@Slf4j
@RestController
@RequestMapping(path = "/policies", produces = "application/json")
public class PolicyController {

    @Autowired
    private RestUtils restUtils;
    
    /**
     * FRE - test the controller
     *
     * @return
     */
    @GetMapping
    public String helloPolicy() {
        log.info("/policies endpoint called!");

        return "Hello from /policies";
    }

    /**
     * FRE - Revert policies list
     *
     * @param languageIsoCode
     * @param indexFrom
     * @param indexTo
     * @return
     * @throws JsonProcessingException
     */
    @GetMapping(path = "/getAll")
    public ResponseEntity<?> all(
            @RequestParam(value = "language", required = false) String languageIsoCode,
            @RequestParam(value = "skip", required = true, defaultValue = "0") Integer indexFrom,
            @RequestParam(value = "take", required = true, defaultValue = "5") Integer indexTo
    ) {

        log.info("-- /policies/getAll endpoint called! -- \n");

        String url = "http://192.168.216.17:5700/api/policies/getAll";

        Map<String, String> queryParams = new HashMap<>();

        queryParams.put("language", "fr");

        // 1sd: index from
        if (indexFrom != null) queryParams.put("skip", indexFrom.toString());

        // 2nd: index to
        if (indexTo != null) queryParams.put("take", indexTo.toString());

        ResponseEntity<PoliciesDefinition> responsePolicies = restUtils.prepareGetWS(url, queryParams, PoliciesDefinition.class);
        log.info("Ws polices response: " + responsePolicies);

        Optional<PoliciesDefinition> responseBody = Optional.ofNullable(responsePolicies.getBody());
        PoliciesDefinition response = responseBody.orElseThrow(
                ()-> new ExternalWsNoResponseException("WS /policies/getAll is not returning a valid response")
        );

        FormattedPolicies formattedPolicies = new FormattedPolicies();
        formattedPolicies.setSuccess(response.getSuccess());
        formattedPolicies.setTotalResults(response.getTotalResults());
        formattedPolicies.setPolicies(response.getItems());

        ApiError apiError = new ApiError();
        if (Boolean.FALSE.equals(response.getSuccess())) {
            apiError.setCode(500);
            apiError.setMessage(Arrays.stream(response.getErrorMsgs()).findFirst().get());

            return new ResponseEntity<>(apiError,null, apiError.getCode());
        }

        log.info("response ready to go: \n"+ responsePolicies);

        return ResponseEntity.ok(formattedPolicies);

    }

    /**
     * FRE - Controller handler for /getDetails endpoint
     *
     * @param policyId
     * @param languageIsoCode
     * @return
     */
    @GetMapping(path = "/getDetails")
    public ResponseEntity<?> getOneById(
            @RequestParam(value = "id") String policyId,
            @RequestParam(value = "language", required = false) String languageIsoCode // currently unused
    ) {
        
        ApiError apierror = new ApiError();
        
        log.info("-- /policies/getDetails endpoint called! -- \n");

        //url
        String url = ("http://192.168.216.17:5700/api/policies/getDetails");
        Map<String, String> queryParams = new HashMap<>();
        queryParams.put("id", policyId);
        queryParams.put("language", "fr");
        
        log.info(queryParams.toString());

        ResponseEntity<PolicyDetailsDefinition> responsePolicyDetails = restUtils.prepareGetWS(url, queryParams, PolicyDetailsDefinition.class);
        log.info(String.valueOf(responsePolicyDetails));
        PolicyDetailsDefinition response = responsePolicyDetails.getBody();
        if (!response.getSuccess()) {
            apierror.setCode(500);
            apierror.setMessage(Arrays.stream(response.getErrorMsgs()).findFirst().get());
            return new ResponseEntity<>(apierror,null, apierror.getCode());
        }

        PolicyDetailsFormatted formattedPolicyDetails = new PolicyDetailsFormatted();
        formattedPolicyDetails.setSuccess(response.getSuccess());
        formattedPolicyDetails.setBeneficiaries(response.getItem().getBeneficiaries());
        formattedPolicyDetails.setBankAccounts(response.getItem().getBankAccounts());
        formattedPolicyDetails.setGuarantees(response.getItem().getGuarantees());


        return ResponseEntity.ok(formattedPolicyDetails);

    }
}
