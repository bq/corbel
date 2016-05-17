package com.bq.corbel.evci.ioc;

import com.bq.corbel.evci.api.EventResource;
import com.bq.corbel.evci.api.EworkerInfoResource;
import com.bq.corbel.evci.client.DefaultEvciClient;
import com.bq.corbel.evci.client.EvciClient;
import com.bq.corbel.evci.converter.DomainObjectJsonMessageConverterFactory;
import com.bq.corbel.evci.eventbus.EvciEventHandler;
import com.bq.corbel.evci.eworker.EworkerArtifactIdRegistry;
import com.bq.corbel.evci.eworker.EworkerRegistry;
import com.bq.corbel.evci.service.DefaultEventService;
import com.bq.corbel.evci.service.EvciMQ;
import com.bq.corbel.evci.service.EventsService;
import com.bq.corbel.event.EvciEvent;
import com.bq.corbel.eventbus.EventHandler;
import com.bq.corbel.eventbus.ioc.EventBusListeningIoc;
import com.bq.corbel.eworker.internal.AmqpEworkerRegistry;
import com.bq.corbel.eworker.internal.InMemoryArtifactIdRegistry;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JSR310Module;
import com.bq.corbel.lib.config.ConfigurationIoC;
import com.bq.corbel.lib.rabbitmq.config.AmqpConfiguration;
import com.bq.corbel.lib.rabbitmq.config.AmqpConfigurer;
import com.bq.corbel.lib.rabbitmq.config.BackoffOptions;
import com.bq.corbel.lib.rabbitmq.ioc.AbstractRabbitMQConfiguration;
import com.bq.corbel.lib.token.ioc.TokenIoc;
import com.bq.corbel.lib.ws.auth.ioc.AuthorizationIoc;
import com.bq.corbel.lib.ws.cors.ioc.CorsIoc;
import com.bq.corbel.lib.ws.dw.ioc.CommonFiltersIoc;
import com.bq.corbel.lib.ws.dw.ioc.DropwizardIoc;
import com.bq.corbel.lib.ws.dw.ioc.RabbitMQHealthCheckIoc;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.*;
import org.springframework.core.env.Environment;

import java.util.Map;
import java.util.Optional;
import java.util.function.UnaryOperator;

@Configuration 
@Import({ConfigurationIoC.class, DropwizardIoc.class, TokenIoc.class, AuthorizationIoc.class, CorsIoc.class,
        EventBusListeningIoc.class, RabbitMQHealthCheckIoc.class, CommonFiltersIoc.class}) 
@ComponentScan({"com.bq.corbel.evci.eworker.plugin", "com.bqreaders.silkroad.evci.eworker.plugin"})
public class EvciIoc extends AbstractRabbitMQConfiguration {

    @Autowired private Environment env;

    @Override
    protected MessageConverter getMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    @Bean
    public EventHandler<EvciEvent> getEvciEventHandler() {
        return new EvciEventHandler(eventService(), getObjectMapper());
    }

    @Bean
    public EventResource eventResource() {
        return new EventResource(eventService(), getObjectMapper());
    }

    @Bean(name = "evciAmqpTemplate")
    @Override
    public AmqpTemplate amqpTemplate() {
        return super.amqpTemplate();
    }

    @Bean
    public EventsService eventService() {
        return new DefaultEventService(amqpTemplate(), routingPatternFunction());
    }

    @Bean
    public AmqpConfiguration rabbitMQConfiguration() {
        return configurer -> {
            configurer.topicExchange(EvciMQ.EVENTS_EXCHANGE,
                    configurer.alternateExchange(configurer.fanoutExchange(EvciMQ.UNKNOWN_EXCHANGE)));
            configurer.bind(EvciMQ.UNKNOWN_EXCHANGE, configurer.queue(EvciMQ.UNKNOWN_QUEUE), Optional.<String>empty(),
                    Optional.<Map<String, Object>>empty());

            configurer.topicExchange(EvciMQ.EVENTS_DEAD_LETTER_EXCHANGE);
        };
    }

    @Bean
    public DomainObjectJsonMessageConverterFactory domainObjectJsonMessageConverterFactory(ObjectMapper objectMapper) {
        return new DomainObjectJsonMessageConverterFactory(objectMapper);
    }

    @Bean
    public ObjectMapper getObjectMapper() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        mapper.registerModule(new JSR310Module());
        return mapper;
    }

    @Bean
    @DependsOn("rabbitMQConfiguration")
    public EworkerRegistry eworkerRegistry(AmqpConfigurer configurer, DomainObjectJsonMessageConverterFactory converterFactory) {
        return new AmqpEworkerRegistry(converterFactory, configurer, getBackoffOptions(), env.getProperty("rabbitmq.maxAttempts",
                Integer.class), routingPatternFunction());
    }

    @Bean
    public EvciClient evciClient() {
        return new DefaultEvciClient(eventService(), getObjectMapper());
    }

    private BackoffOptions getBackoffOptions() {
        BackoffOptions backoffOptions = new BackoffOptions();
        backoffOptions.setInitialInterval(env.getProperty("rabbitmq.backoff.initialInterval", Long.class));
        backoffOptions.setMultiplier(env.getProperty("rabbitmq.backoff.multiplier", Double.class));
        backoffOptions.setMaxInterval(env.getProperty("rabbitmq.backoff.maxInterval", Long.class));
        return backoffOptions;
    }

    @Bean
    public EworkerArtifactIdRegistry eworkerArtifactIdRegistry() {
        return new InMemoryArtifactIdRegistry();
    }

    @Bean
    public EworkerInfoResource eworkerInfoResource(EworkerArtifactIdRegistry eworkerArtifactIdRegistry) {
        return new EworkerInfoResource(eworkerArtifactIdRegistry);
    }

    private UnaryOperator<String> routingPatternFunction() {
        return type -> type.replace(":", ".");
    }

    @Override
    protected Environment getEnvironment() {
        return env;
    }
}
