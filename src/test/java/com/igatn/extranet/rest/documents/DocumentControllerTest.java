package com.igatn.extranet.rest.documents;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.igatn.extranet.domainjpa.api.data.ws.ExternalWsApiRepository;
import com.igatn.extranet.domainjpa.impl.domain.ws.WsExternal.WsDoc.DocumentItem;
import com.igatn.extranet.domainjpa.impl.domain.ws.WsExternal.WsDoc.WsListDocsFormat;
import com.igatn.extranet.domainjpa.impl.domain.ws.entities.ExternalWsApi;
import com.igatn.extranet.rest.documents.model.WsDocDefinition.WsListDocItem;
import com.igatn.extranet.rest.documents.model.WsDocDefinition.WsListDocResponse;
import com.igatn.extranet.rest.documents.service.DocumentService;
import com.igatn.extranet.rest.documents.service.DocumentServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.autoconfigure.security.servlet.ManagementWebSecurityAutoConfiguration;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.client.ExpectedCount;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.context.WebApplicationContext;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static com.igatn.extranet.rest.documents.DocTestUtils.createTestExternalWsDocItem;
import static com.igatn.extranet.rest.documents.DocTestUtils.createTestInternalWsDocItem;
import static com.igatn.extranet.rest.documents.DocTestUtils.createsTestExternalWsDocsList;
import static org.hamcrest.Matchers.hasSize;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withStatus;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * FRE - Testing Insurance system external REST API: List Documents
 */
@Slf4j
@Profile("test")
@SpringBootTest
@ExtendWith(SpringExtension.class)
@EnableAutoConfiguration(exclude = {
        SecurityAutoConfiguration.class,
        ManagementWebSecurityAutoConfiguration.class
})
class DocumentControllerTest {
    
    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    DocumentService documentService = new DocumentServiceImpl();

    @Autowired
    private WebApplicationContext webApplicationContext;
    private MockMvc mockMvc;
    
    @MockBean
    ExternalWsApiRepository externalWsApiRepository;
    
    private MockRestServiceServer mockServer;
    
    private ObjectMapper objectMapper = new ObjectMapper();
    
    @BeforeEach
    public void init() {
        mockServer = MockRestServiceServer.createServer(restTemplate);
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
    }

    /**
     * (in progress) FRE - Stuck here while trying to use stream
     * @throws Exception
     */
    @DisplayName("testing app service list docs")
    @Test
    public void getDocsListAndSendIt() throws Exception {

        final ExternalWsApi EXTERNAL_WS_API_1 = DocTestUtils.createTestExternalWsApi();

        // mocking persistence used inside document service bean
        when(
            externalWsApiRepository.findByLabelContains("Insurance External System")
        )
        .thenReturn(Optional.ofNullable(EXTERNAL_WS_API_1));
        
        // Expected Doc items from WS External API
        DocumentItem doc1External = createTestExternalWsDocItem("Document 1", "Mark Doe");
        DocumentItem doc2External = createTestExternalWsDocItem("Document 2", "Kim Doe");

        // Expected objects from our WS API
        WsListDocItem doc1Internal = createTestInternalWsDocItem(doc1External,10);
        WsListDocItem doc2Internal = createTestInternalWsDocItem(doc2External,11);
        
        // Expected list from ws External API
        List<DocumentItem> documentItemsExternal = new ArrayList<>();
        documentItemsExternal.add(doc1External);
        documentItemsExternal.add(doc2External);
        
        // Expected list from our ws API
        List<WsListDocItem> documentItemsInternal = new ArrayList<>();
        documentItemsInternal.add(doc1Internal);
        documentItemsInternal.add(doc2Internal);

        // Expected results (response definition)
        WsListDocsFormat wsListDocsExternalFormat = new WsListDocsFormat();
        wsListDocsExternalFormat.setItems(documentItemsExternal);
        wsListDocsExternalFormat.setTotalResults(documentItemsExternal.size());
        
        // preparing uri with service path and parameters
        final String LANGUAGE_CODE = "fr";
        final int INDEX_FROM = 0;
        final int INDEX_TO = 5;

        URI wsUri = createsTestExternalWsDocsList(EXTERNAL_WS_API_1, LANGUAGE_CODE, INDEX_FROM, INDEX_TO);

        // mock external rest API
        mockServer
            .expect(ExpectedCount.once(), requestTo(wsUri))
            .andExpect(method(HttpMethod.GET))
            .andRespond(
                withStatus(HttpStatus.OK)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(objectMapper.writeValueAsString(wsListDocsExternalFormat))
            );
        
        // test Endpoint
        String retrievedResponseEntity = mockMvc
            .perform(
                get("/documents/getAll")
                    .queryParam("language", LANGUAGE_CODE)
                    .queryParam("skip", String.valueOf(INDEX_FROM))
                    .queryParam("take", String.valueOf(INDEX_TO))
            )
            .andDo(print())
            .andExpect(jsonPath("$.documents", hasSize(2)))
            .andExpect(jsonPath("$.totalResults").value(2))
            .andExpect(status().isOk())
            .andReturn()
            .getResponse()
            .getContentAsString();
        
        WsListDocResponse retrievedResponse = objectMapper.readValue(retrievedResponseEntity, WsListDocResponse.class);
        
        mockServer.verify();
        
        // result expected from our API
        WsListDocResponse wsListDocInternalFormat = new WsListDocResponse();
        wsListDocInternalFormat.setDocuments(documentItemsInternal);
        wsListDocInternalFormat.setTotalResults(documentItemsInternal.size());
        
        // assertion(s)
        Assertions.assertAll(
                () -> Assertions.assertEquals(retrievedResponse, wsListDocInternalFormat)
        );
    }
}