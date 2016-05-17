package com.bq.corbel.notifications.service;

import com.bq.corbel.notifications.model.NotificationTemplate;

/**
 * Created by Alberto J. Rubio
 */
public class NotificationsDispatcher {

    private final NotificationsServiceFactory notificationsServiceFactory;

    public NotificationsDispatcher(NotificationsServiceFactory notificationsServiceFactory) {
        this.notificationsServiceFactory = notificationsServiceFactory;
    }

    public void send(NotificationTemplate notificationTemplate, String recipient) {
        NotificationsService notificationsService =
                notificationsServiceFactory.getNotificationService(notificationTemplate.getType());
        notificationsService.send(notificationTemplate, recipient);
    }
}
