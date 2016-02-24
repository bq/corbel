package io.corbel.notifications.service;

import io.corbel.notifications.model.Domain;
import io.corbel.notifications.model.NotificationTemplate;
import io.corbel.notifications.repository.DomainRepository;
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
    DomainRepository domainRepository;

    public DefaultSenderNotificationsService(NotificationFiller notificationFiller,
                                             NotificationsDispatcher notificationsDispatcher,
                                             NotificationRepository notificationRepository,
                                             DomainRepository domainRepository) {
        this.notificationFiller = notificationFiller;
        this.notificationsDispatcher = notificationsDispatcher;
        this.notificationRepository = notificationRepository;
        this.domainRepository = domainRepository;
    }

    @Override
    public void sendNotification(String domainId, String notificationId, Map<String, String> customProperties, String recipient) {
        Domain domain = domainRepository.findOne(domainId);

        String notificationTemplateId = notificationId;
        Map<String, String> properties = customProperties;
        if(domain != null) {
            notificationTemplateId =  domain.getTemplates().get(notificationId);
            properties = getProperties(domain, customProperties);
        }

        NotificationTemplate notificationTemplate = notificationRepository.findOne(notificationTemplateId);
        if (notificationTemplate != null) {
            NotificationTemplate notificationTemplateFilled = notificationFiller.fill(notificationTemplate, properties);
            notificationsDispatcher.send(notificationTemplateFilled, recipient);
        }

    }

    private Map<String, String> getProperties(Domain domain, Map<String, String> customProperties) {
        Map<String, String> propertiesByDomain = Optional.ofNullable(domain)
                .map(config -> config.getProperties())
                .orElse(Collections.emptyMap());

        Map<String, String> properties = new HashMap<>();
        properties.putAll(propertiesByDomain);
        properties.putAll(customProperties);

        return  properties;
    }


}
