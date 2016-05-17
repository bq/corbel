package com.bq.corbel.notifications.handler;

import com.bq.corbel.event.NotificationEvent;
import com.bq.corbel.notifications.eventbus.NotificationEventHandler;
import com.bq.corbel.notifications.service.SenderNotificationsService;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.fest.assertions.api.Assertions.assertThat;

/**
 * @author Alberto J. Rubio
 */
@RunWith(MockitoJUnitRunner.class)
public class NotificationTemplateEventHandlerTest {

    @Mock
    private SenderNotificationsService senderNotificationsService;

    private NotificationEventHandler handler;

    @Before
	public void setUp() throws Exception {
		handler = new NotificationEventHandler(senderNotificationsService);
	}


	@Test
	public void testGetEventType() {
		assertThat(handler.getEventType()).isEqualTo(NotificationEvent.class);
	}
}
