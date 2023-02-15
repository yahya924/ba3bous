package com.igatn.extranet.rest.external.web;

import com.igatn.extranet.domainjpa.impl.domain.user.User;
import com.igatn.extranet.rest.exceptions.NullAuthenticationPrincipalException;
import com.igatn.extranet.rest.external.web.models.UpdatePasswordParameters;
import com.igatn.extranet.rest.external.web.service.ExtranetWebService;
import com.igatn.extranet.rest.user.models.GenericSuccessResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@Slf4j
@RestController
@RequestMapping(path = "/extranet-web", produces = "application/json")
public class ExtranetWebController {
    
    @Autowired
    private ExtranetWebService extranetWebService;

    /**
     * Test the controller
     *
     * @return
     */
    @GetMapping
    public String helloExtranetWeb() {
        log.info("/extranet-web");

        return "Hello from /extranet-web";
    }

    /**
     * Update user password from extranet Web endpoint
     *
     * @return
     */
    @PostMapping("update-password")
    public ResponseEntity<GenericSuccessResponse> updatePassword(@RequestBody UpdatePasswordParameters parameters, Authentication auth) 
    {
        User user = Optional.ofNullable((User) auth.getPrincipal()).orElseThrow(NullAuthenticationPrincipalException::new);
        
        Boolean success = extranetWebService.syncInternalUserPassword(user, parameters.getPassword());

        GenericSuccessResponse successResponse = new GenericSuccessResponse(success);

        return ResponseEntity.ok(successResponse);
    }
}
