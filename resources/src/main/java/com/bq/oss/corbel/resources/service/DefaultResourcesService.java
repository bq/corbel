package com.bq.oss.corbel.resources.service;

import com.bq.oss.corbel.event.ResourceEvent;
import com.bq.oss.corbel.eventbus.service.EventBus;
import com.bq.oss.corbel.rem.internal.RemEntityTypeResolver;
import com.bq.oss.corbel.resources.rem.Rem;
import com.bq.oss.corbel.resources.rem.request.*;
import com.bq.oss.corbel.resources.rem.service.RemService;
import com.bq.oss.lib.queries.jaxrs.QueryParameters;
import com.bq.oss.lib.queries.parser.AggregationParser;
import com.bq.oss.lib.queries.parser.QueryParser;
import com.bq.oss.lib.queries.parser.SortParser;
import com.bq.oss.lib.token.TokenInfo;
import com.bq.oss.lib.ws.api.error.ApiRequestException;
import com.bq.oss.lib.ws.api.error.ErrorResponseFactory;
import com.bq.oss.lib.ws.auth.AuthorizationInfo;
import com.fasterxml.jackson.core.JsonParseException;
import com.google.common.collect.Lists;
import com.sun.jersey.api.core.HttpRequestContext;
import com.sun.jersey.server.impl.model.HttpHelper;
import com.sun.jersey.spi.container.ContainerRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.*;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.ext.MessageBodyReader;
import javax.ws.rs.ext.Providers;
import java.io.IOException;
import java.io.InputStream;
import java.lang.annotation.Annotation;
import java.net.URI;
import java.util.List;
import java.util.Optional;

/**
 * Created by Alexander De Leon on 26/05/15.
 */
public class DefaultResourcesService implements  ResourcesService{

    private static final Logger LOG = LoggerFactory.getLogger(DefaultResourcesService.class);
    private static final Annotation[] EMPTY_ANNOTATIONS = new Annotation[] {};

    private final RemService remService;
    private final RemEntityTypeResolver remEntityTypeResolver;
    private final int defaultPageSize;
    private final int maxPageSize;
    private final QueryParser queryParser;
    private final AggregationParser aggregationParser;
    private final EventBus eventBus;
    private final SortParser sortParser;

    private Providers providers;

    public DefaultResourcesService(RemService remService, RemEntityTypeResolver remEntityTypeResolver, int defaultPageSize, int maxPageSize,
                       QueryParser queryParser, AggregationParser aggregationParser, SortParser sortParser, EventBus eventBus) {
        this.remService = remService;
        this.remEntityTypeResolver = remEntityTypeResolver;
        this.defaultPageSize = defaultPageSize;
        this.maxPageSize = maxPageSize;
        this.queryParser = queryParser;
        this.aggregationParser = aggregationParser;
        this.sortParser = sortParser;
        this.eventBus = eventBus;
    }


    @Override
    public Response returnCollection(String type, Request request, UriInfo uriInfo, AuthorizationInfo authorizationInfo, URI typeUri,
                                      HttpMethod method, QueryParameters queryParameters, InputStream inputStream, MediaType contentType) {

        List<org.springframework.http.MediaType> acceptedMediaTypes = getRequestAcceptedMediaTypes(request);
        Rem rem = remService.getRem(type, acceptedMediaTypes, getRequestMethod(request));
        RequestParameters<CollectionParameters> parameters;
        Optional<?> entity;

        try {
            switch (method) {
                case GET:
                    parameters = collectionParameters(queryParameters, getTokenInfo(authorizationInfo), acceptedMediaTypes,
                            uriInfo.getQueryParameters(), request);
                    entity = Optional.empty();
                    break;

                case POST:
                    parameters = collectionParameters(getTokenInfo(authorizationInfo), acceptedMediaTypes, uriInfo.getQueryParameters(),
                            request);
                    entity = getEntity(Optional.ofNullable(inputStream), rem, contentType);
                    break;

                case DELETE:
                    parameters = collectionParameters(getTokenInfo(authorizationInfo), acceptedMediaTypes, uriInfo.getQueryParameters(),
                            request);
                    entity = Optional.empty();
                    break;

                default:
                    parameters = null;
                    entity = Optional.empty();
            }

            return remService.collection(rem, type, parameters, typeUri, entity);
        }

        catch (JsonParseException e) {
            return ErrorResponseFactory.getInstance().invalidEntity(e.getOriginalMessage());
        }

        catch (IOException e) {
            LOG.error("Unable to read entity from content of media type {}", contentType, e);
            return ErrorResponseFactory.getInstance().serverError(e);
        }

        catch (ApiRequestException e) {
            return ErrorResponseFactory.getInstance().badRequest(e);
        }
    }


