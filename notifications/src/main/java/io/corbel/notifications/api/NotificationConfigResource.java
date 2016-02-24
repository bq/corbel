package io.corbel.notifications.api;


import com.google.gson.JsonElement;
import io.corbel.lib.queries.builder.ResourceQueryBuilder;
import io.corbel.lib.queries.jaxrs.QueryParameters;
import io.corbel.lib.queries.request.*;
import io.corbel.lib.ws.annotation.Rest;
import io.corbel.lib.ws.api.error.ErrorResponseFactory;
import io.corbel.lib.ws.auth.AuthorizationInfo;
import io.corbel.notifications.model.NotificationConfigByDomain;
import io.corbel.notifications.repository.NotificationConfigByDomainRepository;
import io.dropwizard.auth.Auth;

import javax.validation.Valid;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import java.util.List;

@Path(ApiVersion.CURRENT + "/config")
public class NotificationConfigResource {

    private static final int BAD_REQUEST_STATUS = 400;
    private static final String DOMAIN = "domain";

    private final NotificationConfigByDomainRepository notificationConfigByDomainRepository;
    private final AggregationResultsFactory<JsonElement> aggregationResultsFactory;


    public NotificationConfigResource(NotificationConfigByDomainRepository notificationConfigByDomainRepository,
                                      AggregationResultsFactory aggregationResultsFactory) {
        this.notificationConfigByDomainRepository = notificationConfigByDomainRepository;
        this.aggregationResultsFactory = aggregationResultsFactory;
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    public Response postNotificationConfig(@Valid NotificationConfigByDomain notificationConfigByDomain, @Auth AuthorizationInfo authorizationInfo,
                                           @Context UriInfo uriInfo) {
        notificationConfigByDomain.setDomain(authorizationInfo.getDomainId());
        notificationConfigByDomainRepository.save(notificationConfigByDomain);
        return Response.created(uriInfo.getAbsolutePathBuilder().path(notificationConfigByDomain.getId()).build()).build();
    }

    @GET
    public Response getNotificationsConfig(@Rest QueryParameters queryParameters, @Auth AuthorizationInfo authorizationInfo) {
        if (queryParameters.getAggregation().isPresent()) {
            return getNotificationConfigAggregation(authorizationInfo.getDomainId(), queryParameters.getQuery()
                    .orElse(null), queryParameters.getAggregation().orElse(null));
        } else {
            List<NotificationConfigByDomain> notificationConfigByDomainList = notificationConfigByDomainRepository.find(addDomainToQuery(authorizationInfo.getDomainId(), queryParameters.getQuery()
                    .orElse(null)), queryParameters.getPagination(), queryParameters.getSort().orElse(null));

            return Response.ok().type(MediaType.APPLICATION_JSON).entity(notificationConfigByDomainList).build();
        }

    }

    private Response getNotificationConfigAggregation(String domainId, ResourceQuery query, Aggregation aggregation) {
        if (!AggregationOperator.$COUNT.equals(aggregation.getOperator())) {
            return Response.status(BAD_REQUEST_STATUS).build();
        }

        long count = notificationConfigByDomainRepository.count(addDomainToQuery(domainId, query));
        JsonElement result = aggregationResultsFactory.countResult(count);
        return Response.ok().type(MediaType.APPLICATION_JSON).entity(result).build();
    }

    private ResourceQuery addDomainToQuery(String domain, ResourceQuery resourceQuery) {
        ResourceQueryBuilder builder = new ResourceQueryBuilder(resourceQuery);
        builder.remove(DOMAIN).add(DOMAIN, domain);
        return builder.build();
    }

    @GET
    @Path("{id}")
    public Response getNotificationConfig(@PathParam("id") String id, @Auth AuthorizationInfo authorizationInfo) {
        NotificationConfigByDomain notificationConfigByDomain = notificationConfigByDomainRepository.findOne(id);
        if (notificationConfigByDomain == null || !authorizationInfo.getDomainId().equals(notificationConfigByDomain.getDomain())) {
            return NotificationsErrorResponseFactory.getInstance().notFound();
        }
        return Response.ok().type(MediaType.APPLICATION_JSON).entity(notificationConfigByDomain).build();
    }

    @PUT
    @Path("{id}")
    public Response updateNotificationConfig(NotificationConfigByDomain notificationConfigByDomainData, @Auth AuthorizationInfo authorizationInfo,
                                             @PathParam("id") String id) {

        NotificationConfigByDomain notificationConfigByDomain= notificationConfigByDomainRepository.findOne(id);

        if((notificationConfigByDomainData.getDomain() != null && !authorizationInfo.getDomainId().equals(notificationConfigByDomainData.getDomain()))
                || notificationConfigByDomain == null) {
            return ErrorResponseFactory.getInstance().notFound();
        }

        notificationConfigByDomain.updateNotificationConfigByDomain(notificationConfigByDomainData);
        notificationConfigByDomainRepository.save(notificationConfigByDomain);
        return Response.status(Response.Status.NO_CONTENT).build();

    }

    @DELETE
    @Path("/{id}")
    public Response deleteNotificationConfig(@PathParam("id") String id, @Auth AuthorizationInfo authorizationInfo) {
        NotificationConfigByDomain notificationConfigByDomain = notificationConfigByDomainRepository.findOne(id);
        if(notificationConfigByDomain.getDomain().equals(authorizationInfo.getDomainId())) {
            notificationConfigByDomainRepository.delete(id);
        }
        return Response.status(Response.Status.NO_CONTENT).build();
    }
}
