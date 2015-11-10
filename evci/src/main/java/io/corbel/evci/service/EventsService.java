package io.corbel.evci.service;

import com.fasterxml.jackson.databind.JsonNode;
import io.corbel.evci.model.Header;

public interface EventsService {

	void registerEvent(String type, JsonNode event);

}
