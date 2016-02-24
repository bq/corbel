package io.corbel.notifications.cli.dsl

import io.corbel.lib.cli.console.Description
import io.corbel.lib.cli.console.Shell
import io.corbel.notifications.model.NotificationConfigByDomain
import io.corbel.notifications.model.NotificationTemplate
import io.corbel.notifications.repository.NotificationConfigByDomainRepository
import io.corbel.notifications.repository.NotificationRepository

/**
 * @author Alberto J. Rubio
 *
 */
@Shell("notifications")
class NotificationsShell {

    NotificationRepository notificationRepository
    NotificationConfigByDomainRepository notificationConfigByDomainRepository

    public NotificationsShell(NotificationRepository notificationRepository,
                              NotificationConfigByDomainRepository notificationConfigByDomainRepository) {
        this.notificationRepository = notificationRepository
        this.notificationConfigByDomainRepository = notificationConfigByDomainRepository;
    }

    @Description("Creates a new notification on the DB. The input parameter is a map containing the notification data.")
    def createNotification(notificationFields) {
        assert notificationFields.id : 'Notification id is required'
        assert notificationFields.sender : 'Notification sender is required'
        assert notificationFields.type : 'Notification type is required'
        assert notificationFields.text : 'Notification text is required'
        NotificationTemplate notification = new NotificationTemplate()
        notification.id = notificationFields.id
        notification.sender = notificationFields.sender
        notification.type = notificationFields.type
        notification.text = notificationFields.text
        notification.title = notificationFields.title
        notificationRepository.save(notification)
    }

    @Description("Creates a new notification Config by Domain on the DB. The input parameter is a map containing the notification config data.")
    def createNotificationConfig(notificationConfigFields) {
        assert notificationConfigFields.id : 'Notification config id is required'
        assert notificationConfigFields.domain : 'Notification config domain is required'
        assert notificationConfigFields.template : 'Notification config template is required'
        assert notificationConfigFields.properties : 'Notification config properties is required'
        NotificationConfigByDomain notificationConfigByDomain = new NotificationConfigByDomain()
        notificationConfigByDomain.id = notificationConfigFields.id
        notificationConfigByDomain.domain = notificationConfigFields.domain
        notificationConfigByDomain.template = notificationConfigFields.template
        notificationConfigByDomain.properties = notificationConfigFields.template
        notificationConfigByDomainRepository.save(notificationConfigByDomain)
    }

}

