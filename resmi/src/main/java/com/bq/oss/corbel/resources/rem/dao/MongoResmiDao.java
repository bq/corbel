package com.bq.oss.corbel.resources.rem.dao;

import com.bq.oss.corbel.resources.rem.model.GenericDocument;
import com.bq.oss.corbel.resources.rem.model.ResourceUri;
import com.bq.oss.corbel.resources.rem.utils.JsonUtils;
import com.bq.oss.lib.mongo.JsonObjectMongoWriteConverter;
import com.bq.oss.lib.mongo.utils.GsonUtil;
import com.bq.oss.lib.queries.request.*;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.mongodb.core.FindAndModifyOptions;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.AggregationOperation;
import org.springframework.data.mongodb.core.index.Index;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

/**
 * @author Alberto J. Rubio
 *
 */
public class MongoResmiDao implements ResmiDao {

    private static final String ID = "id";
    private static final String _ID = "_id";

    private static final String RELATION_CONCATENATOR = ".";
    private static final Logger LOG = LoggerFactory.getLogger(MongoResmiDao.class);
    private static final String EMPTY_STRING = "";
    private static final String EXPIRE_AT = "_expireAt";
    private static final String CREATED_AT = "_createdAt";

    private final MongoOperations mongoOperations;
    private final JsonObjectMongoWriteConverter jsonObjectMongoWriteConverter;
    private final NamespaceNormalizer namespaceNormalizer;
    private final ResmiOrder resmiOrder;

    public MongoResmiDao(MongoOperations mongoOperations, JsonObjectMongoWriteConverter jsonObjectMongoWriteConverter,
            NamespaceNormalizer namespaceNormalizer, ResmiOrder resmiOrder) {
        this.mongoOperations = mongoOperations;
        this.jsonObjectMongoWriteConverter = jsonObjectMongoWriteConverter;
        this.namespaceNormalizer = namespaceNormalizer;
        this.resmiOrder = resmiOrder;
    }

    @Override
    public boolean exists(String type, String id) {
        return mongoOperations.exists(Query.query(Criteria.where(_ID).is(id)), namespaceNormalizer.normalize(type));
    }

    @Override
    public JsonObject findResource(ResourceUri uri) {
        return mongoOperations.findById(uri.getTypeId(), JsonObject.class, getMongoCollectionName(uri));
    }

    @Override
    public JsonArray findCollection(ResourceUri uri, Optional<List<ResourceQuery>> resourceQueries, Pagination pagination,
            Optional<Sort> sort) {
        Query query = new MongoResmiQueryBuilder().query(resourceQueries.orElse(null)).pagination(pagination).sort(sort.orElse(null))
                .build();
        LOG.debug("findCollection Query executed : " + query.getQueryObject().toString());
        return JsonUtils.convertToArray(mongoOperations.find(query, JsonObject.class, getMongoCollectionName(uri)));
    }

    @Override
    public JsonElement findRelation(ResourceUri uri, Optional<List<ResourceQuery>> resourceQueries, Pagination pagination,
            Optional<Sort> sort) {
        MongoResmiQueryBuilder mongoResmiQueryBuilder = new MongoResmiQueryBuilder();

        if (uri.getRelationId() != null) {
            mongoResmiQueryBuilder.relationDestinationId(uri.getRelationId());
        }

        Query query = mongoResmiQueryBuilder.relationSubjectId(uri).query(resourceQueries.orElse(null)).pagination(pagination)
                .sort(sort.orElse(null)).build();
        query.fields().exclude(_ID).exclude(JsonRelation._SRC_ID);
        LOG.debug("findRelation Query executed : " + query.getQueryObject().toString());
        JsonArray result = renameIds(JsonUtils.convertToArray(mongoOperations.find(query, JsonObject.class, getMongoCollectionName(uri))));

        if (uri.getRelationId() != null && result.size() == 1) {
            return result.get(0);
        } else {
            return result;
        }
    }

    @Override
    public <T> List<T> findAll(String type, Class<T> entityClass) {
        return mongoOperations.findAll(entityClass, namespaceNormalizer.normalize(type));
    }

    @Override
    public void updateResource(ResourceUri uri, JsonObject entity) {
        findAndModify(getMongoCollectionName(uri), Optional.of(uri.getTypeId()), entity, true, Optional.empty());
    }

