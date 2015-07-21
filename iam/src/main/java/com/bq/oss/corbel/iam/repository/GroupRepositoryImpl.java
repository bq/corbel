package com.bq.oss.corbel.iam.repository;

import static org.springframework.data.mongodb.core.query.Criteria.where;
import static org.springframework.data.mongodb.core.query.Query.query;

import org.springframework.data.mongodb.core.MongoOperations;

public class GroupRepositoryImpl implements GroupRepositoryCustom {

    private final static String ID = "id";
    private final static String DOMAIN = "domain";

    private MongoOperations mongoOperations;

    public GroupRepositoryImpl(MongoOperations mongoOperations) {
        this.mongoOperations = mongoOperations;
    }

    @Override
    public void deleteByIdAndDomain(String id, String domain) {
        mongoOperations.remove(query(where(ID).is(id).and(DOMAIN).is(domain)));
    }

}
