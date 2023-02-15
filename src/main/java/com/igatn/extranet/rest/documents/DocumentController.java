package com.igatn.extranet.rest.documents;

import com.igatn.extranet.rest.documents.model.WsDocDefinition.WsListDocParams;
import com.igatn.extranet.rest.documents.model.WsDocDefinition.WsListDocResponse;
import com.igatn.extranet.rest.documents.service.DocumentService;
import com.igatn.extranet.rest.exceptions.WsExternalApiNotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * ASGHIR - Documents, rest controller
 */
@Slf4j
@RestController
@RequestMapping(path = "/documents", produces = "application/json")
public class DocumentController {
    
    @Autowired
    DocumentService documentService;
    
    /**
     * test the controller
     *
     * @return
     */
    @GetMapping
    public String helloDocuments() {
        log.info("/documents endpoint called!");

        return "Hello from /documents";
    }


    @GetMapping(path = "/getAll")
    public ResponseEntity<WsListDocResponse> all(
            @RequestParam(value = "language", required = true, defaultValue = "fr") String languageIsoCode,
            @RequestParam(value = "skip", required = true, defaultValue = "0") Integer indexFrom,
            @RequestParam(value = "take", required = true, defaultValue = "0") Integer indexTo
    )  {

        log.info("-- /documents/getAll endpoint called! --");
        
        try {
            WsListDocParams params = WsListDocParams.builder()
                    .language(languageIsoCode)
                    .indexTo(indexTo)
                    .indexFrom(indexFrom)
                    .build();
            
            WsListDocResponse wsListDocResponse = documentService.getAll(params);
            
            ResponseEntity<WsListDocResponse> response = new ResponseEntity<>(wsListDocResponse, HttpStatus.OK);

            log.info("Sending response to client-side..: ".concat(response.getBody().toString()));

            return response;
        } catch (WsExternalApiNotFoundException e) {
            e.printStackTrace();
        }

        return new ResponseEntity<>(new WsListDocResponse(), HttpStatus.NO_CONTENT);
    }
}

