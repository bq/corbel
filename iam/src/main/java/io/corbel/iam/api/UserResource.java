package io.corbel.iam.api;

import com.google.gson.JsonElement;
import io.corbel.iam.exception.DuplicatedOauthServiceIdentityException;
import io.corbel.iam.exception.IdentityAlreadyExistsException;
import io.corbel.iam.exception.UserProfileConfigurationException;
import io.corbel.iam.model.*;
import io.corbel.iam.repository.CreateUserException;
import io.corbel.iam.service.DeviceService;
import io.corbel.iam.service.DomainService;
import io.corbel.iam.service.IdentityService;
import io.corbel.iam.service.UserService;
import io.corbel.iam.utils.Message;
import io.corbel.lib.queries.builder.ResourceQueryBuilder;
import io.corbel.lib.queries.jaxrs.QueryParameters;
import io.corbel.lib.queries.request.*;
import io.corbel.lib.ws.annotation.Rest;
import io.corbel.lib.ws.auth.AuthorizationInfo;
import io.corbel.lib.ws.model.Error;
import io.dropwizard.auth.Auth;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DuplicateKeyException;

import javax.validation.Valid;
import javax.ws.rs.*;
import javax.ws.rs.core.*;
import javax.ws.rs.core.Response.Status;
import java.net.URI;
import java.net.URISyntaxException;
import java.time.Clock;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author Alexander De Leon
 */
@Path(ApiVersion.CURRENT + "/user") public class UserResource {

    private static final Logger LOG = LoggerFactory.getLogger(UserResource.class);
    private static final String ME = "me";

    private final UserService userService;
    private final IdentityService identityService;
    private final DomainService domainService;
    private final Clock clock;
    private final DeviceService deviceService;
    private final AggregationResultsFactory<JsonElement> aggregationResultsFactory;

    public UserResource(UserService userService, DomainService domainService, IdentityService identityService, DeviceService deviceService,
                        AggregationResultsFactory<JsonElement> aggregationResultsFactory, Clock clock) {
        this.userService = userService;
        this.domainService = domainService;
        this.identityService = identityService;
        this.deviceService = deviceService;
        this.clock = clock;
        this.aggregationResultsFactory = aggregationResultsFactory;
    }

