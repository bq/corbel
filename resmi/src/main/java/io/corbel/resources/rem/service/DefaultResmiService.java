package io.corbel.resources.rem.service;

import io.corbel.lib.queries.request.Aggregation;
import io.corbel.lib.queries.request.AggregationResult;
import io.corbel.lib.queries.request.Average;
import io.corbel.lib.queries.request.ResourceQuery;
import io.corbel.lib.queries.request.Sum;
import io.corbel.resources.rem.dao.NotFoundException;
import io.corbel.resources.rem.dao.RelationMoveOperation;
import io.corbel.resources.rem.dao.ResmiDao;
import io.corbel.resources.rem.model.ResourceUri;
import io.corbel.resources.rem.request.CollectionParameters;
import io.corbel.resources.rem.request.RelationParameters;
import io.corbel.resources.rem.resmi.exception.MongoAggregationException;
import io.corbel.resources.rem.resmi.exception.StartsWithUnderscoreException;

import java.time.Clock;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import org.springframework.data.mongodb.core.index.Index;

import com.google.common.collect.Sets;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;

/**
 * @author Francisco Sanchez
 */
public class DefaultResmiService implements ResmiService {

    protected static final String _DST_ID = "_dst_id";
    protected static final String _SRC_ID = "_src_id";
    protected static final String _ORDER = "_order";
    protected static final String _EXPIRE_AT = "_expireAt";
    protected static final String _CREATED_AT = "_createdAt";
    protected static final String _UPDATED_AT = "_updatedAt";
    protected static final String _ACL = "_acl";
    protected final static Set<String> ignorableReservedAttributeNames = Sets.newHashSet(_ID, _EXPIRE_AT, _ORDER, _SRC_ID, _DST_ID,
            _CREATED_AT, _UPDATED_AT, _ACL);

    public final static String SEARCHABLE_FIELDS = "searchable";

    protected final ResmiDao resmiDao;
    protected final Clock clock;

    public DefaultResmiService(ResmiDao resmiDao, Clock clock) {
        this.resmiDao = resmiDao;
        this.clock = clock;
    }

    @Override
    public AggregationResult aggregate(ResourceUri resourceUri, CollectionParameters apiParameters) throws BadConfigurationException {
        Aggregation operation = apiParameters.getAggregation().get();
        switch (operation.getOperator()) {
            case $COUNT:
                return resmiDao.count(resourceUri, operation.operate(apiParameters.getQueries().orElse(null)));
            case $AVG:
                return resmiDao.average(resourceUri, operation.operate(apiParameters.getQueries().orElse(null)),
                        ((Average) operation).getField());
            case $SUM:
                return resmiDao.sum(resourceUri, operation.operate(apiParameters.getQueries().orElse(null)), ((Sum) operation).getField());
            default:
                throw new RuntimeException("Aggregation operation not supported: " + operation.getOperator());
        }
    }

    @Override
    public JsonArray findCollection(ResourceUri uri, Optional<CollectionParameters> apiParameters) throws BadConfigurationException {
        return resmiDao.findCollection(uri, apiParameters.flatMap(params -> params.getQueries()),
                apiParameters.map(params -> params.getPagination()), apiParameters.flatMap(params -> params.getSort()));
    }

    @Override
    public JsonArray findCollectionDistinct(ResourceUri uri, Optional<CollectionParameters> apiParameters, List<String> fields,
            boolean first) throws BadConfigurationException, MongoAggregationException {
        return resmiDao.findCollectionWithGroup(uri, apiParameters.flatMap(params -> params.getQueries()),
                apiParameters.map(params -> params.getPagination()), apiParameters.flatMap(params -> params.getSort()), fields, first);
    }

    @Override
    public JsonObject findResource(ResourceUri uri) {
        return resmiDao.findResource(uri);
    }

    @Override
    public JsonElement findRelation(ResourceUri uri, Optional<RelationParameters> apiParameters) throws BadConfigurationException {
        return resmiDao.findRelation(uri, apiParameters.flatMap(params -> params.getQueries()),
                apiParameters.map(params -> params.getPagination()), apiParameters.flatMap(params -> params.getSort()));
    }

    @Override
    public JsonArray findRelationDistinct(ResourceUri uri, Optional<RelationParameters> apiParameters, List<String> fields, boolean first)
            throws BadConfigurationException, MongoAggregationException {
        return resmiDao.findRelationWithGroup(uri, apiParameters.flatMap(params -> params.getQueries()),
                apiParameters.map(params -> params.getPagination()), apiParameters.flatMap(params -> params.getSort()), fields, first);
    }

    @Override
    public JsonObject saveResource(ResourceUri uri, JsonObject object, Optional<String> optionalUserId)
            throws StartsWithUnderscoreException {
        verifyNotUnderscore(object);
        optionalUserId.ifPresent(userId -> setId(userId, object));
        createDates(object);
        resmiDao.saveResource(uri, object);
        return object;
    }

