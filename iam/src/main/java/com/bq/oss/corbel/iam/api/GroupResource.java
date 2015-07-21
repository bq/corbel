package com.bq.oss.corbel.iam.api;

import java.util.Collections;

import javax.validation.Valid;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import com.bq.oss.corbel.iam.exception.GroupAlreadyExistsException;
import com.bq.oss.corbel.iam.exception.NoGroupException;
import com.bq.oss.corbel.iam.model.Group;
import com.bq.oss.corbel.iam.service.GroupService;
import com.bq.oss.lib.queries.jaxrs.QueryParameters;
import com.bq.oss.lib.ws.annotation.Rest;
import com.bq.oss.lib.ws.auth.AuthorizationInfo;
import io.dropwizard.auth.Auth;

@Path(ApiVersion.CURRENT + "/group") public class GroupResource {

    private final GroupService groupService;

    public GroupResource(GroupService groupService) {
        this.groupService = groupService;
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response getAllGroups(@Auth AuthorizationInfo authorizationInfo, @Rest QueryParameters queryParameters) {

        return Response
                .ok(groupService.getAll(authorizationInfo.getDomainId(), queryParameters.getQueries().orElseGet(Collections::emptyList),
                        queryParameters.getPagination(), queryParameters.getSort().orElse(null)))
                .build();
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    public Response createGroup(@Context UriInfo uriInfo, @Auth AuthorizationInfo authorizationInfo, @Valid Group group) {
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
    public Response getGroup(@PathParam("id") final String id, @Auth AuthorizationInfo authorizationInfo) {
        return groupService.get(id, authorizationInfo.getDomainId()).map(group -> Response.ok(group).build())
                .orElseGet(() -> IamErrorResponseFactory.getInstance().groupNotExists(id));
    }

    @PUT
    @Path("/{id}")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response updateGroup(@PathParam("id") final String id, @Auth AuthorizationInfo authorizationInfo, @Valid Group group) {
        group.setId(id);
        group.setDomain(authorizationInfo.getDomainId());

        try {
            groupService.update(group);
        } catch (NoGroupException e) {
            return IamErrorResponseFactory.getInstance().groupNotExists(id);
        } catch (GroupAlreadyExistsException e) {
            return IamErrorResponseFactory.getInstance().groupAlreadyExists(e.getMessage());
        }
        return Response.noContent().build();
    }

    @DELETE
    @Path("/{id}")
    public Response deleteGroup(@PathParam("id") final String id, @Auth AuthorizationInfo authorizationInfo) {
        groupService.delete(id, authorizationInfo.getDomainId());
        return Response.noContent().build();
    }

}
