package com.bq.corbel.notifications.ioc;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

import com.bq.corbel.lib.ws.dw.ioc.RabbitMQHealthCheckIoc;
import com.bq.corbel.event.NotificationEvent;
import com.bq.corbel.eventbus.EventHandler;
import com.bq.corbel.eventbus.ioc.EventBusListeningIoc;
import com.bq.corbel.notifications.eventbus.NotificationEventHandler;
import com.bq.corbel.notifications.service.SenderNotificationsService;

/**
 * Created by Alberto J. Rubio
 */
@Configuration
@Import({ NotificationsIoc.class, EventBusListeningIoc.class, RabbitMQHealthCheckIoc.class })
public class NotificationsListenerIoc {

	@Bean
	public EventHandler<NotificationEvent> getMailEventHandler(SenderNotificationsService senderNotificationsService) {
		return new NotificationEventHandler(senderNotificationsService);
	}

}
