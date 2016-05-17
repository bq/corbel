package com.bq.corbel.iam.api;

import com.google.gson.JsonElement;
import com.bq.corbel.iam.exception.ClientAlreadyExistsException;
import com.bq.corbel.iam.exception.DomainAlreadyExists;
import com.bq.corbel.iam.exception.InvalidAggregationException;
import com.bq.corbel.iam.model.Client;
import com.bq.corbel.iam.model.Domain;
import com.bq.corbel.iam.model.TraceableEntity;
import com.bq.corbel.iam.service.ClientService;
import com.bq.corbel.iam.service.DomainService;
import com.bq.corbel.iam.utils.Message;
import com.google.common.base.Strings;
import com.bq.corbel.lib.queries.QueryNodeImpl;
import com.bq.corbel.lib.queries.StringQueryLiteral;
import com.bq.corbel.lib.queries.builder.ResourceQueryBuilder;
import com.bq.corbel.lib.queries.jaxrs.QueryParameters;
import com.bq.corbel.lib.queries.request.Pagination;
import com.bq.corbel.lib.queries.request.QueryOperator;
import com.bq.corbel.lib.queries.request.ResourceQuery;
import com.bq.corbel.lib.queries.request.Sort;
import com.bq.corbel.lib.ws.annotation.Rest;
import com.bq.corbel.lib.ws.api.error.ErrorResponseFactory;
import com.bq.corbel.lib.ws.auth.AuthorizationInfo;
import com.bq.corbel.lib.ws.model.Error;
import io.dropwizard.auth.Auth;

import javax.annotation.Resource;
import javax.validation.ConstraintViolationException;
import javax.validation.Valid;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import java.net.Inet4Address;
import java.net.UnknownHostException;
import java.util.Collections;
import java.util.Date;
import java.util.Optional;

@Path(ApiVersion.CURRENT + "/{domain}/client")
public class ClientResource {
    private final ClientService clientService;
    private final DomainService domainService;

    public ClientResource(ClientService clientService, DomainService domainService) {
        this.clientService = clientService;
        this.domainService = domainService;
    }


    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response getClientsByDomain(@PathParam("domain") String domainId, @Rest QueryParameters queryParameters) {
        ResourceQuery query = queryParameters.getQuery().orElse(null);
        return queryParameters.getAggregation().map(aggregation -> {
                    try {
                        JsonElement result = clientService.getClientsAggregation(domainId, query, aggregation);
                        return Response.ok(result).build();
                    } catch (InvalidAggregationException e) {
                        return ErrorResponseFactory.getInstance().badRequest();
                    }
                }
        ).orElseGet(() -> {
            Pagination pagination = queryParameters.getPagination();
            Sort sort = queryParameters.getSort().orElse(null);
            return Response.ok(clientService.findClientsByDomain(domainId, query, pagination, sort)).build();
        });
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    public Response createClient(@Context UriInfo uriInfo, @Valid Client client, @PathParam("domain") String domainId) {
        Optional<Domain> domain = domainService.getDomain(domainId);
        if (!domain.isPresent()) {
            return IamErrorResponseFactory.getInstance().domainNotExists(domainId);
        }
        if (!domainService.scopesAllowedInDomain(client.getScopes(), domain.get())) {
            return IamErrorResponseFactory.getInstance().scopesNotAllowed(domainId);
        }
        client.setDomain(domainId);
        addTrace(client);
        try {
            clientService.createClient(client);
        } catch (ClientAlreadyExistsException e) {
            return IamErrorResponseFactory.getInstance().conflict(
                    new com.bq.corbel.lib.ws.model.Error("conflict", "The client already exists"));
        }
        return Response.created(uriInfo.getAbsolutePathBuilder().path(client.getId()).build()).build();
    }

    @GET
    @Path("/{clientId}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getClient(@PathParam("clientId") String clientId, @PathParam("domain") String domainId) {
        return clientService.find(clientId).filter(client -> client.getDomain().equals(domainId))
                .map(client -> Response.ok(client).build()).orElseGet(() -> IamErrorResponseFactory.getInstance().notFound());
    }

    @PUT
    @Path("/{clientId}")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response modifyClient(@Valid Client client, @PathParam("clientId") String clientId,
                                 @PathParam("domain") String domainId) {
        client.setDomain(domainId);
        client.setId(clientId);
        addTrace(client);
        clientService.update(client);
        return Response.noContent().build();
    }

    @DELETE
    @Path("/{clientId}")
    public Response deleteClient(@PathParam("clientId") String clientId, @PathParam("domain") String domainId) {
        clientService.delete(domainId, clientId);
        return Response.noContent().build();
    }

    private void addTrace(TraceableEntity entity) {
        String hostName;
        try {
            hostName = Inet4Address.getLocalHost().getHostName();
        } catch (UnknownHostException e) {
            hostName = "[error getting host name]";
        }
        entity.setCreatedBy("IamAPI on " + hostName);
        entity.setCreatedDate(new Date());
    }
}