    @Override
    public boolean conditionalUpdateResource(ResourceUri uri, JsonObject entity, List<ResourceQuery> resourceQueries) {
        JsonObject saved = findAndModify(getMongoCollectionName(uri), Optional.of(uri.getTypeId()), entity, false, Optional.of(resourceQueries));
        return saved != null;
    }

    private JsonObject findAndModify(String collection, Optional<String> id, JsonObject entity, boolean upsert, Optional<List<ResourceQuery>> resourceQueries) {
        JsonElement created = entity.remove(CREATED_AT);

        Update update = updateFromJsonObject(entity, id, Optional.ofNullable(created));


        Query query = Query.query(Criteria.where(_ID).exists(false));
        if (id.isPresent()) {
            MongoResmiQueryBuilder builder = new MongoResmiQueryBuilder().id(id.get());
            if (resourceQueries.isPresent()) {
                builder.query(resourceQueries.get());
            }
            query = builder.build();
        }

        JsonObject saved = mongoOperations.findAndModify(query, update, FindAndModifyOptions.options().upsert(upsert).returnNew(true),
                JsonObject.class, collection);

        entity.addProperty(ID, id.isPresent() ? id.get() : saved.get(ID).getAsString());

        if (created != null) {
            entity.add(CREATED_AT, created);
        }
        return saved;
    }

    @Override
    public void saveResource(ResourceUri uri, Object entity) {
        mongoOperations.save(entity, getMongoCollectionName(uri));
    }



    @SuppressWarnings("unchecked")
    private Update updateFromJsonObject(JsonObject entity, Optional<String> id, Optional<JsonElement> created) {
        Update update = new Update();

        if (id.isPresent()) {
            entity.remove(ID);
            if (entity.entrySet().isEmpty()) {
                update.set(_ID, id);
            }
        }

        if (created.isPresent() && created.get().isJsonPrimitive()) {
            update.setOnInsert(CREATED_AT, GsonUtil.getPrimitive(created.get().getAsJsonPrimitive()));
        }

        jsonObjectMongoWriteConverter.convert(entity).toMap().forEach((key, value) -> update.set((String) key, value));
        entity.entrySet().stream().filter((entry) -> entry.getValue().isJsonNull()).forEach((entry) -> update.unset(entry.getKey()));
        return update;
    }

    @Override
    public void createRelation(ResourceUri uri, JsonObject entity) throws NotFoundException {
        if (!exists(uri.getType(), uri.getTypeId())) {
            throw new NotFoundException("The resource does not exist");
        }

        JsonObject storedRelation = findRelation(uri);
        String id = null;

        JsonObject relationJson = JsonRelation.create(uri.getTypeId(), uri.getRelationId(), entity);
        if (storedRelation != null) {
            id = storedRelation.get(ID).getAsString();
            relationJson = updateRelation(storedRelation, relationJson);
        } else {
            resmiOrder.addNextOrderInRelation(uri.getType(), uri.getTypeId(), uri.getRelation(), relationJson);
        }

        findAndModify(getMongoCollectionName(uri), Optional.ofNullable(id), relationJson, true, Optional.empty());
    }

    private JsonObject findRelation(ResourceUri uri) {
        Criteria criteria = Criteria.where(JsonRelation._SRC_ID).is(uri.getTypeId()).and(JsonRelation._DST_ID).is(uri.getRelationId());
        return mongoOperations.findOne(new Query(criteria), JsonObject.class, getMongoCollectionName(uri));
    }

    @Override
    public void ensureExpireIndex(ResourceUri uri) {
        mongoOperations.indexOps(getMongoCollectionName(uri)).ensureIndex(new Index().on(EXPIRE_AT, Direction.ASC).expire(0));
    }

    @Override
    public void ensureIndex(ResourceUri uri, Index index) {
        mongoOperations.indexOps(getMongoCollectionName(uri)).ensureIndex(index);
    }

    private JsonObject updateRelation(JsonObject storedRelation, JsonObject relationJson) {
        relationJson.add("id", storedRelation.get("id"));
        relationJson.add("_order", storedRelation.get("_order"));
        return relationJson;
    }



