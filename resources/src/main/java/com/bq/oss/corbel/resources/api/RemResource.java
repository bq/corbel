package com.bq.oss.corbel.resources.api;

import java.io.InputStream;
import java.lang.annotation.Annotation;
import java.net.URI;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import javax.ws.rs.*;
import javax.ws.rs.core.*;
import javax.ws.rs.ext.Providers;

import com.bq.oss.corbel.resources.service.ResourcesService;
import org.eclipse.jetty.http.HttpStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpMethod;

import com.bq.oss.corbel.event.ResourceEvent;
import com.bq.oss.corbel.resources.href.LinksFilter;
import com.bq.oss.corbel.resources.rem.request.*;
import com.bq.oss.lib.queries.jaxrs.QueryParameters;
import com.bq.oss.lib.ws.annotation.Rest;
import com.bq.oss.lib.ws.auth.AuthorizationInfo;
import com.sun.jersey.spi.container.ContainerRequest;

/**
 * Entry point for any resource on the resources. Here we obtain the appropiate Resource Resolver Module (REM) and delegate on it the
 * resolution of the resource's representation.
 * 
 * 
 * @author Alexander De Leon
 * 
 */
@Resource @Path(ApiVersion.CURRENT + "/resource") public class RemResource {

    private static final Logger LOG = LoggerFactory.getLogger(RemResource.class);
    private static final Annotation[] EMPTY_ANNOTATIONS = new Annotation[] {};
    private final ResourcesService resourcesService;

    // injected when the resource is registered with the container
    @Context Providers providers;

    public RemResource(ResourcesService resourcesService) {
        this.resourcesService = resourcesService;
    }

    private void updateRequestWithLinksTypeAndUri(Request request, URI typeUri, String type) {
        ContainerRequest containerRequest = (ContainerRequest) request;
        containerRequest.getProperties().put(LinksFilter.TYPE, type);
        containerRequest.getProperties().put(LinksFilter.URI, typeUri);
    }

    @GET
    @Path("/{type}")
    public Response getCollection(@PathParam("type") String type, @Context Request request, @Context UriInfo uriInfo,
            @Context AuthorizationInfo authorizationInfo, @Rest QueryParameters queryParameters) {

        URI typeUri = getTypeUri(type, uriInfo);
        updateRequestWithLinksTypeAndUri(request, typeUri, type);
        resourcesService.setProviders(providers);
        return resourcesService.returnCollection(type, request, uriInfo, authorizationInfo, typeUri, HttpMethod.GET, queryParameters, null, null);
    }

    @POST
    @Path("/{type}")
    public Response postCollection(@PathParam("type") String type, @Context Request request, @Context UriInfo uriInfo,
            @Context AuthorizationInfo authorizationInfo, InputStream inputStream, @HeaderParam("Content-Type") MediaType contentType) {
        resourcesService.setProviders(providers);
        return resourcesService.returnCollection(type, request, uriInfo, authorizationInfo, getTypeUri(type, uriInfo), HttpMethod.POST, null, inputStream,
                contentType);
    }

    @DELETE
    @Path("/{type}")
    public Response deleteCollection(@PathParam("type") String type, @Context Request request, @Context UriInfo uriInfo,
            @Context AuthorizationInfo authorizationInfo) {
        resourcesService.setProviders(providers);
        return resourcesService.returnCollection(type, request, uriInfo, authorizationInfo, getTypeUri(type, uriInfo), HttpMethod.DELETE, null, null, null);
    }



    @GET
    @Path("/{type}/{id}")
    public Response getResource(@PathParam("type") String type, @PathParam("id") ResourceId id, @Context Request request,
            @Context UriInfo uriInfo, @Context AuthorizationInfo authorizationInfo) {
        resourcesService.setProviders(providers);
        URI typeUri = getTypeUri(type, uriInfo);
        updateRequestWithLinksTypeAndUri(request, typeUri, type);
        return resourcesService.returnCollectionOrService(type, id, request, uriInfo, authorizationInfo, typeUri, HttpMethod.GET, null, null, null);
    }

    @PUT
    @Path("/{type}/{id}")
    public Response putResource(@PathParam("type") String type, @PathParam("id") ResourceId id, @Context Request request,
            @Context UriInfo uriInfo, @Context AuthorizationInfo authorizationInfo, InputStream inputStream,
            @HeaderParam("Content-Type") MediaType contentType, @HeaderParam("Content-Length") Long contentLength,
            @Rest QueryParameters queryParameters) {

        Response result = resourcesService.returnCollectionOrService(type, id, request, uriInfo, authorizationInfo, getTypeUri(type, uriInfo),
                HttpMethod.PUT, inputStream, contentType, contentLength);
        if (authorizationInfo != null && (result.getStatus() == HttpStatus.NO_CONTENT_204 || result.getStatus() == HttpStatus.OK_200)) {
            resourcesService.notifyEvent(ResourceEvent.updateResourceEvent(type, id.getId(), authorizationInfo.getDomainId()));
        }
        return result;
    }

    @DELETE
    @Path("/{type}/{id}")
    public Response deleteResource(@PathParam("type") String type, @PathParam("id") ResourceId id, @Context Request request,
            @Context UriInfo uriInfo, @Context AuthorizationInfo authorizationInfo) {
        return resourcesService.returnCollectionOrService(type, id, request, uriInfo, authorizationInfo, getTypeUri(type, uriInfo), HttpMethod.DELETE, null,
                null, null);
    }



    @GET
    @Path("/{type}/{id}/{rel}")
    public Response getRelation(@PathParam("type") String type, @PathParam("id") ResourceId id, @PathParam("rel") String rel,
            @Context Request request, @Context UriInfo uriInfo, @Context AuthorizationInfo authorizationInfo,
            @Rest QueryParameters queryParameters, @MatrixParam("r") String resource) {

        URI typeUri = getBaseUri(uriInfo);
        updateRequestWithLinksTypeAndUri(request, typeUri, type);
        return resourcesService.returnRelation(type, id, rel, request, uriInfo, authorizationInfo, HttpMethod.GET, queryParameters, resource, null, null);
    }

    @PUT
    @Path("/{type}/{id}/{rel}")
    public Response putRelation(@PathParam("type") String type, @PathParam("id") ResourceId id, @PathParam("rel") String rel,
            @Context Request request, @Context UriInfo uriInfo, @Context AuthorizationInfo authorizationInfo,
            @MatrixParam("r") String resource, InputStream inputStream, @HeaderParam("Content-Type") MediaType contentType) {

        return resourcesService.returnRelation(type, id, rel, request, uriInfo, authorizationInfo, HttpMethod.PUT, null, resource, inputStream, contentType);
    }

    @DELETE
    @Path("/{type}/{id}/{rel}")
    public Response deleteRelation(@PathParam("type") String type, @PathParam("id") ResourceId id, @PathParam("rel") String rel,
            @Context Request request, @Context UriInfo uriInfo, @Context AuthorizationInfo authorizationInfo,
            @MatrixParam("r") String resource) {

        return resourcesService.returnRelation(type, id, rel, request, uriInfo, authorizationInfo, HttpMethod.PUT, null, resource, null, null);
    }

    private URI getBaseUri(UriInfo uriInfo) {
        return uriInfo.getBaseUriBuilder().path(this.getClass()).build();
    }

    private URI getTypeUri(String type, UriInfo uriInfo) {
        return uriInfo.getBaseUriBuilder().path(this.getClass()).path("{type}").build(type);
    }

}