    @GET
    public Response getUsers(@Rest QueryParameters queryParameters, @Auth AuthorizationInfo authorizationInfo) {
        String domainId = authorizationInfo.getDomainId();
        ResourceQuery query = queryParameters.getQuery().orElse(null);
        Pagination pagination = queryParameters.getPagination();
        Sort sort = queryParameters.getSort().orElse(null);
        Aggregation aggregation = queryParameters.getAggregation().orElse(null);

        if (queryParameters.getAggregation().isPresent()) {
            return getUsersAggregation(domainId, query, aggregation);
        } else {
            List<User> users = userService.findUsersByDomain(domainId, query, pagination, sort).stream().map(User::getUserProfile)
                    .collect(Collectors.toList());
            return Response.ok().type(MediaType.APPLICATION_JSON).entity(users).build();
        }
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    public Response postUser(@Valid UserWithIdentity user, @Context UriInfo uriInfo, @Auth AuthorizationInfo authorizationInfo) {
        Optional<Domain> optDomain = domainService.getDomain(authorizationInfo.getDomainId());

        if (!optDomain.isPresent()) {
            return IamErrorResponseFactory.getInstance().invalidEntity(Message.NOT_FOUND.getMessage());
        }

        Domain domain = optDomain.get();

        user.setDomain(domain.getId());

        user.setScopes(domain.getDefaultScopes());

        setTracebleEntity(user, authorizationInfo);

        User createdUser;
        // The new user can only be on the domainId of the client making the request
        try {
            createdUser = userService.create(ensureNoId(user));
        } catch (CreateUserException duplicatedUser) {
            return IamErrorResponseFactory.getInstance().entityExists(Message.USER_EXISTS, duplicatedUser.getMessage());
        }

        Identity identity = user.getIdentity();
        if (identity != null) {
            try {
                addIdentity(identity);
            } catch (Exception e) {
                // Rollback user creation and handle error
                userService.delete(user);
                return handleIdentityError(e, identity);
            }
        }

        return Response.created(uriInfo.getAbsolutePathBuilder().path(createdUser.getId()).build()).build();
    }

    @PUT
    @Path("/{id}")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response updateUser(@PathParam("id") String userId, User userData, @Auth AuthorizationInfo authorizationInfo) {
        if (ME.equals(userId)) {
            userData.setScopes(null);
            userData.setGroups(null);
        }

        User user = getUserResolvingMeAndUserDomainVerifying(userId, authorizationInfo);

        if (userData != null) {

            user.updateUser(userData);

            Optional<Domain> optDomain = domainService.getDomain(authorizationInfo.getDomainId());

            if (!optDomain.isPresent()) {
                return IamErrorResponseFactory.getInstance().invalidEntity(Message.NOT_FOUND.getMessage());
            }

            Domain domain = optDomain.get();

            if (!domainService.scopesAllowedInDomain(user.getScopes(), domain)) {
                return IamErrorResponseFactory.getInstance().scopesNotAllowed(domain.getId());
            }
            try {
                userService.update(user);
            } catch (DuplicateKeyException e) {
                return IamErrorResponseFactory.getInstance().conflict(new Error("conflict", "The email or username already exists"));
            }
        }
        return Response.status(Status.NO_CONTENT).build();
    }

    @GET
    @Path("/{id}")
    public Response getUser(@PathParam("id") String userId, @Auth AuthorizationInfo authorizationInfo) {
        User user = getUserResolvingMeAndUserDomainVerifying(userId, authorizationInfo);
        return Response.ok().type(MediaType.APPLICATION_JSON).entity(user.getUserProfile()).build();
    }

    @GET
    @Path("/{id}/avatar")
    public Response getAvatar(@PathParam("id") String id, @Auth AuthorizationInfo authorizationInfo) {
        Optional<User> user = resolveMeIdAliases(id, authorizationInfo);

        if (!user.isPresent() || !userDomainMatchAuthorizationDomain(user.get(), authorizationInfo)) {
            return IamErrorResponseFactory.getInstance().notFound();
        }

        return user.map(User::getProperties).map(p -> p.get("avatar")).map(a -> {
            try {
                return new URI(a.toString());
            } catch (URISyntaxException e) {
                return null;
            }
        }).map(avatarUri -> Response.temporaryRedirect(avatarUri).build())
                .orElseGet(() -> IamErrorResponseFactory.getInstance().notfound(new Error("not_found", "User " + id + " has no avatar.")));
    }

    @GET
    @Path("/{userId}/devices")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getDevices(@Rest QueryParameters queryParameters, @PathParam("userId") String userId,
            @Auth AuthorizationInfo authorizationInfo) {
        User user = getUserResolvingMeAndUserDomainVerifying(userId, authorizationInfo);
        return Optional.ofNullable(deviceService.getByUserId(user.getId(), queryParameters))
                .map(devices -> {
                    devices.stream().forEach(device -> device.setId(device.getUid()));
                    return Response.ok().type(MediaType.APPLICATION_JSON).entity(devices).build();
                })
                .orElseGet(() -> IamErrorResponseFactory.getInstance().notFound());
    }

    @GET
    @Path("/{userId}/devices/{deviceId}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getDevice(@PathParam("userId") String userId, @PathParam("deviceId") String deviceUid,
            @Auth AuthorizationInfo authorizationInfo) {
        User user = getUserResolvingMeAndUserDomainVerifying(userId, authorizationInfo);
        return Optional.ofNullable(deviceService.getByUidAndUserId(deviceUid, user.getId(), user.getDomain()))
                .map(device -> {
                    device.setId(deviceUid);
                    return Response.ok().type(MediaType.APPLICATION_JSON).entity(device).build();
                })
                .orElseGet(() -> IamErrorResponseFactory.getInstance().notFound());
    }

    @PUT
    @Path("/{userId}/devices")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response updateDevice(@PathParam("userId") String userId, @Valid Device deviceData, @Auth AuthorizationInfo authorizationInfo,
            @Context UriInfo uriInfo) {
        User user = getUserResolvingMeAndUserDomainVerifying(userId, authorizationInfo);
        ensureNoId(deviceData);
        deviceData.setUserId(user.getId());
        deviceData.setDomain(authorizationInfo.getDomainId());
        Device storeDevice = deviceService.update(deviceData);
        return Response.created(uriInfo.getAbsolutePathBuilder().path(storeDevice.getUid()).build()).build();
    }

    @DELETE
    @Path("/{userId}/devices/{deviceId}")
    public Response deleteDevice(@PathParam("userId") String userId, @PathParam("deviceId") final String deviceUid,
            @Auth AuthorizationInfo authorizationInfo) {
        User user = getUserResolvingMeAndUserDomainVerifying(userId, authorizationInfo);
        deviceService.deleteByUidAndUserId(deviceUid, user.getId(), authorizationInfo.getDomainId());
        return Response.status(Status.NO_CONTENT).build();
    }

    @Path("/resetPassword")
    @GET
    public Response generateResetPasswordEmail(@QueryParam("email") String email, @Auth AuthorizationInfo authorizationInfo) {
        userService.sendMailResetPassword(email, authorizationInfo.getClientId(), authorizationInfo.getDomainId());
        return Response.noContent().build();
    }

    @PUT
    @Path("/me/signout")
    public Response signOut(@Auth AuthorizationInfo authorizationInfo) {
        return Optional.ofNullable(userService.findById(authorizationInfo.getUserId()))
                .filter(user -> userDomainMatchAuthorizationDomain(user, authorizationInfo)).map(user -> {
                    userService.signOut(user.getId(), Optional.of(authorizationInfo.getToken()));
                    return Response.noContent().build();
                }).orElseGet(() -> IamErrorResponseFactory.getInstance().notFound());
    }

    @GET
    @Path("/me/session")
    public Response getSession(@Auth AuthorizationInfo authorizationInfo){
        return Optional.ofNullable(userService.findById(authorizationInfo.getUserId()))
                .filter(user -> userDomainMatchAuthorizationDomain(user, authorizationInfo)).map(user -> {
                    return Response.ok()
                            .type(MediaType.APPLICATION_JSON)
                            .entity(userService.getSession(authorizationInfo.getToken()))
                            .build();
                }).orElseGet(() -> IamErrorResponseFactory.getInstance().notFound());
    }

    @PUT
    @Path("/{id}/disconnect")
    public Response disconnect(@PathParam("id") String userId, @Auth AuthorizationInfo authorizationInfo) {
        return resolveMeIdAliases(userId, authorizationInfo).filter(user -> userDomainMatchAuthorizationDomain(user, authorizationInfo))
                .map(user -> {
                    userService.signOut(user.getId()); // invalidate all user tokens
                    return Response.noContent().build();
                }).orElseGet(() -> IamErrorResponseFactory.getInstance().notFound());
    }

    @DELETE
    @Path("/{id}/sessions")
    public Response deleteAllSessions(@PathParam("id") String userId, @Auth AuthorizationInfo authorizationInfo) {
        return resolveMeIdAliases(userId, authorizationInfo).filter(user -> userDomainMatchAuthorizationDomain(user, authorizationInfo))
                .map(user -> {
                    userService.invalidateAllTokens(user.getId());
                    return Response.noContent().build();
                }).orElseGet(() -> IamErrorResponseFactory.getInstance().notFound());
    }

    @DELETE
    @Path("/{id}")
    public Response deleteUser(@PathParam("id") String userId, @Auth AuthorizationInfo authorizationInfo) {
        Optional<User> optionalUser = resolveMeIdAliases(userId, authorizationInfo);
        optionalUser.ifPresent(user -> {
            checkingUserDomain(user, authorizationInfo);
            identityService.deleteUserIdentities(user);
            userService.delete(user);
            deviceService.deleteByUserId(user);
        });
        return Response.noContent().build();
    }

    @POST
    @Path("/{id}/identity")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response postUserIdentity(@Valid Identity identity, @PathParam("id") String id, @Auth AuthorizationInfo authorizationInfo,
            @Context Request request) {
        User user = getUserResolvingMeAndUserDomainVerifying(id, authorizationInfo);

        identity.setDomain(authorizationInfo.getDomainId());
        identity.setUserId(user.getId());

        try {
            addIdentity(identity);
            return Response.status(Status.CREATED).build();
        } catch (Exception e) {
            return handleIdentityError(e, identity);
        }
    }

    @GET
    @Path("/{id}/identity")
    public Response getUserIdentity(@PathParam("id") String id, @Auth AuthorizationInfo authorizationInfo) {
        User user = getUserResolvingMeAndUserDomainVerifying(id, authorizationInfo);
        return Response.ok().type(MediaType.APPLICATION_JSON).entity(identityService.findUserIdentities(user)).build();
    }

    @GET
    @Path("/profile")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getUserProfiles(@Auth AuthorizationInfo authorizationInfo, @Rest QueryParameters queryParameters)
            throws UserProfileConfigurationException {

        Optional<Domain> optionalDomain = domainService.getDomain(authorizationInfo.getDomainId());

        ResourceQuery query = queryParameters.getQuery().orElse(new ResourceQuery());

        query = filterQuery(optionalDomain, query);

        if (queryParameters.getAggregation().isPresent()) {
            return getUsersAggregation(authorizationInfo.getDomainId(), query, queryParameters.getAggregation().get());
        }

        final ResourceQuery queryFinal = query;

        List<User> profiles = optionalDomain.map(
                domain -> {
                    try {
                        return userService.findUserProfilesByDomain(domain, queryFinal, queryParameters.getPagination(), queryParameters
                                .getSort().orElse(null));
                    } catch (UserProfileConfigurationException e) {
                        return new LinkedList<User>();
                    }
                }).orElseGet(LinkedList::new);

        return Response.ok(profiles).type(MediaType.APPLICATION_JSON).build();
    }

    @GET
    @Path("/{id}/profile")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getUserProfile(@PathParam("id") String id, @Auth AuthorizationInfo authorizationInfo) {
        User user = getUserResolvingMeAndUserDomainVerifying(id, authorizationInfo);
        Optional<Domain> domain = domainService.getDomain(authorizationInfo.getDomainId());

        if (!domain.isPresent()) {
            return IamErrorResponseFactory.getInstance().notFound();
        }

        try {
            return Optional.ofNullable(userService.getUserProfile(user, domain.get().getUserProfileFields()))
                    .map(userProfile -> Response.ok().type(MediaType.APPLICATION_JSON).entity(userProfile).build())
                    .orElseGet(() -> IamErrorResponseFactory.getInstance().notFound());
        } catch (UserProfileConfigurationException e) {
            return IamErrorResponseFactory.getInstance().serverError(new Error("misconfiguration", e.getMessage()));
        }
    }

    @PUT
    @Path("/{id}/groups")
    @Produces(MediaType.APPLICATION_JSON)
    public Response addGroupsToUser(@PathParam("id") String id, Set<String> groups, @Auth AuthorizationInfo authorizationInfo) {
        User user = getUserResolvingMeAndUserDomainVerifying(id, authorizationInfo);
        Optional<Domain> domain = domainService.getDomain(authorizationInfo.getDomainId());

        if (!domain.isPresent()) {
            return IamErrorResponseFactory.getInstance().notFound();
        }

        user.addGroups(groups);
        userService.update(user);
        return Response.noContent().build();
    }

    @DELETE
    @Path("/{id}/groups/{groupId}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response deleteGroupsToUser(@PathParam("id") String id, @PathParam("groupId") String groupId,
            @Auth AuthorizationInfo authorizationInfo) {
        User user = getUserResolvingMeAndUserDomainVerifying(id, authorizationInfo);
        Optional<Domain> domain = domainService.getDomain(authorizationInfo.getDomainId());

        if (!domain.isPresent()) {
            return IamErrorResponseFactory.getInstance().notFound();
        }

        user.deleteGroup(groupId);
        userService.update(user);
        return Response.noContent().build();
    }

    private <T extends Entity> T ensureNoId(T entity) {
        entity.setId(null);
        return entity;
    }

    private Response getUsersAggregation(String domainId, ResourceQuery query, Aggregation aggregation) {
        if (!AggregationOperator.$COUNT.equals(aggregation.getOperator())) {
            return IamErrorResponseFactory.getInstance().badRequest(
                    new Error("bad_request", "Aggregator" + aggregation.getOperator() + "not supported"));
        }
        JsonElement result = aggregationResultsFactory.countResult(userService.countUsersByDomain(domainId, query));
        return Response.ok().type(MediaType.APPLICATION_JSON).entity(result).build();
    }

    private Identity addIdentity(Identity identity) throws IllegalOauthServiceException, IdentityAlreadyExistsException,
            DuplicatedOauthServiceIdentityException {

        String domainId = Optional.ofNullable(identity.getDomain()).orElseThrow(
                () -> new IllegalArgumentException("Identity \"" + identity.getId() + "\" has no domain."));

        Optional<Domain> optDomain = domainService.getDomain(domainId);

        if (!optDomain.isPresent()) {
            throw new IllegalArgumentException("Domain \"" + domainId + "\" not exists.");
        }

        optDomain.map(domain -> domainService.oAuthServiceAllowedInDomain(identity.getOauthService(), domain))
                .map(oAuthServiceAllowedInDomain -> !oAuthServiceAllowedInDomain ? null : true)
                .orElseThrow(IllegalOauthServiceException::new);

        return identityService.addIdentity(identity);
    }

    private <T extends TraceableEntity> void setTracebleEntity(T entity, AuthorizationInfo authorizationInfo) {
        String sign = authorizationInfo.getClientId();
        if (authorizationInfo.getUserId() != null) {
            sign = authorizationInfo.getUserId() + "@" + sign;
        }
        entity.setCreatedBy(sign);
        entity.setCreatedDate(Date.from(clock.instant()));
    }

    private Optional<User> resolveMeIdAliases(String id, AuthorizationInfo authorizationInfo) {
        if (ME.equals(id)) {
            id = authorizationInfo.getUserId();
        }
        return Optional.ofNullable(userService.findById(id));
    }

    private boolean userDomainMatchAuthorizationDomain(User user, AuthorizationInfo authorizationInfo) {
        return Objects.equals(user.getDomain(), authorizationInfo.getDomainId());
    }

    private void checkingUserDomain(User user, AuthorizationInfo authorizationInfo) {
        if (!userDomainMatchAuthorizationDomain(user, authorizationInfo)) {
            throw new WebApplicationException(IamErrorResponseFactory.getInstance().unauthorized("User domain mismatch"));
        }
    }

    private User getUserResolvingMeAndUserDomainVerifying(String userId, AuthorizationInfo authorizationInfo) {
        User user = resolveMeIdAliases(userId, authorizationInfo).orElseThrow(
                () -> new WebApplicationException(IamErrorResponseFactory.getInstance().notFound()));
        checkingUserDomain(user, authorizationInfo);
        return user;
    }

    private Response handleIdentityError(Exception e, Identity identity) {
        if (e instanceof IllegalOauthServiceException) {
            return IamErrorResponseFactory.getInstance().invalidOAuthService(identity.getDomain());
        }
        if (e instanceof IdentityAlreadyExistsException) {
            return IamErrorResponseFactory.getInstance().identityExists(Message.IDENTITY_EXITS, identity.getOauthId(),
                    identity.getOauthService(), identity.getDomain());
        }
        if (e instanceof DuplicatedOauthServiceIdentityException) {
            return IamErrorResponseFactory.getInstance().oauthServiceDuplicated(Message.DUPLICATED_OAUTH_SERVICE_IDENTITY,
                    identity.getUserId(), identity.getOauthService(), identity.getDomain());
        }
        if (e instanceof IllegalArgumentException) {
            return IamErrorResponseFactory.getInstance().invalidArgument(e.getMessage());
        }
        LOG.error("Unexpected exception", e);
        return IamErrorResponseFactory.getInstance().serverError(e);
    }

    private ResourceQuery filterQuery(Optional<Domain> optionalDomain, ResourceQuery query) {
        Set<String> filters = query.getFilters();
        Set<String> difference = optionalDomain.map(domain -> {
            filters.removeAll(domain.getUserProfileFields());
            return filters;
        }).orElseGet(HashSet::new);

        if (!difference.isEmpty()) {
            query = new ResourceQueryBuilder().add("_notExistent", "").build();
        }
        return query;
    }

    @SuppressWarnings("serial") private static class IllegalOauthServiceException extends Exception {}
}
