package com.bq.corbel.iam.service;

import java.util.Map;

import com.bq.corbel.iam.model.Device;
import com.bq.corbel.iam.model.User;

/**
 * @author Alberto J. Rubio
 */
public interface EventsService {

    void sendUserCreatedEvent(User user, boolean avoidNotification);

    void sendUserModifiedEvent(User user);

    void sendUserAuthenticationEvent(User user);

    void sendUserDeletedEvent(User user, String domain);

    void sendNotificationEvent(String domainId, String notificationId, String recipient, Map<String, String> properties);

    void sendDomainDeletedEvent(String domainId);

    void sendCreateScope(String scope);

    void sendDeleteScope(String scope);

    void sendClientAuthenticationEvent(String domainId, String id);

    void sendDeviceCreateEvent(Device device);

    void sendDeviceUpdateEvent(Device device);

    void sendDeviceDeleteEvent(Device device);

    void sendUpdateDomainPublicScopesEvent(String domainId);
}
