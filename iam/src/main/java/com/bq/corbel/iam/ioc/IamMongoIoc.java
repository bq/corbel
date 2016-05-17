package com.bq.corbel.iam.ioc;

import com.bq.corbel.iam.repository.MongoIndexes;
import com.bq.corbel.iam.repository.ScopeRepository;
import com.bq.corbel.lib.mongo.JsonObjectMongoReadConverter;
import com.bq.corbel.lib.mongo.JsonObjectMongoWriteConverter;
import com.bq.corbel.lib.mongo.config.DefaultMongoConfiguration;
import com.bq.corbel.lib.queries.mongo.repository.QueriesRepositoryFactoryBean;
import com.bq.corbel.lib.ws.dw.ioc.MongoHealthCheckIoc;

import java.util.Arrays;
import java.util.concurrent.TimeUnit;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.concurrent.ConcurrentMapCache;
import org.springframework.cache.support.SimpleCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.core.env.Environment;
import org.springframework.data.mongodb.core.convert.CustomConversions;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;

import com.google.common.cache.CacheBuilder;
import com.google.gson.Gson;

/**
 * @author Alberto J. Rubio
 */
@Configuration @Import({MongoHealthCheckIoc.class}) @EnableMongoRepositories(value = {"com.bq.corbel.iam.repository",
        "com.bq.corbel.lib.token.repository"}, repositoryFactoryBeanClass = QueriesRepositoryFactoryBean.class) @EnableCaching public class IamMongoIoc
        extends DefaultMongoConfiguration {

    @Autowired private Environment env;

    @Override
    protected Environment getEnvironment() {
        return env;
    }

    @Override
    protected String getDatabaseName() {
        return "iam";
    }

    @Override
    public CustomConversions customConversions() {
        return new CustomConversions(Arrays.asList(new JsonObjectMongoReadConverter(getGson()), new JsonObjectMongoWriteConverter()));
    }

    @Bean
    public Gson getGson() {
        return new Gson();
    }

    @Bean
    public MongoIndexes getMongoIndexes() {
        return new MongoIndexes();
    }

    @Bean
    public CacheManager cacheManager(Environment env) {
        // configure and return an implementation of Spring's CacheManager SPI
        boolean allowNullValues = true;
        SimpleCacheManager cacheManager = new SimpleCacheManager();

        ConcurrentMapCache scopeCache = new ConcurrentMapCache(ScopeRepository.SCOPE_CACHE, CacheBuilder.newBuilder()
                .expireAfterWrite(env.getProperty("iam.cache.scopes.expiry", Long.class), TimeUnit.MINUTES).build().asMap(),
                allowNullValues);


        cacheManager.setCaches(Arrays.asList(scopeCache));
        return cacheManager;
    }
}
