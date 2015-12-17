package io.corbel.resources.rem.search;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import io.corbel.lib.queries.request.Pagination;
import io.corbel.lib.queries.request.ResourceQuery;
import io.corbel.lib.queries.request.Sort;
import io.corbel.resources.rem.model.ResourceUri;

/**
 * @author Francisco Sanchez
 */
public interface ResmiSearch {

    JsonArray search(ResourceUri uri, String search, List<ResourceQuery> resourceQueries, Pagination pagination, Optional<Sort> sort);

    JsonElement count(ResourceUri uri, String search, List<ResourceQuery> resourceQueries);

    JsonArray search(ResourceUri resourceUri, String templateName, Map<String, Object> templateParams, int page, int size);

    JsonElement count(ResourceUri resourceUri, String templateName, Map<String, Object> templateParams);

    void indexDocument(ResourceUri uri, JsonObject fields);

    void deleteDocument(ResourceUri uri);

    boolean upsertResmiIndex(ResourceUri uri);

    boolean upsertResmiIndex(ResourceUri uri, String settings, String defaultMapping);

    void setupMapping(ResourceUri uri, String mapping);

    void createIndex(String domain, String name, String settings);

}
