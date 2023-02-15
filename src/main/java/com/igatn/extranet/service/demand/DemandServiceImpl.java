package com.igatn.extranet.service.demand;

import com.igatn.extranet.domainjpa.api.data.demand.DemandRepository;
import com.igatn.extranet.domainjpa.api.data.ws.ExternalWsApiRepository;
import com.igatn.extranet.domainjpa.api.data.ws.WsConfigRepository;
import com.igatn.extranet.domainjpa.impl.domain.attachment.Attachment;
import com.igatn.extranet.domainjpa.impl.domain.client.Client;
import com.igatn.extranet.domainjpa.impl.domain.user.User;
import com.igatn.extranet.domainjpa.impl.domain.ws.entities.ExternalWsApi;
import com.igatn.extranet.domainjpa.impl.domain.ws.entities.WsConfig;
import com.igatn.extranet.rest.exceptions.ClientNotFoundException;
import com.igatn.extranet.rest.exceptions.OccuredErrorException;
import com.igatn.extranet.rest.exceptions.ws.ExternalWsNoResponseException;
import com.igatn.extranet.rest.user.models.MissingDocsDefinition.ListAttachmentDocs;
import com.igatn.extranet.rest.user.models.MissingDocsDefinition.ListMissingDocs;
import com.igatn.extranet.rest.user.models.MissingDocsDefinition.WsMissingDocsInternal;
import com.igatn.extranet.rest.user.models.MissingDocsDefinition.WsMissingDocsResponse;
import com.igatn.extranet.rest.user.models.MissingDocsListParams;
import com.igatn.extranet.utils.RestUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * FRE - Demand service
 */
@Slf4j
@Service
public class DemandServiceImpl implements DemandService {

    private final DemandRepository demandRepository;
    
    private final ExternalWsApiRepository externalWsApiRepository;

    private final WsConfigRepository wsConfigRepository;
    
    private final RestUtils restUtils;

    public DemandServiceImpl(DemandRepository demandRepository, ExternalWsApiRepository externalWsApiRepository, WsConfigRepository wsConfigRepository, RestUtils restUtils) {
        this.demandRepository = demandRepository;
        this.externalWsApiRepository = externalWsApiRepository;
        this.wsConfigRepository = wsConfigRepository;
        this.restUtils = restUtils;
    }

    @Override
    public WsMissingDocsResponse getAll(MissingDocsListParams queryParams, User user) {

        try {

            Client client = user.getClient();

            ExternalWsApi externalWsApi = externalWsApiRepository.findById(client.getId()).
                orElseThrow(() -> new ClientNotFoundException("client not found!"));

            log.info("Found WS external API: " + externalWsApi.getLabel() + "!");


            Optional<WsConfig> wsMissingDocsListExternalConfig = wsConfigRepository.findByHostAndLabel(externalWsApi, "Insured missing documents");

            WsConfig wsMissingDocsExternalConfig = wsMissingDocsListExternalConfig.orElseThrow(
                () -> new OccuredErrorException("WS missing documents list config object doesn't exist!")
            );

            StringBuilder wsFullPathBuilder = new StringBuilder();
            wsFullPathBuilder.append(externalWsApi.getRootPath());
            wsFullPathBuilder.append(wsMissingDocsExternalConfig.getPath());
            String wsFullPath = wsFullPathBuilder.toString();

            log.info("Building URI to call WS  missing documents list");


            URI wsUri = UriComponentsBuilder.
                fromUri(externalWsApi.getAsUri())
                .replacePath(wsFullPath)
                .queryParam("type", queryParams.getType())
                .queryParam("skip", queryParams.getIndexFrom())
                .queryParam("take", queryParams.getIndexTo())
                .queryParamIfPresent("status", Optional.ofNullable(queryParams.getStatus()))
                .build()
                .toUri();

            log.info("WS missing Docs list URI is ready: " + wsUri.toString());

            log.info("Calling external WS... missing documents list");

            ResponseEntity<WsMissingDocsResponse> responseMissingDocs = restUtils.prepareGetWs(wsUri, WsMissingDocsResponse.class);

            HttpStatus wsExternalResponseStatus = responseMissingDocs.getStatusCode();

            if (wsExternalResponseStatus == HttpStatus.OK) {
                WsMissingDocsResponse responseBody = responseMissingDocs.getBody();

                Optional<WsMissingDocsResponse> externalWsResponseBody = Optional.ofNullable(responseBody);

                WsMissingDocsResponse externalWsResponse = externalWsResponseBody.orElseThrow(
                    () -> new OccuredErrorException("WS External Missing Documents list response body is invalid")
                );

                log.info("Formatting WS MissingDocs list data for client-side..");

                if (externalWsResponse.isSuccess()) {
                    DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy").withZone(ZoneId.of("GMT"));
                    List<WsMissingDocsInternal> missingDocsFormatted = externalWsResponse
                        .getItems()
                        .stream().map(item -> {
                            var missingDocsItem = new WsMissingDocsInternal();

                            missingDocsItem.setType(item.getType());
                            missingDocsItem.setEntityId(item.getEntityId());
                            missingDocsItem.setBeneficiaryFirstName(item.getBeneficiaryFirstName());
                            missingDocsItem.setBeneficiaryLastName(item.getBeneficiaryLastName());
                            missingDocsItem.setLabel(item.getLabel());
                            try {
                                missingDocsItem.setDate(LocalDateTime.parse(item.getDate()).format(dateTimeFormatter));
                            } catch (DateTimeParseException e) {
                                log.error("WS External MissingDocs list response is invalid: an error occur during parsing date");
                                e.printStackTrace();
                            }
                            missingDocsItem.setStatus(item.getStatus());


                            var documents = item.getMissingDocs();

                            var documentsList = new ArrayList<ListMissingDocs>();

                            if (documents != null && !documents.isEmpty()) {
                                for (var doc :
                                    documents
                                ) {
                                    var document = new ListMissingDocs();
                                    document.setId(doc.getId());
                                    document.setName(doc.getName());
                                    document.setMandatory(doc.getMandatory());
                                    documentsList.add(document);
                                }
                            }
                            missingDocsItem.setMissingDocs(documentsList);

                            List<Attachment> attachmentsList = demandRepository.findBySubjectLike(item.getEntityId());

                            if (!attachmentsList.isEmpty()) {


                                var attachments = attachmentsList.stream().map(obj -> {
                                    ListAttachmentDocs attachment = new ListAttachmentDocs();

                                    attachment.setId(obj.getId());
                                    attachment.setFileName(obj.getFileName());
                                    attachment.setFileUrl(obj.getFileUrl());
                                    attachment.setCreatedAt(dateTimeFormatter.format(obj.getCreatedAt()));

                                    return attachment;
                                }).toList();

                                missingDocsItem.setAttachments(attachments);
                            }

                            return (missingDocsItem);
                        }).toList();


                    var response = new WsMissingDocsResponse();
                    response.setSuccess(externalWsResponse.isSuccess());
                    response.setTotalResults(externalWsResponse.getTotalResults());
                    response.setItems(missingDocsFormatted);

                    log.info("WS External MissingDocs has a valid response: " + response.toString());

                    return response;

                } else {
                    throw new OccuredErrorException("WS External MissingDocs has returned an error msg");
                }


            } else {
                throw new ExternalWsNoResponseException("WS External Missing Docs list call didn't pass!");
            }

        } catch (Exception e) {
            throw new OccuredErrorException(e.getMessage());
        }

    }

}
