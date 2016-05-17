package com.bq.corbel.notifications.template;

import com.bq.corbel.notifications.model.NotificationTemplate;

import java.util.Map;

/**
 * @author Francisco Sanchez
 */
public interface NotificationFiller {

	NotificationTemplate fill(NotificationTemplate notificationTemplate, Map<String, String> properties);
}