    @Override
    public JsonObject updateCollection(ResourceUri uri, JsonObject jsonObject) throws StartsWithUnderscoreException {

        verifyNotUnderscore(jsonObject);
        createDates(jsonObject);
        resmiDao.updateCollection(uri, jsonObject);
        indexInSearchService(uri, jsonObject);

        return jsonObject;
    }

    @Override
    public JsonObject conditionalUpdateCollection(ResourceUri uri, JsonObject jsonObject, List<ResourceQuery> resourceQueries)
        throws StartsWithUnderscoreException {
            verifyNotUnderscore(jsonObject);
            updateDates(jsonObject);
            boolean found = resmiDao.conditionalUpdateCollection(uri, jsonObject, resourceQueries);
            if (found) {
                indexInSearchService(uri, jsonObject);
                return jsonObject;
            }
            return null;
    }

    @Override
    public JsonObject updateResource(ResourceUri uri, JsonObject jsonObject) throws StartsWithUnderscoreException {
        verifyNotUnderscore(jsonObject);
        createDates(jsonObject);
        resmiDao.updateResource(uri, jsonObject);
        return jsonObject;
    }

    @Override
    public JsonObject conditionalUpdateResource(ResourceUri uri, JsonObject jsonObject, List<ResourceQuery> resourceQueries)
            throws StartsWithUnderscoreException {
        verifyNotUnderscore(jsonObject);
        updateDates(jsonObject);
        boolean found = resmiDao.conditionalUpdateResource(uri, jsonObject, resourceQueries);
        if (found) {
            return jsonObject;
        }
        return null;
    }

    @Override
    public JsonObject createRelation(ResourceUri uri, JsonObject requestEntity) throws NotFoundException, StartsWithUnderscoreException {
        verifyNotUnderscore(requestEntity);
        createDates(requestEntity);
        resmiDao.createRelation(uri, requestEntity);
        return requestEntity;
    }

    @Override
    public void moveRelation(ResourceUri uri, RelationMoveOperation relationMoveOperation) {
        if (uri.isTypeWildcard()) {
            throw new IllegalArgumentException("Relation origin must not be a wildcard");
        }
        resmiDao.moveRelation(uri, relationMoveOperation);
    }

    @Override
    public void deleteResource(ResourceUri uri) {
        resmiDao.deleteResource(uri);
    }

    @Override
    public void deleteCollection(ResourceUri uri, Optional<List<ResourceQuery>> queries) {
        resmiDao.deleteCollection(uri, queries);
    }

    @Override
    public void deleteRelation(ResourceUri uri) {
        resmiDao.deleteRelation(uri);
    }

    @Override
    public void ensureExpireIndex(ResourceUri uri) {
        resmiDao.ensureExpireIndex(uri);
    }

    @Override
    public void ensureIndex(ResourceUri uri, Index index) {
        resmiDao.ensureIndex(uri, index);
    }

    @Override
    public void removeObjectId(JsonObject object) {
        if (object.has(ResmiService.ID)) {
            object.remove(ResmiService.ID);
        }
    }

    protected void setId(String userId, JsonObject jsonObject) {
        String id = userId != null ? generateIdWithUserId(userId) : generateId();
        jsonObject.add(ResmiService.ID, new JsonPrimitive(id));
    }

    protected String generateIdWithUserId(String userId) {
        return userId + ":" + generateId();
    }

    protected String generateId() {
        return UUID.randomUUID().toString();
    }

    protected void updateDates(JsonObject entity) {
        if (entity == null) {
            return;
        }

        Date date = Date.from(clock.instant());
        String formatedDate = formatDate(date);

        entity.remove(_CREATED_AT);
        entity.remove(_UPDATED_AT);
        entity.addProperty(_UPDATED_AT, formatedDate);
    }

    private void createDates(JsonObject entity) {
        if (entity == null) {
            return;
        }

        Date date = Date.from(clock.instant());
        String formatedDate = formatDate(date);

        entity.remove(_CREATED_AT);
        entity.addProperty(_CREATED_AT, formatedDate);

        entity.remove(_UPDATED_AT);
        entity.addProperty(_UPDATED_AT, formatedDate);
    }

    protected String formatDate(Date date) {
        return "ISODate(" + String.format("%tFT%<tT.%<tLZ", date) + ")";
    }

    protected JsonObject verifyNotUnderscore(JsonObject entity) throws StartsWithUnderscoreException {
        if (entity != null) {
            for (Map.Entry<String, JsonElement> entry : entity.entrySet()) {
                String key = entry.getKey();

                if (key.startsWith("_") && !ignorableReservedAttributeNames.contains(key)) {
                    throw new StartsWithUnderscoreException(entry.getKey());
                }
            }
        }

        return entity;
    }
}
