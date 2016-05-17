package com.bq.corbel.notifications.model;

import com.bq.corbel.lib.mongo.IdGenerator;
import com.bq.corbel.notifications.utils.DomainNameIdGenerator;

public class NotificationTemplateIdGenerator implements IdGenerator<NotificationTemplate> {

    @Override
    public String generateId(NotificationTemplate notificationTemplate) {
        return DomainNameIdGenerator.generateNotificationTemplateId(notificationTemplate.getDomain(), notificationTemplate.getName());
    }
}
