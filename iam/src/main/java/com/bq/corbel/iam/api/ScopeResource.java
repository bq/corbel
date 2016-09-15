package com.bq.corbel.iam.api;

import java.util.Arrays;
import java.util.Optional;

import javax.validation.Valid;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import com.bq.corbel.iam.exception.ScopeAbsentIdException;
import com.bq.corbel.iam.exception.ScopeNameException;
import com.bq.corbel.iam.model.Scope;
import com.bq.corbel.iam.service.ScopeService;

@Path(ApiVersion.CURRENT + "/{domain}/scope") public class ScopeResource {

    private final ScopeService scopeService;

    public ScopeResource(ScopeService scopeService) {
        this.scopeService = scopeService;
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    public final Response createScope(@PathParam("domain") String domainId, @Context UriInfo uriInfo, @Valid Scope scope) {
        try {
            /*
            //TODO: Fix login with scopes
            if (scope.getId().contains(Scope.ID_SEPARATOR)) {
                return IamErrorResponseFactory.getInstance().scopeIdNotAllowed(scope.getId());
            }
            scope.setId(domainId + Scope.ID_SEPARATOR + scope.getId());
            */
            scopeService.create(scope);
        } catch (ScopeAbsentIdException e) {
            return IamErrorResponseFactory.getInstance().missingParameter("id");
        } catch (ScopeNameException e) {
            return IamErrorResponseFactory.getInstance().scopeIdNotAllowed(scope.getId());
        }
        return Response.created(uriInfo.getAbsolutePathBuilder().path(scope.getId()).build()).build();
    }

    @GET
    @Path("/{scopeId}")
    @Produces(MediaType.APPLICATION_JSON)
    public final Response getScope(@PathParam("domain") String domain, @PathParam("scopeId") String scopeId) {
        /*
        //TODO: Fix login with scopes
        if(!scopeId.startsWith(domain + Scope.ID_SEPARATOR)){
            throw new WebApplicationException(IamErrorResponseFactory.getInstance().unauthorized("Scope domain mismatch"));
        }*/
        return Optional.ofNullable(scopeService.getScope(scopeId)).map(scope -> Response.ok(scope).build())
                .orElseGet(() -> IamErrorResponseFactory.getInstance().notFound());
    }

    @DELETE
    @Path("/{scopeId}")
    @Consumes(MediaType.APPLICATION_JSON)
    public final Response deleteScope(@PathParam("domain") String domain, @PathParam("scopeId") String scopeId) {
        /*
        //TODO: Fix login with scopes
        if(!scopeId.startsWith(domain + Scope.ID_SEPARATOR)){
            throw new WebApplicationException(IamErrorResponseFactory.getInstance().unauthorized("Scope domain mismatch"));
        }*/
        scopeService.delete(scopeId);
        return Response.noContent().build();
    }

    @GET
    @Path("/{scopeId}/expand")
    @Produces(MediaType.APPLICATION_JSON)
    public final Response expandScope(@PathParam("domain") String domain, @PathParam("scopeId") String scopeId) {
        return Optional.ofNullable(scopeService.expandScopes(Arrays.asList(scopeId)))
                .map(scopes -> Response.ok(scopes).build())
                .orElseGet(() -> IamErrorResponseFactory.getInstance().notFound());
    }
}
