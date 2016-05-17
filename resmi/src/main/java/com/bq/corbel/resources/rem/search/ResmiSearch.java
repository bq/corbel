package com.bq.corbel.resources.rem.search;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import com.bq.corbel.lib.queries.request.Pagination;
import com.bq.corbel.lib.queries.request.ResourceQuery;
import com.bq.corbel.lib.queries.request.Sort;
import com.bq.corbel.resources.rem.model.ResourceUri;
import com.bq.corbel.resources.rem.resmi.exception.InvalidApiParamException;

/**
 * @author Francisco Sanchez
 */
public interface ResmiSearch {

    JsonArray search(ResourceUri uri, String search, List<ResourceQuery> resourceQueries, Pagination pagination, Optional<Sort> sort) throws InvalidApiParamException;

    JsonElement count(ResourceUri uri, String search, List<ResourceQuery> resourceQueries);

    JsonArray search(ResourceUri resourceUri, String templateName, Map<String, Object> templateParams, int page, int size) throws InvalidApiParamException;

    JsonElement count(ResourceUri resourceUri, String templateName, Map<String, Object> templateParams) throws InvalidApiParamException;

    void indexDocument(ResourceUri uri, JsonObject fields);

    void deleteDocument(ResourceUri uri);

    boolean upsertResmiIndex(ResourceUri uri);

    boolean upsertResmiIndex(ResourceUri uri, String settings, String defaultMapping) throws InvalidApiParamException;

    void setupMapping(ResourceUri uri, String mapping);

    void createIndex(String domain, String name, String settings);

}
