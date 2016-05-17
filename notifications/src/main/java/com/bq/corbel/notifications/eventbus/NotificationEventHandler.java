package com.bq.corbel.notifications.eventbus;

import com.bq.corbel.event.NotificationEvent;
import com.bq.corbel.eventbus.EventHandler;
import com.bq.corbel.notifications.service.SenderNotificationsService;

/**
 * Created by Alberto J. Rubio
 */
public class NotificationEventHandler implements EventHandler<NotificationEvent> {

	private SenderNotificationsService senderNotificationsService;

	public NotificationEventHandler(SenderNotificationsService senderNotificationsService) {
		this.senderNotificationsService = senderNotificationsService;
	}

	@Override
	public void handle(NotificationEvent event) {
		senderNotificationsService.sendNotification(event.getDomain(), event.getNotificationId(), event.getProperties(),
				event.getRecipient());
	}

	@Override
	public Class<NotificationEvent> getEventType() {
		return NotificationEvent.class;
	}
}