    @Override
    public Response returnCollectionOrService(String type, ResourceId id, Request request, UriInfo uriInfo,
                                               AuthorizationInfo authorizationInfo, URI typeUri, HttpMethod method, InputStream inputStream, MediaType contentType,
                                               Long contentLength) {

        List<org.springframework.http.MediaType> acceptedMediaTypes = getRequestAcceptedMediaTypes(request);
        Rem rem = remService.getRem(type, acceptedMediaTypes, getRequestMethod(request));

        try {
            Optional<?> entity = method == HttpMethod.PUT ? getEntity(Optional.ofNullable(inputStream), rem, contentType) : Optional
                    .empty();

            if (id.isWildcard()) {
                return remService.collection(rem, type,
                        collectionParameters(getTokenInfo(authorizationInfo), acceptedMediaTypes, uriInfo.getQueryParameters(), request),
                        typeUri, entity);
            }

            return remService.resource(
                    rem,
                    type,
                    id,
                    //TODO! Query parameters
                    resourceParameters(null, getTokenInfo(authorizationInfo), acceptedMediaTypes, contentLength, uriInfo.getQueryParameters(),
                            request), entity);
        }

        catch (IOException e) {
            LOG.error("Unable to read entity from content of media type {}", contentType, e);
            return ErrorResponseFactory.getInstance().serverError(e);
        }

        catch (ApiRequestException e) {
            return ErrorResponseFactory.getInstance().badRequest(e);
        }
    }


    @Override
    public Response returnRelation(String type, ResourceId id, String rel, Request request, UriInfo uriInfo,
                                    AuthorizationInfo authorizationInfo, HttpMethod method, QueryParameters queryParameters, String resource,
                                    InputStream inputStream, MediaType contentType) {

        List<org.springframework.http.MediaType> acceptedMediaTypes = getRequestAcceptedMediaTypes(request);
        Rem rem = remService.getRem(type, acceptedMediaTypes, getRequestMethod(request));
        RequestParameters<RelationParameters> parameters;
        Optional<?> entity;

        try {
            switch (method) {
                case GET:
                    parameters = relationParameters(queryParameters, Optional.ofNullable(resource), getTokenInfo(authorizationInfo),
                            acceptedMediaTypes, uriInfo.getQueryParameters(), request);
                    entity = Optional.empty();
                    break;

                case PUT:
                    parameters = relationParameters(Optional.ofNullable(resource), getTokenInfo(authorizationInfo), acceptedMediaTypes,
                            uriInfo.getQueryParameters(), request);
                    entity = getEntity(Optional.ofNullable(inputStream), rem, contentType);
                    break;

                case DELETE:
                    parameters = relationParameters(Optional.ofNullable(resource), getTokenInfo(authorizationInfo), acceptedMediaTypes,
                            uriInfo.getQueryParameters(), request);
                    entity = Optional.empty();
                    break;

                default:
                    parameters = null;
                    entity = Optional.empty();
            }

            return remService.relation(rem, type, id, rel, parameters, entity);
        }

        catch (JsonParseException e) {
            return ErrorResponseFactory.getInstance().invalidEntity(e.getOriginalMessage());
        }

        catch (IOException e) {
            LOG.error("Unable to read entity from content of media type {}", contentType, e);
            return ErrorResponseFactory.getInstance().serverError(e);
        }

        catch (ApiRequestException e) {
            return ErrorResponseFactory.getInstance().badRequest(e);
        }
    }

    @Override
    public void notifyEvent(ResourceEvent resourceEvent) {
        eventBus.dispatch(resourceEvent);
    }

    @Override
    public void setProviders(Providers providers) {
        this.providers = providers;
    }


