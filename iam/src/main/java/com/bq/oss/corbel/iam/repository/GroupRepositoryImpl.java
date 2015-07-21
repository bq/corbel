package com.bq.oss.corbel.iam.repository;

import static org.springframework.data.mongodb.core.query.Criteria.where;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.data.mongodb.core.query.Query;

import com.bq.oss.corbel.iam.model.Group;
import com.bq.oss.lib.queries.mongo.builder.MongoQueryBuilder;
import com.bq.oss.lib.queries.request.Pagination;
import com.bq.oss.lib.queries.request.ResourceQuery;
import com.bq.oss.lib.queries.request.Sort;

public class GroupRepositoryImpl extends HasScopesRepositoryBase<Group, String>implements GroupRepositoryCustom {

    @Autowired
    public GroupRepositoryImpl(MongoOperations mongoOperations) {
        super(mongoOperations, Group.class);
    }

    @Override
    public List<Group> findByDomain(String domain, List<ResourceQuery> resourceQueries, Pagination pagination, Sort sort) {
        Query query = new MongoQueryBuilder().query(resourceQueries).pagination(pagination).sort(sort).build()
                .addCriteria(where("domain").is(domain));
        return mongo.find(query, Group.class);
    }

}
