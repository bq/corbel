package io.corbel.notifications.model;

import io.corbel.lib.mongo.IdGenerator;

public class NotificationTemplateIdGenerator implements IdGenerator<NotificationTemplate> {

    private static final String SEPARATOR = ":";

    @Override
    public String generateId(NotificationTemplate notificationTemplate) {
        return notificationTemplate.getDomain() + SEPARATOR + notificationTemplate.getName();
    }
}
