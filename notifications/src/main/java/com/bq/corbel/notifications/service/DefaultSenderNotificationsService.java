package com.bq.corbel.notifications.service;

import com.bq.corbel.notifications.model.Domain;
import com.bq.corbel.notifications.model.NotificationTemplate;
import com.bq.corbel.notifications.repository.DomainRepository;
import com.bq.corbel.notifications.repository.NotificationRepository;
import com.bq.corbel.notifications.template.NotificationFiller;
import com.bq.corbel.notifications.utils.DomainNameIdGenerator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
    private static final Logger LOG = LoggerFactory.getLogger(DefaultSenderNotificationsService.class);

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
        String currentNotificationId = DomainNameIdGenerator.generateNotificationTemplateId(domainId, notificationId);

        Domain domain = domainRepository.findOne(domainId);

        String notificationTemplateId = Optional.ofNullable(domain)
                .map(currentDomain -> currentDomain.getTemplates())
                .map(currentTemplate -> currentTemplate.get(currentNotificationId))
                .orElse(currentNotificationId);

        Map<String, String> properties = Optional.ofNullable(domain)
                .map(currentDomain -> getProperties(currentDomain, customProperties))
                .orElse(customProperties);


        NotificationTemplate notificationTemplate = notificationRepository.findOne(notificationTemplateId);
        if (notificationTemplate != null) {
            NotificationTemplate notificationTemplateFilled = notificationFiller.fill(notificationTemplate, properties);
            notificationsDispatcher.send(notificationTemplateFilled, recipient);
            LOG.debug("Template with id: "+ notificationTemplateId + " was sent");
        }
        else{
            LOG.error("Template with id: "+ notificationTemplateId +" not found");
        }

    }

    private Map<String, String> getProperties(Domain domain, Map<String, String> customProperties) {
        Map<String, String> propertiesByDomain = domain.getProperties() != null ? domain.getProperties() : Collections.emptyMap();

        Map<String, String> properties = new HashMap<>();
        properties.putAll(propertiesByDomain);

        if(customProperties != null) {
            properties.putAll(customProperties);
        }

        return  properties;
    }


}
