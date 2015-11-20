package io.corbel.iam.service;

import io.corbel.iam.model.Device;
import io.corbel.iam.model.User;

import java.util.Map;

/**
 * @author Alberto J. Rubio
 */
public interface EventsService {

    void sendUserCreatedEvent(User user);

    void sendUserModifiedEvent(User user);

    void sendUserAuthenticationEvent(User user);

    void sendUserDeletedEvent(String id, String domain);

    void sendNotificationEvent(String notificationId, String recipient, Map<String, String> properties);

    void sendDomainDeletedEvent(String domainId);

    void sendCreateScope(String scope);

    void sendDeleteScope(String scope);

    void sendClientAuthenticationEvent(String domainId, String id);

    void sendDeviceCreateEvent(Device device);

    void sendDeviceUpdateEvent(Device device);

    void sendDeviceDeleteEvent(String deviceId, String userId, String domainId);

}
