package com.bq.oss.corbel.iam.api;

import java.util.Collections;
import java.util.List;

import javax.validation.Valid;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import com.bq.oss.corbel.iam.exception.GroupAlreadyExistsException;
import com.bq.oss.corbel.iam.model.Group;
import com.bq.oss.corbel.iam.service.GroupService;

import io.corbel.lib.queries.jaxrs.QueryParameters;
import io.corbel.lib.ws.annotation.Rest;
import io.corbel.lib.ws.auth.AuthorizationInfo;
import io.dropwizard.auth.Auth;

@Path(ApiVersion.CURRENT + "/group") public class GroupResource {

    private final GroupService groupService;

    public GroupResource(GroupService groupService) {
        this.groupService = groupService;
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response getAll(@Auth AuthorizationInfo authorizationInfo, @Rest QueryParameters queryParameters) {

        return Response
                .ok(groupService.getAll(authorizationInfo.getDomainId(), queryParameters.getQueries().orElseGet(Collections::emptyList),
                        queryParameters.getPagination(), queryParameters.getSort().orElse(null)))
                .build();
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    public Response create(@Context UriInfo uriInfo, @Auth AuthorizationInfo authorizationInfo, @Valid Group group) {
        group.setDomain(authorizationInfo.getDomainId());

        try {
            return Response.created(uriInfo.getAbsolutePathBuilder().path(groupService.create(group).getId()).build()).build();
        } catch (GroupAlreadyExistsException e) {
            return IamErrorResponseFactory.getInstance().groupAlreadyExists(e.getMessage());
        }
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/{id}")
    public Response get(@PathParam("id") final String id, @Auth AuthorizationInfo authorizationInfo) {
        return groupService.get(id, authorizationInfo.getDomainId()).map(group -> Response.ok(group).build())
                .orElseGet(() -> IamErrorResponseFactory.getInstance().groupNotExists(id));
    }

    @PUT
    @Path("/{id}/addScopes")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response addScopes(@PathParam("id") final String id, @Auth AuthorizationInfo authorizationInfo, List<String> scopes) {
        String domain = authorizationInfo.getDomainId();

        return groupService.get(id).map(group -> {
            if (!group.getDomain().equals(domain)) {
                return IamErrorResponseFactory.getInstance().unauthorizedGroupUpdate(id);
            }

            groupService.addScopes(id, scopes);
            return Response.noContent().build();
        }).orElseGet(() -> IamErrorResponseFactory.getInstance().groupNotExists(id));
    }

    @PUT
    @Path("/{id}/removeScopes")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response removeScopes(@PathParam("id") final String id, @Auth AuthorizationInfo authorizationInfo, List<String> scopes) {
        String domain = authorizationInfo.getDomainId();

        return groupService.get(id).map(group -> {
            if (!group.getDomain().equals(domain)) {
                return IamErrorResponseFactory.getInstance().unauthorizedGroupUpdate(id);
            }

            groupService.removeScopes(id, scopes);
            return Response.noContent().build();
        }).orElseGet(() -> IamErrorResponseFactory.getInstance().groupNotExists(id));
    }

    @DELETE
    @Path("/{id}")
    public Response deleteGroup(@PathParam("id") final String id, @Auth AuthorizationInfo authorizationInfo) {
        String domain = authorizationInfo.getDomainId();

        return groupService.get(id).map(group -> {
            if (!group.getDomain().equals(domain)) {
                return IamErrorResponseFactory.getInstance().unauthorizedGroupDeletion(id);
            }

            groupService.delete(id, domain);
            return Response.noContent().build();
        }).orElseGet(() -> Response.noContent().build());
    }

}
