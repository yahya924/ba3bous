package com.igatn.extranet.rest.documents;

import com.igatn.extranet.domainjpa.impl.domain.ws.WsExternal;
import com.igatn.extranet.domainjpa.impl.domain.ws.WsExternal.WsDoc;
import com.igatn.extranet.domainjpa.impl.domain.ws.WsExternal.WsDoc.DocumentItem;
import com.igatn.extranet.domainjpa.impl.domain.ws.entities.ExternalWsApi;
import com.igatn.extranet.domainjpa.utils.SimpleDateFormatter;
import com.igatn.extranet.rest.documents.model.WsDocDefinition.WsListDocItem;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.time.Instant;

/**
 * FRE -
 */
final class DocTestUtils {

    /**
     * FRE - creates External Ws API 
     * @return
     */
    static ExternalWsApi createTestExternalWsApi() {
        // Preparing External API Insurance System persistence
        final String DATE_NOW = SimpleDateFormatter.formatDate(Instant.now());

        final String EXTERNAL_API_HOSTNAME = "192.168.216.17";
        final int EXTERNAL_API_PORT = 5700;
        final String EXTERNAL_API_MAIN_PATH = "/api";
        final String EXTERNAL_API_LABEL = "Insurance External System";
        final String EXTERNAL_API_PROTOCOL = WsExternal.PROTOCOL_HTTP;

        final ExternalWsApi EXTERNAL_API_1 = ExternalWsApi.externalWsApiBuilder()
                .label(EXTERNAL_API_LABEL)
                .protocol(EXTERNAL_API_PROTOCOL)
                .hostname(EXTERNAL_API_HOSTNAME)
                .port(EXTERNAL_API_PORT)
                .rootPath(EXTERNAL_API_MAIN_PATH)
                .createdAt(DATE_NOW)
                .create();

        return EXTERNAL_API_1;
    }

    /**
     * FRE - creates unformatted ws list doc item 
     * @param name
     * @param beneficiaryName
     * @return
     */
    static DocumentItem createTestExternalWsDocItem(String name, String beneficiaryName) {
        DocumentItem doc1External = new DocumentItem();
        doc1External.setId("");
        doc1External.setName(name);
        doc1External.setBeneficiaryName(beneficiaryName);
        doc1External.setUrl("");

        return doc1External;
    }
    
    /**
     * FRE - creates formatted ws list doc item 
     * @param externalDocItem
     * @param id
     * @return
     */
    static WsListDocItem createTestInternalWsDocItem(DocumentItem externalDocItem, int id) {
        WsListDocItem internalDocItem = new WsListDocItem();
        internalDocItem.setId(id);
        internalDocItem.setName(externalDocItem.getName());
        internalDocItem.setBeneficiaryName(externalDocItem.getBeneficiaryName());
        internalDocItem.setUrl("https://www.peipotato.org/sites/default/files/2017-10/Test%20PDF.pdf");
        return internalDocItem;
    }

    static URI createsTestExternalWsDocsList(ExternalWsApi EXTERNAL_WS_API_1, String LANGUAGE_CODE, int INDEX_FROM, int INDEX_TO) {
        URI wsUri = UriComponentsBuilder
                .fromUri(EXTERNAL_WS_API_1.getAsUri())
                .pathSegment(WsDoc.BASE_PATH, WsDoc.LIST_PATH)
                .queryParam(WsDoc.PARAM_LANGUAGE, LANGUAGE_CODE)
                .queryParam(WsDoc.PARAM_RESULT_FROM, INDEX_FROM)
                .queryParam(WsDoc.PARAM_RESULT_TO, INDEX_TO)
                .build()
                .toUri();
        return wsUri;
    }
}