    private List<org.springframework.http.MediaType> getRequestAcceptedMediaTypes(Request request) {
        return Lists.transform(HttpHelper.getAccept((HttpRequestContext) request),
                input -> new org.springframework.http.MediaType(input.getType(), input.getSubtype(), input.getParameters()));
    }

    private org.springframework.http.HttpMethod getRequestMethod(Request request) {
        return org.springframework.http.HttpMethod.valueOf(request.getMethod());
    }

    private RequestParameters<CollectionParameters> collectionParameters(QueryParameters queryParameters, TokenInfo tokenInfo,
                                                                         List<org.springframework.http.MediaType> acceptedMediaTypes, MultivaluedMap<String, String> params, Request request) {
        return new RequestParametersImpl<>(new CollectionParametersImpl(queryParameters), tokenInfo, acceptedMediaTypes, null, params,
                getHeadersFromRequest(request));
    }

    private RequestParameters<CollectionParameters> collectionParameters(TokenInfo tokenInfo,
                                                                         List<org.springframework.http.MediaType> acceptedMediaTypes, MultivaluedMap<String, String> params, Request request) {
        return collectionParameters(new QueryParameters(defaultPageSize, 0, maxPageSize, Optional.empty(), Optional.empty(), queryParser,
                Optional.empty(), aggregationParser, sortParser, Optional.empty()), tokenInfo, acceptedMediaTypes, params, request);
    }

    private MultivaluedMap<String, String> getHeadersFromRequest(Request request) {
        MultivaluedMap<String, String> headers;

        if (request instanceof ContainerRequest) {
            headers = ((ContainerRequest) request).getRequestHeaders();
        } else {
            LOG.warn("Request is not instance of ContainerRequest");
            headers = new MultivaluedHashMap<>();
        }
        return headers;
    }

    private TokenInfo getTokenInfo(AuthorizationInfo authorizationInfo) {
        return authorizationInfo != null ? authorizationInfo.getTokenReader().getInfo() : null;
    }


    @SuppressWarnings({"rawtypes", "unchecked"})
    private Optional<?> getEntity(Optional<InputStream> entityStream, Rem<?> rem, MediaType mediaType) throws WebApplicationException,
            IOException {
        if (entityStream.isPresent()) {
            Class type = remEntityTypeResolver.getEntityType(rem);
            MessageBodyReader<?> reader = providers.getMessageBodyReader(type, type, EMPTY_ANNOTATIONS, mediaType);
            if (reader != null) {
                return Optional.ofNullable(reader.readFrom(type, type, EMPTY_ANNOTATIONS, mediaType, null, entityStream.get()));
            }
            LOG.warn("Did not find a provider for type {} and mediaType {}", type, mediaType);
        }
        return Optional.empty();
    }
    private RequestParameters<RelationParameters> relationParameters(QueryParameters queryParameters,
                                                                     Optional<String> predicateResourceUri, TokenInfo tokenInfo, List<org.springframework.http.MediaType> acceptedMediaTypes,
                                                                     MultivaluedMap<String, String> params, Request request) {

        return new RequestParametersImpl<>(new RelationParametersImpl(queryParameters, predicateResourceUri), tokenInfo,
                acceptedMediaTypes, null, params, getHeadersFromRequest(request));
    }



    private RequestParameters<RelationParameters> relationParameters(Optional<String> predicateResourceUri, TokenInfo tokenInfo,
                                                                     List<org.springframework.http.MediaType> acceptedMediaTypes, MultivaluedMap<String, String> params, Request request) {
        return relationParameters(new QueryParameters(defaultPageSize, 0, maxPageSize, Optional.empty(), Optional.empty(), queryParser,
                        Optional.empty(), aggregationParser, sortParser, Optional.empty()), predicateResourceUri, tokenInfo, acceptedMediaTypes,
                params, request);
    }

    private RequestParameters<ResourceParameters> resourceParameters(QueryParameters queryParameters, TokenInfo tokenInfo,
                                                                     List<org.springframework.http.MediaType> acceptedMediaTypes, Long contentLength, MultivaluedMap<String, String> params,
                                                                     Request request) {
        return new RequestParametersImpl<>(new ResourceParametersImpl(queryParameters.getQueries()), tokenInfo, acceptedMediaTypes, contentLength, params,
                getHeadersFromRequest(request));
    }



}
