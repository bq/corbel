package com.bq.corbel.evci.eventbus;

import com.bq.corbel.evci.model.Header;
import com.bq.corbel.evci.service.EventsService;
import com.bq.corbel.event.EvciEvent;
import com.bq.corbel.eventbus.EventHandler;

import java.util.Objects;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableMap;

/**
 * @author Cristian del Cerro
 */
public class EvciEventHandler implements EventHandler<EvciEvent> {

    private static final Logger LOG = LoggerFactory.getLogger(EvciEventHandler.class);

    private final EventsService eventsService;
    private final ObjectMapper objectMapper;

    public EvciEventHandler(EventsService eventsService, ObjectMapper objectMapper) {
        this.eventsService = eventsService;
        this.objectMapper = objectMapper;
    }

    @Override
    public void handle(EvciEvent evciEvent) {
        Object data = evciEvent.getData();
        if (Objects.nonNull(data)) {
            try {
                Header header = new Header();
                header.setDomainId(evciEvent.getDomain());
                JsonNode jsonHeader = objectMapper.convertValue(header, JsonNode.class);
                eventsService.registerEvent(
                        evciEvent.getType(),
                        objectMapper.createObjectNode().setAll(
                                ImmutableMap.of("header", jsonHeader, "content", objectMapper.readTree(evciEvent.getData()))));
            } catch (Exception e) {
                LOG.error("Received EvciEvent with unparsable JSON data.", e);
                throw new RuntimeException(e); // causes message to be rejected and send to dead-letter queue
            }
        } else {
            LOG.warn("Received EvciEvent with null data. Ignoring event!");
        }
    }

    @Override
    public Class<EvciEvent> getEventType() {
        return EvciEvent.class;
    }
}
