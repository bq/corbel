package com.bq.corbel.notifications.service;

import com.bq.corbel.notifications.model.NotificationTemplate;

/**
 * Created by Alberto J. Rubio
 */
public interface NotificationsService {

    void send(NotificationTemplate notificationTemplate, String ... recipient);
}
