package com.bq.corbel.notifications.ioc;

import java.io.InputStream;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.bq.corbel.lib.mongo.IdGenerator;
import com.bq.corbel.lib.mongo.IdGeneratorMongoEventListener;
import com.bq.corbel.lib.queries.request.AggregationResultsFactory;
import com.bq.corbel.lib.queries.request.JsonAggregationResultsFactory;
import com.bq.corbel.lib.ws.digest.DigesterFactory;
import com.bq.corbel.notifications.api.DomainResource;
import com.bq.corbel.notifications.model.NotificationTemplate;
import com.bq.corbel.notifications.model.NotificationTemplateIdGenerator;
import com.bq.corbel.notifications.repository.DomainRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Scope;
import org.springframework.core.env.Environment;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;
import org.springframework.data.mongodb.repository.support.MongoRepositoryFactory;

import com.bq.corbel.lib.config.ConfigurationIoC;
import com.bq.corbel.lib.mongo.config.DefaultMongoConfiguration;
import com.bq.corbel.lib.queries.mongo.repository.QueriesRepositoryFactoryBean;
import com.bq.corbel.lib.token.ioc.TokenIoc;
import com.bq.corbel.lib.ws.auth.ioc.AuthorizationIoc;
import com.bq.corbel.lib.ws.cors.ioc.CorsIoc;
import com.bq.corbel.lib.ws.dw.ioc.CommonFiltersIoc;
import com.bq.corbel.lib.ws.dw.ioc.DropwizardIoc;
import com.bq.corbel.lib.ws.dw.ioc.MongoHealthCheckIoc;
import com.bq.corbel.lib.ws.ioc.QueriesIoc;
import com.bq.corbel.notifications.api.NotificationsResource;
import com.bq.corbel.notifications.cli.dsl.NotificationsShell;
import com.bq.corbel.notifications.repository.NotificationRepository;
import com.bq.corbel.notifications.service.AndroidPushNotificationsService;
import com.bq.corbel.notifications.service.ApplePushNotificationsService;
import com.bq.corbel.notifications.service.DefaultSenderNotificationsService;
import com.bq.corbel.notifications.service.EmailNotificationsService;
import com.bq.corbel.notifications.service.NotificationsDispatcher;
import com.bq.corbel.notifications.service.NotificationsService;
import com.bq.corbel.notifications.service.NotificationsServiceFactory;
import com.bq.corbel.notifications.service.SenderNotificationsService;
import com.bq.corbel.notifications.service.SpringNotificationsServiceFactory;
import com.bq.corbel.notifications.template.DefaultNotificationFiller;
import com.bq.corbel.notifications.template.NotificationFiller;
import com.notnoop.apns.APNS;
import com.notnoop.apns.ApnsService;
import com.notnoop.apns.ApnsServiceBuilder;

/**
 * Created by Alberto J. Rubio
 */
@Configuration @Import({ConfigurationIoC.class, DropwizardIoc.class, TokenIoc.class, AuthorizationIoc.class, CorsIoc.class,
        QueriesIoc.class, MongoHealthCheckIoc.class, CommonFiltersIoc.class}) @EnableMongoRepositories(
        value = "com.bq.corbel.notifications.repository", repositoryFactoryBeanClass = QueriesRepositoryFactoryBean.class) public class NotificationsIoc
        extends DefaultMongoConfiguration {

    @Autowired private Environment env;

    @Bean
    public NotificationsShell getNotificationsShell(NotificationRepository notificationRepository,
                                                    DomainRepository domainRepository) {
        return new NotificationsShell(notificationRepository, domainRepository);
    }

    @Bean
    public NotificationsResource getTemplateResource(NotificationRepository notificationRepository,
            SenderNotificationsService senderNotificationsService) {
        return new NotificationsResource(notificationRepository, senderNotificationsService);
    }

    @Bean
    public DomainResource getDomainResource(DomainRepository domainRepository) {
        return new DomainResource(domainRepository);
    }

    @Bean
    public MongoRepositoryFactory getMongoRepositoryFactory(MongoOperations mongoOperations) {
        return new MongoRepositoryFactory(mongoOperations);
    }

    @Bean
    public SenderNotificationsService getNotificationsEventService(NotificationRepository notificationRepository,
            NotificationsDispatcher notificationsDispatcher, DomainRepository domainRepository) {
        return new DefaultSenderNotificationsService(getTemplateFiller(), notificationsDispatcher, notificationRepository,
                domainRepository);
    }

    @Bean
    public NotificationFiller getTemplateFiller() {
        return new DefaultNotificationFiller();
    }

    @Bean
    public NotificationsDispatcher getNotificationsDispatcher(NotificationsServiceFactory notificationsServiceFactory) {
        return new NotificationsDispatcher(notificationsServiceFactory);
    }

    @Bean
    public NotificationsServiceFactory getNotificationsServiceFactory() {
        return new SpringNotificationsServiceFactory();
    }

    @Bean(name = "email")
    @Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
    public NotificationsService getNotificationsService() {
        return new EmailNotificationsService();
    }

    @Bean(name = "android")
    @Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
    public NotificationsService getAndroidPushNotificationService() {
        return new AndroidPushNotificationsService();
    }

    @Bean(name = "apple")
    @Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
    public NotificationsService getApplePushNotificationService() {
        return new ApplePushNotificationsService(getApnsService());
    }

    public ApnsService getApnsService() {
        InputStream certificate = this.getClass().getClassLoader().getResourceAsStream("certs/" + env.getProperty("apple.cert.name"));
        ApnsServiceBuilder apnsServiceBuilder = APNS.newService().withCert(certificate, env.getProperty("apple.cert.password"));
        if (env.getProperty("apple.cert.production", Boolean.class)) {
            apnsServiceBuilder.withProductionDestination();
        } else {
            apnsServiceBuilder.withSandboxDestination();
        }
        return apnsServiceBuilder.build();
    }

    @Bean
    public IdGeneratorMongoEventListener<NotificationTemplate> getNotificationTemplateIdGeneratorMongoEventListener() {
        return new IdGeneratorMongoEventListener<>(getNotificationTemplateIdGenerator(), NotificationTemplate.class);
    }

    private IdGenerator<NotificationTemplate> getNotificationTemplateIdGenerator() {
        return new NotificationTemplateIdGenerator();
    }


    @Override
    protected Environment getEnvironment() {
        return env;
    }

    @Override
    protected String getDatabaseName() {
        return "notifications";
    }
}
