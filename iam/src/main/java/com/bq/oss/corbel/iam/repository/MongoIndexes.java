package com.bq.oss.corbel.iam.repository;

import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.mongodb.core.MongoOperations;

import com.bq.oss.corbel.iam.model.Identity;
import com.bq.oss.corbel.iam.model.User;
import com.bq.oss.corbel.iam.model.UserToken;
import com.bq.oss.lib.mongo.index.MongoIndex;

/**
 * @author Rubén Carrasco
 * 
 */
public class MongoIndexes {

    private static final Logger LOG = LoggerFactory.getLogger(MongoIndexes.class);

    public void ensureIndexes(MongoOperations mongo) {
        LOG.info("Creating mongo indexes");
        mongo.indexOps(Identity.class).ensureIndex(
                new MongoIndex().on("domain", Direction.ASC).on("oauthId", Direction.ASC).on("oauthService", Direction.ASC).unique()
                        .background().getIndexDefinition());
        mongo.indexOps(User.class).ensureIndex(
                new MongoIndex().on("domain", Direction.ASC).on("username", Direction.ASC).background().unique().getIndexDefinition());
        mongo.indexOps(User.class).ensureIndex(
                new MongoIndex().on("domain", Direction.ASC).on("email", Direction.ASC).background().unique().getIndexDefinition());
        mongo.indexOps(UserToken.class).ensureIndex(
                new MongoIndex().on(UserToken.EXPIRABLE_FIELD, Direction.ASC).expires(0, TimeUnit.SECONDS).background()
                        .getIndexDefinition());
    }

}
