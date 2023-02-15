package com.igatn.extranet.rest.languages;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.google.common.collect.Lists;
import com.igatn.extranet.domainjpa.api.data.LanguageRepository;
import com.igatn.extranet.domainjpa.impl.domain.translation.Language;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * OJA - Language Controller
 */
@Slf4j
@RestController
@RequestMapping(path = "/languages", produces = "application/json")
public class LanguageController {

    @Autowired
    private LanguageRepository languageRepository;

    /**
     * test the controller
     *
     * @return
     */
    @GetMapping
    public String helloLanguages() {
        log.info("/languages");

        return "Hello from /languages";
    }

    /**
     * OJA - Return all the languages supported by the app
     *
     * @return
     * @throws JsonProcessingException
     */
    @GetMapping(path = "/getAll")
    public ResponseEntity<?> all() throws JsonProcessingException {
        
        log.info("-- /languages/getAll endpoint reached! -- \n");
        
        List<Language> languages = Lists.newArrayList(languageRepository.findAll());
        
        LanguagesResponseModel response = new LanguagesResponseModel();

        response.setSuccess(true);
        response.setLanguages(languages);

        return ResponseEntity.ok(response);
    }
}

@Data
@AllArgsConstructor
@NoArgsConstructor
class LanguagesResponseModel {
    Boolean success;
    List<Language> languages;
}
