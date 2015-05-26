package com.bq.oss.corbel.resources.service;

import com.bq.oss.corbel.event.ResourceEvent;
import com.bq.oss.corbel.resources.rem.request.ResourceId;
import com.bq.oss.lib.queries.jaxrs.QueryParameters;
import com.bq.oss.lib.ws.auth.AuthorizationInfo;
import org.springframework.http.HttpMethod;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Request;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import javax.ws.rs.ext.Providers;
import java.io.InputStream;
import java.net.URI;

/**
 * Created by Francisco Sanchez on 26/05/15.
 */
public interface ResourcesService {

    Response returnCollection(String type, Request request, UriInfo uriInfo, AuthorizationInfo authorizationInfo, URI typeUri,
                                     HttpMethod method, QueryParameters queryParameters, InputStream inputStream, MediaType contentType);

    Response returnCollectionOrService(String type, ResourceId id, Request request, UriInfo uriInfo,
                                       AuthorizationInfo authorizationInfo, URI typeUri, HttpMethod method, InputStream inputStream, MediaType contentType,
                                       Long contentLength);
    Response returnRelation(String type, ResourceId id, String rel, Request request, UriInfo uriInfo,
                            AuthorizationInfo authorizationInfo, HttpMethod method, QueryParameters queryParameters, String resource,
                            InputStream inputStream, MediaType contentType);

    void notifyEvent(ResourceEvent resourceEvent);

    void setProviders(Providers providers);
}
