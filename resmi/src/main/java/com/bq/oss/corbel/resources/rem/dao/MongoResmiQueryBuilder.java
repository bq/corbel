package com.bq.oss.corbel.resources.rem.dao;

import org.springframework.data.mongodb.core.query.Criteria;

import com.bq.oss.corbel.resources.rem.model.ResourceUri;
import com.bq.oss.corbel.resources.rem.request.ResourceId;
import com.bq.oss.lib.queries.builder.QueryBuilder;
import com.bq.oss.lib.queries.mongo.builder.MongoQueryBuilder;

/**
 * @author Alberto J. Rubio
 *
 */
public class MongoResmiQueryBuilder extends MongoQueryBuilder {

    public MongoResmiQueryBuilder id(String id) {
        query.addCriteria(Criteria.where("_id").is(id));
        return this;
    }

    public MongoResmiQueryBuilder relationSubjectId(ResourceUri resourceUri) {
        if (resourceUri.isRelation()) {
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

}
