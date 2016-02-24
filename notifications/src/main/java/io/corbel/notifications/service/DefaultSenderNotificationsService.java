package io.corbel.notifications.service;

import io.corbel.notifications.model.NotificationConfigByDomain;
import io.corbel.notifications.model.NotificationTemplate;
import io.corbel.notifications.repository.NotificationConfigByDomainRepository;
import io.corbel.notifications.repository.NotificationRepository;
import io.corbel.notifications.template.NotificationFiller;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * @author Cristian del Cerro
 */
public class DefaultSenderNotificationsService implements SenderNotificationsService {

    NotificationFiller notificationFiller;
    NotificationsDispatcher notificationsDispatcher;
    NotificationRepository notificationRepository;
    NotificationConfigByDomainRepository notificationConfigByDomainRepository;

    public DefaultSenderNotificationsService(NotificationFiller notificationFiller,
                                             NotificationsDispatcher notificationsDispatcher,
                                             NotificationRepository notificationRepository,
                                             NotificationConfigByDomainRepository notificationConfigByDomainRepository) {
        this.notificationFiller = notificationFiller;
        this.notificationsDispatcher = notificationsDispatcher;
        this.notificationRepository = notificationRepository;
        this.notificationConfigByDomainRepository = notificationConfigByDomainRepository;
    }

    @Override
    public void sendNotification(String domainId, String notificationId, Map<String, String> customProperties, String recipient) {
        NotificationTemplate notificationTemplate = notificationRepository.findOne(notificationId);

        Map<String, String> properties = getProperties(domainId, notificationId, customProperties);

        if(notificationTemplate != null) {
            NotificationTemplate notificationTemplateFilled = notificationFiller.fill(notificationTemplate, properties);
            notificationsDispatcher.send(notificationTemplateFilled, recipient);
        }
    }

    private Map<String, String> getProperties(String domainId, String notificationId, Map<String, String> customProperties) {
        if(domainId == null) {
            return  customProperties;
        }

        NotificationConfigByDomain notificationConfigByDomain = notificationConfigByDomainRepository
                .findByDomainAndTemplate(domainId, notificationId);

        Map<String, String> propertiesByDomain = Optional.ofNullable(notificationConfigByDomain)
                .map(config -> config.getProperties())
                .orElse(Collections.emptyMap());

        Map<String, String> properties = new HashMap<>();
        properties.putAll(propertiesByDomain);
        properties.putAll(customProperties);

        return  properties;
    }


}
