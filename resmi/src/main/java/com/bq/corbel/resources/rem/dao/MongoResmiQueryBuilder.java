package com.bq.corbel.resources.rem.dao;

import com.bq.corbel.lib.queries.builder.QueryBuilder;
import com.bq.corbel.lib.queries.mongo.builder.CriteriaBuilder;
import com.bq.corbel.lib.queries.mongo.builder.MongoQueryBuilder;
import com.bq.corbel.lib.queries.request.ResourceQuery;
import com.bq.corbel.resources.rem.model.ResourceUri;
import com.bq.corbel.resources.rem.request.ResourceId;

import java.util.List;

import org.springframework.data.mongodb.core.query.Criteria;

/**
 * @author Alberto J. Rubio
 *
 */
public class MongoResmiQueryBuilder extends MongoQueryBuilder {

    DateQueryNodeTransformer transformer = new DateQueryNodeTransformer();

    public MongoResmiQueryBuilder id(String id) {
        query.addCriteria(Criteria.where("_id").is(id));
        return this;
    }

    public MongoResmiQueryBuilder relationSubjectId(ResourceUri resourceUri) {
        if (resourceUri.isRelation() && !resourceUri.isTypeWildcard()) {
            relationSubjectId(new ResourceId(resourceUri.getTypeId()));
        }
        return this;
    }

    public MongoResmiQueryBuilder relationSubjectId(ResourceId id) {
        if (!id.isWildcard()) {
            query.addCriteria(Criteria.where(JsonRelation._SRC_ID).is(id.getId()));
        }
        return this;
    }

    public MongoResmiQueryBuilder relationDestinationId(String id) {
        query.addCriteria(Criteria.where(JsonRelation._DST_ID).is(id));
        return this;
    }

    @Override
    public QueryBuilder query(ResourceQuery resourceQuery) {
        if (resourceQuery != null) {
            query.addCriteria(CriteriaBuilder.buildFromResourceQuery(resourceQuery, transformer));
        }
        return this;
    }

    @Override
    public QueryBuilder query(List<ResourceQuery> resourceQueries) {
        if (resourceQueries != null && !resourceQueries.isEmpty()) {
            query.addCriteria(CriteriaBuilder.buildFromResourceQueries(resourceQueries, transformer));
        }
        return this;
    }

}
