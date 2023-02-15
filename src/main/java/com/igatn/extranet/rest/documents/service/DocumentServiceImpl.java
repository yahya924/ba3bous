package com.igatn.extranet.rest.documents.service;

import com.codepoetics.protonpack.Indexed;
import com.codepoetics.protonpack.StreamUtils;
import com.igatn.extranet.domainjpa.api.data.ws.ExternalWsApiRepository;
import com.igatn.extranet.domainjpa.impl.domain.ws.WsExternal.WsDoc;
import com.igatn.extranet.domainjpa.impl.domain.ws.WsExternal.WsDoc.WsListDocsFormat;
import com.igatn.extranet.domainjpa.impl.domain.ws.entities.ExternalWsApi;
import com.igatn.extranet.rest.documents.model.WsDocDefinition.WsListDocItem;
import com.igatn.extranet.rest.documents.model.WsDocDefinition.WsListDocParams;
import com.igatn.extranet.rest.documents.model.WsDocDefinition.WsListDocResponse;
import com.igatn.extranet.rest.exceptions.WsExternalApiNotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponents;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.util.List;
import java.util.stream.Collectors;

/**
 * FRE - Ws Docs internal service
 */
@Slf4j
@Service
public class DocumentServiceImpl implements DocumentService {

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private ExternalWsApiRepository externalWsApiRepository;

    @Override
    public WsListDocResponse getAll(WsListDocParams params) {

        // get External ws host from repository
        // FRE - TODO - Make users in relationship with client (require modeling)
        // FRE - TODO - make client in relationship with WS external API! (require modeling)
        final String INSURANCE_EXTERNAL_API = "Insurance External System";

        ExternalWsApi externalWsApi;

        try {
            externalWsApi = externalWsApiRepository.findByLabelContains(INSURANCE_EXTERNAL_API)
                    .orElseThrow(() -> new WsExternalApiNotFoundException(
                            String.format("No WS External API with label: \"%s\" is available", INSURANCE_EXTERNAL_API)));

            // Build URI
            UriComponents wsListDocsUriBuilder = UriComponentsBuilder.newInstance()
                    .scheme(externalWsApi.getProtocol())
                    .host(externalWsApi.getHostname())
                    .port(externalWsApi.getPort())
                    .path(externalWsApi.getRootPath())
                    .pathSegment(WsDoc.BASE_PATH, WsDoc.LIST_PATH)
                    .queryParam(WsDoc.PARAM_LANGUAGE, params.getLanguage())
                    .queryParam(WsDoc.PARAM_RESULT_FROM, params.getIndexFrom())
                    .queryParam(WsDoc.PARAM_RESULT_TO, params.getIndexTo())
                    .build();

            URI uri = wsListDocsUriBuilder.toUri();

            log.info("Processing query for thrid party API finished!");

            log.info("External WS Docs list URI: ".concat(uri.toString()));

            ResponseEntity<WsListDocsFormat> externalWsListDocResponse = restTemplate.exchange(
                    uri, HttpMethod.GET, null, WsListDocsFormat.class);

            log.info("External WS Docs list Response: ".concat(externalWsListDocResponse.getBody().toString()));

            log.info("Formatting data for client-side..");

            WsListDocResponse response = new WsListDocResponse();

            response.setTotalResults(externalWsListDocResponse.getBody().getTotalResults());

            // FRE - reorder documents as there is no IDs from External Insurance system now 
            List<WsListDocItem> finalDocuments = StreamUtils
                    .zipWithIndex(
                            externalWsListDocResponse
                                    .getBody()
                                    .getItems()
                                    .stream()
                    )
                    .map(di -> {
                        WsListDocItem wsListDocItem = new WsListDocItem();
                        wsListDocItem.setId(di.getIndex()+10);
                        wsListDocItem.setName(di.getValue().getName());
                        wsListDocItem.setBeneficiaryName(di.getValue().getBeneficiaryName());
                        // FRE - WARNING: this URL is to get empty pdf from the web
                        // it's a temporary solution, waiting for the update in external insurance system
                        wsListDocItem.setUrl("https://www.peipotato.org/sites/default/files/2017-10/Test%20PDF.pdf");

                        Indexed<WsListDocItem> documentItemIndexed = Indexed.index(di.getIndex(), wsListDocItem);

                        return documentItemIndexed;
                    })
                    .map(i -> i.getValue())
                    .collect(Collectors.toList());

            response.setDocuments(finalDocuments);

            return response;
        } catch (WsExternalApiNotFoundException e) {
            e.printStackTrace();
        }

        return null;
    }
}
