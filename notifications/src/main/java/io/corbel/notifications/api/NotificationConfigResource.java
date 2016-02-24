package io.corbel.notifications.api;


import io.corbel.lib.queries.jaxrs.QueryParameters;
import io.corbel.lib.ws.annotation.Rest;
import io.corbel.lib.ws.api.error.ErrorResponseFactory;
import io.corbel.notifications.model.NotificationConfigByDomain;
import io.corbel.notifications.repository.NotificationConfigByDomainRepository;

import javax.validation.Valid;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import java.util.List;

@Path(ApiVersion.CURRENT + "/config")
public class NotificationConfigResource {

    private final NotificationConfigByDomainRepository notificationConfigByDomainRepository;


    public NotificationConfigResource(NotificationConfigByDomainRepository notificationConfigByDomainRepository) {
        this.notificationConfigByDomainRepository = notificationConfigByDomainRepository;
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    public Response postNotificationConfig(@Valid NotificationConfigByDomain notificationConfigByDomain, @Context UriInfo uriInfo) {
        notificationConfigByDomainRepository.save(notificationConfigByDomain);
        return Response.created(uriInfo.getAbsolutePathBuilder().path(notificationConfigByDomain.getId()).build()).build();
    }

    @GET
    public Response getNotificationsConfig(@Rest QueryParameters queryParameters) {
        List<NotificationConfigByDomain> notificationsConfigByDomain = notificationConfigByDomainRepository.find(queryParameters.getQuery()
                .orElse(null), queryParameters.getPagination(), queryParameters.getSort().orElse(null));
        return Response.ok().type(MediaType.APPLICATION_JSON).entity(notificationsConfigByDomain).build();
    }

    @GET
    @Path("{id}")
    public Response getNotificationConfig(@PathParam("id") String id) {
        NotificationConfigByDomain notificationConfigByDomain = notificationConfigByDomainRepository.findOne(id);
        if (notificationConfigByDomain == null) {
            return NotificationsErrorResponseFactory.getInstance().notFound();
        }
        return Response.ok().type(MediaType.APPLICATION_JSON).entity(notificationConfigByDomain).build();
    }

    @PUT
    @Path("{id}")
    public Response updateNotificationConfig(NotificationConfigByDomain notificationConfigByDomainData, @PathParam("id") String id) {
        NotificationConfigByDomain notificationConfigByDomain= notificationConfigByDomainRepository.findOne(id);

        if(notificationConfigByDomain != null) {
            notificationConfigByDomain.updateNotificationConfigByDomain(notificationConfigByDomainData);
            notificationConfigByDomainRepository.save(notificationConfigByDomain);
            return Response.status(Response.Status.NO_CONTENT).build();
        }
        else {
            return ErrorResponseFactory.getInstance().notFound();
        }
    }

    @DELETE
    @Path("/{id}")
    public Response deleteNotificationConfig(@PathParam("id") String id) {
        notificationConfigByDomainRepository.delete(id);
        return Response.status(Response.Status.NO_CONTENT).build();
    }
}
