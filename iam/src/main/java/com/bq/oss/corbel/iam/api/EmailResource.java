package com.bq.oss.corbel.iam.api;

import com.bq.oss.corbel.iam.model.User;
import com.bq.oss.corbel.iam.service.UserService;
import com.bq.oss.lib.ws.auth.AuthorizationInfo;
import io.dropwizard.auth.Auth;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.Optional;

@Path(ApiVersion.CURRENT + "/email")
public class EmailResource {

    private final UserService userService;

    public EmailResource(UserService userService) {
        this.userService = userService;
    }

    @GET
    @Path("/{email}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getUserIdByEmail(@PathParam("email") String userEmail, @Auth AuthorizationInfo authorizationInfo) {
        try {
            User user = getUserResolvingMeAndUserDomainVerifyingEmail(userEmail, authorizationInfo);
            return Response.ok(user.getUserWithOnlyId()).build();
        } catch (WebApplicationException i) {
            return Response.status(Response.Status.NOT_FOUND).entity(new com.bq.oss.lib.ws.model.Error("not_found", "No user with such email")).type(MediaType.APPLICATION_JSON_TYPE).build();
        }
    }

    private User getUserResolvingMeAndUserDomainVerifyingEmail(String userEmail, AuthorizationInfo authorizationInfo) {
        return Optional.ofNullable(userService.findByDomainAndEmail(authorizationInfo.getDomainId(), userEmail)).orElseThrow(
                () -> new WebApplicationException(IamErrorResponseFactory.getInstance().notFound()));
    }
}
