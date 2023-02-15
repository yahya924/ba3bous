package com.igatn.extranet.rest.support;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.igatn.extranet.domainjpa.impl.domain.support.Support;
import com.igatn.extranet.domainjpa.impl.domain.user.User;
import com.igatn.extranet.domainjpa.api.data.SupportRepository;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Set;

/**
 * OJA - Support Controller
 */
@Slf4j
@RestController
@RequestMapping(path = "/supports", produces = "application/json")
public class SupportController {
    
    @Autowired
    SupportRepository supportRepository;

    /**
     * test the controller
     *
     * @return
     */
    @GetMapping
    public String helloSupport() {
        log.info("/supports");

        return "Hello from /supports";
    }

    /**
     * OJA - Return the user's supports
     *
     * @return
     * @throws JsonProcessingException
     */
    @GetMapping(path = "/getAll")
    public ResponseEntity<?> all(Authentication auth) throws JsonProcessingException {

        log.info("-- /supports/getAll endpoint reached! -- \n");
        
        User user = (User) auth.getPrincipal();
        
        Set<Support> supports = supportRepository.findByUser(user);
        
        SupportsResponseModel response = new SupportsResponseModel();
        
        response.setSuccess(true);
        response.setSupports(supports);
                
        return ResponseEntity.ok(response);
    }
}

@Data
@AllArgsConstructor
@NoArgsConstructor
class SupportsResponseModel {
    Boolean success;
    Set<Support> supports;
}