    /*
     * TODO: This should be refactor out of here (alex 31.01.14)
     */
    private JsonArray renameIds(JsonArray array) {
        for (JsonElement element : array) {
            if (element.isJsonObject()) {
                JsonObject object = element.getAsJsonObject();
                renameIds(object);
            }
        }
        return array;
    }

    private JsonElement renameIds(JsonObject object) {
        object.add("id", object.get(JsonRelation._DST_ID));
        object.remove(JsonRelation._DST_ID);
        return object;
    }

    @Override
    public JsonObject deleteResource(ResourceUri uri) {
        Criteria criteria = Criteria.where(_ID).is(uri.getTypeId());
        return findAndRemove(uri, criteria);
    }

    @Override
    public List<GenericDocument> deleteCollection(ResourceUri uri, Optional<List<ResourceQuery>> queries) {
        List<ResourceQuery> resourceQueries = queries.orElse(Collections.<ResourceQuery>emptyList());
        Criteria criteria = new MongoResmiQueryBuilder().getCriteriaFromResourceQueries(resourceQueries);
        return findAllAndRemove(uri, criteria);
    }

    @Override
    public List<GenericDocument> deleteRelation(ResourceUri uri) {
        Criteria criteria = new Criteria();
        if (!uri.isTypeWildcard()) {
            criteria = criteria.and(JsonRelation._SRC_ID).is(uri.getTypeId());
        }
        if (uri.getRelationId() != null) {
            criteria = criteria.and(JsonRelation._DST_ID).is(uri.getRelationId());
        }

        return findAllAndRemove(uri, criteria);
    }

    private List<GenericDocument> findAllAndRemove(ResourceUri resourceUri, Criteria criteria) {
        return mongoOperations.findAllAndRemove(new Query(criteria), GenericDocument.class, getMongoCollectionName(resourceUri));
    }

    private JsonObject findAndRemove(ResourceUri resourceUri, Criteria criteria) {
        return mongoOperations.findAndRemove(new Query(criteria), JsonObject.class, getMongoCollectionName(resourceUri));
    }


    @Override
    public void moveRelation(ResourceUri uri, RelationMoveOperation relationMoveOperation) {
        resmiOrder.moveRelation(uri, relationMoveOperation);
    }


    @Override
    public CountResult count(ResourceUri resourceUri, List<ResourceQuery> resourceQueries) {
        Query query = new MongoResmiQueryBuilder().relationSubjectId(resourceUri).query(resourceQueries).build();
        if (resourceUri.isRelation()) {
            query.fields().exclude(_ID).exclude(JsonRelation._SRC_ID);
        }
        LOG.debug("Query executed : " + query.getQueryObject().toString());
        return new CountResult(mongoOperations.count(query, getMongoCollectionName(resourceUri)));
    }

    @Override
    public AverageResult average(ResourceUri resourceUri, List<ResourceQuery> resourceQueries, String field) {
        List<AggregationOperation> aggregations = new ArrayList<>();
        aggregations.add(Aggregation.match(new MongoResmiQueryBuilder().getCriteriaFromResourceQueries(resourceQueries)));
        aggregations.add(Aggregation.group().avg(field).as("average"));

        return mongoOperations
                .aggregate(Aggregation.newAggregation(aggregations), getMongoCollectionName(resourceUri), AverageResult.class)
                .getUniqueMappedResult();
    }

    @Override
    public SumResult sum(ResourceUri resourceUri, List<ResourceQuery> resourceQueries, String field) {
        List<AggregationOperation> aggregations = new ArrayList<>();
        aggregations.add(Aggregation.match(new MongoResmiQueryBuilder().getCriteriaFromResourceQueries(resourceQueries)));
        aggregations.add(Aggregation.group().sum(field).as("sum"));

        return mongoOperations.aggregate(Aggregation.newAggregation(aggregations), getMongoCollectionName(resourceUri), SumResult.class)
                .getUniqueMappedResult();
    }

    private String getMongoCollectionName(ResourceUri resourceUri) {
        return Optional
                .ofNullable(namespaceNormalizer.normalize(resourceUri.getType()))
                .map(type -> type
                        + Optional.ofNullable(resourceUri.getRelation())
                        .map(relation -> RELATION_CONCATENATOR + namespaceNormalizer.normalize(relation)).orElse(EMPTY_STRING))
                .orElse(EMPTY_STRING);
    }
}
