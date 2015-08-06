package com.bq.oss.corbel.resources.rem.service;

import io.corbel.lib.queries.request.AggregationResult;
import io.corbel.lib.queries.request.ResourceQuery;

import java.util.List;
import java.util.Optional;

import org.springframework.data.mongodb.core.index.Index;

import com.bq.oss.corbel.resources.rem.dao.NotFoundException;
import com.bq.oss.corbel.resources.rem.dao.RelationMoveOperation;
import com.bq.oss.corbel.resources.rem.model.ResourceUri;
import com.bq.oss.corbel.resources.rem.model.SearchResource;
import com.bq.oss.corbel.resources.rem.request.CollectionParameters;
import com.bq.oss.corbel.resources.rem.request.RelationParameters;
import com.bq.oss.corbel.resources.rem.resmi.exception.StartsWithUnderscoreException;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

/**
 * @author Francisco Sanchez
 */
public interface ResmiService {

    String ID = "id";
    String _ID = "_id";

    JsonArray findCollection(ResourceUri uri, Optional<CollectionParameters> apiParameters) throws BadConfigurationException;

    JsonObject findResource(ResourceUri uri);

    JsonElement findRelation(ResourceUri uri, Optional<RelationParameters> apiParameters) throws BadConfigurationException;

    AggregationResult aggregate(ResourceUri uri, CollectionParameters apiParameters) throws BadConfigurationException;

    JsonObject saveResource(ResourceUri uri, JsonObject object, Optional<String> userId) throws StartsWithUnderscoreException;

    JsonObject updateResource(ResourceUri uri, JsonObject jsonObject) throws StartsWithUnderscoreException;

    JsonObject conditionalUpdateResource(ResourceUri uri, JsonObject object, List<ResourceQuery> resourceQueries)
            throws StartsWithUnderscoreException;

    JsonObject createRelation(ResourceUri uri, JsonObject requestEntity) throws NotFoundException, StartsWithUnderscoreException;

    void moveRelation(ResourceUri uri, RelationMoveOperation relationMoveOperation);

    void deleteCollection(ResourceUri uri, Optional<List<ResourceQuery>> queries);

    void deleteResource(ResourceUri uri);

    void deleteRelation(ResourceUri uri);

    List<SearchResource> getSearchableFields();

    void addSearchableFields(SearchResource searchResource);

    void ensureExpireIndex(ResourceUri uri);

    void ensureIndex(ResourceUri uri, Index index);

    void removeObjectId(JsonObject object);

}
