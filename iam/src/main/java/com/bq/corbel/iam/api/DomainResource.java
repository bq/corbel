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

@Path(ApiVersion.CURRENT + "/{domain}/domain") public class DomainResource {

    private final DomainService domainService;

    public DomainResource(DomainService domainService) {
        this.domainService = domainService;
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response getDomain(@PathParam("domain") String domainId) {
        return domainService.getDomain(domainId).map(domain -> Response.ok(domain).build())
                .orElseGet(() -> IamErrorResponseFactory.getInstance().notFound());
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    public Response createDomain(@PathParam("domain") String domainId, @Context UriInfo uriInfo,
                                 @Auth AuthorizationInfo authorizationInfo, Domain domain) {
        if (Strings.isNullOrEmpty(domain.getId())) {
            throw new ConstraintViolationException("Empty domain id", Collections.emptySet());
        }
        if (domain.getId().contains(Domain.ID_SEPARATOR)) {
            return IamErrorResponseFactory.getInstance().invalidEntity(
                    new Error("invalid_domain_id", Message.INVALID_DOMAIN_ID.getMessage()));
        }
        domain.setId(domainId + Domain.ID_SEPARATOR + domain.getId());
        addTrace(domain);
        try {
            domainService.insert(domain);
        } catch (DomainAlreadyExists domainAlreadyExists) {
            return IamErrorResponseFactory.getInstance().entityExists(Message.DOMAIN_EXISTS, domainAlreadyExists.getDomain());
        }
        return Response.created(uriInfo.getAbsolutePathBuilder().path(domain.getId()).build()).build();
    }

    @PUT
    @Consumes(MediaType.APPLICATION_JSON)
    public Response modifyDomain(@PathParam("domain") String domainId, Domain domain) {
        domain.setId(domainId);
        addTrace(domain);
        domainService.update(domain);
        return Response.noContent().build();
    }

    @DELETE
    public Response deleteDomain(@PathParam("domain") String domain) {
        domainService.delete(domain);
        return Response.noContent().build();
    }

    @GET
    @Path("/all")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getAllDomains(@PathParam("domain") String domainId, @Rest QueryParameters queryParameters) {

        ResourceQuery query = new ResourceQueryBuilder(queryParameters.getQuery().orElse(null))
                .add("id", generateDomainInIdRegex(domainId), QueryOperator.$LIKE).build();

        return queryParameters.getAggregation().map(aggregation -> {
            try {
                JsonElement result = domainService.getDomainsAggregation(query, aggregation);
                return Response.ok(result).build();

            } catch (InvalidAggregationException e) {
                return ErrorResponseFactory.getInstance().badRequest();
            }
        }).orElseGet(() -> {
            Pagination pagination = queryParameters.getPagination();
            Sort sort = queryParameters.getSort().orElse(null);
            return Response.ok(domainService.getAll(query, pagination, sort)).build();
        });
    }

    private String generateDomainInIdRegex(String domainId) {
        return "^" + domainId + "(" + Domain.ID_SEPARATOR + ".*)?";
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