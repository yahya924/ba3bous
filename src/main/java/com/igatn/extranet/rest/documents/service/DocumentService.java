package com.igatn.extranet.rest.documents.service;

import com.igatn.extranet.rest.documents.model.WsDocDefinition.WsListDocParams;
import com.igatn.extranet.rest.documents.model.WsDocDefinition.WsListDocResponse;
import com.igatn.extranet.rest.exceptions.WsExternalApiNotFoundException;

/**
 * FRE - document rest service
 */
public interface DocumentService {

    WsListDocResponse getAll(WsListDocParams params) throws WsExternalApiNotFoundException;
}
