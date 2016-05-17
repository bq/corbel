package com.bq.corbel.resources.rem.resmi.ioc;

import com.bq.corbel.lib.config.ConfigurationIoC;
import com.bq.corbel.lib.mongo.IdInjectorMongoEventListener;
import com.bq.corbel.lib.mongo.JsonObjectMongoReadConverter;
import com.bq.corbel.lib.mongo.JsonObjectMongoWriteConverter;
import com.bq.corbel.lib.mongo.config.DefaultMongoConfiguration;
import com.bq.corbel.lib.queries.request.AggregationResultsFactory;
import com.bq.corbel.lib.queries.request.JsonAggregationResultsFactory;
import com.bq.corbel.resources.rem.Rem;
import com.bq.corbel.resources.rem.dao.DefaultResmiOrder;
import com.bq.corbel.resources.rem.dao.MongoResmiDao;
import com.bq.corbel.resources.rem.dao.NamespaceNormalizer;
import com.bq.corbel.resources.rem.dao.ResmiDao;
import com.bq.corbel.resources.rem.dao.ResmiOrder;
import com.bq.corbel.resources.rem.health.ElasticSearchHealthCheck;
import com.bq.corbel.resources.rem.resmi.ResmiDeleteRem;
import com.bq.corbel.resources.rem.resmi.ResmiGetRem;
import com.bq.corbel.resources.rem.resmi.ResmiPostRem;
import com.bq.corbel.resources.rem.resmi.ResmiPutRem;
import com.bq.corbel.resources.rem.search.DefaultElasticSearchService;
import com.bq.corbel.resources.rem.search.DefaultResmiSearch;
import com.bq.corbel.resources.rem.search.ElasticSearchService;
import com.bq.corbel.resources.rem.search.ResmiSearch;
import com.bq.corbel.resources.rem.service.DefaultNamespaceNormalizer;
import com.bq.corbel.resources.rem.service.DefaultResmiService;
import com.bq.corbel.resources.rem.service.InMemorySearchableFieldsRegistry;
import com.bq.corbel.resources.rem.service.ResmiService;
import com.bq.corbel.resources.rem.service.SearchableFieldsRegistry;
import com.bq.corbel.resources.rem.service.WithSearchResmiService;
import com.bq.corbel.resources.rem.utils.ResmiJsonObjectMongoWriteConverter;

import java.time.Clock;
import java.util.Arrays;

import org.elasticsearch.client.Client;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Lazy;
import org.springframework.core.env.Environment;
import org.springframework.data.mongodb.core.convert.CustomConversions;

import com.google.gson.Gson;
import com.google.gson.JsonElement;

/**
 * @author Cristian del Cerro
 */
@Configuration// Import configuration mechanism
@Import({ConfigurationIoC.class, DefaultElasticSearchConfiguration.class}) public class ResmiIoc extends DefaultMongoConfiguration {

    @Value("${resmi.elasticsearch.index.settings:/elasticsearch/index.settings}") private String elasticSearchIndexSettings;
    @Value("${resmi.elasticsearch.defaultMapping.settings:/elasticsearch/defaultMapping.settings}") private String elasticSearchDefaultMappingSettings;

    @Autowired private Environment env;
    @Autowired private ApplicationContext applicationContext;

    @Override
    protected String getDatabaseName() {
        return "resmi";
    }

    @Bean
    public ResmiDao mongoResmiDao(AggregationResultsFactory<JsonElement> aggregationResultsFactory) throws Exception {
        return new MongoResmiDao(mongoTemplate(), getJsonObjectMongoWriteConverter(), getNamespaceNormilizer(), getMongoResmiOrder(),
                aggregationResultsFactory);
    }

    @Bean
    public ResmiOrder getMongoResmiOrder() throws Exception {
        return new DefaultResmiOrder(mongoTemplate(), getNamespaceNormilizer());
    }

    @Bean(name = ResmiRemNames.RESMI_GET)
    public Rem getResmiGetRem(ResmiService resmiService) throws Exception {
        return new ResmiGetRem(resmiService);
    }

    @Bean(name = ResmiRemNames.RESMI_POST)
    public Rem getResmiPostRem(ResmiService resmiService) throws Exception {
        return new ResmiPostRem(resmiService);
    }

    @Bean(name = ResmiRemNames.RESMI_PUT)
    public Rem getResmiPutRem(ResmiService resmiService) throws Exception {
        return new ResmiPutRem(resmiService);
    }

    @Bean(name = ResmiRemNames.RESMI_DELETE)
    public Rem getResmiDeleteRem(ResmiService resmiService) throws Exception {
        return new ResmiDeleteRem(resmiService);
    }

    @Bean
    public IdInjectorMongoEventListener getIdInjectorMongoEventListener() {
        return new IdInjectorMongoEventListener();
    }

    @Override
    protected Environment getEnvironment() {
        return env;
    }

    @Override
    public CustomConversions customConversions() {
        return new CustomConversions(Arrays.asList(getJsonObjectMongoReadConverter(), getJsonObjectMongoWriteConverter()));
    }

    @Bean
    public JsonObjectMongoReadConverter getJsonObjectMongoReadConverter() {
        return new JsonObjectMongoReadConverter(getGson());
    }

    @Bean
    public JsonObjectMongoWriteConverter getJsonObjectMongoWriteConverter() {
        return new ResmiJsonObjectMongoWriteConverter();
    }

    @Bean
    public ResmiService resmiService(@Value("${resmi.elasticsearch.enabled:false}") boolean elasticSearchEnabled,
            AggregationResultsFactory<JsonElement> aggregationResultsFactory, ResmiDao resmiDao) throws Exception {
        if (elasticSearchEnabled) {
            return new WithSearchResmiService(resmiDao, resmiSearch(aggregationResultsFactory), getSearchableFieldsRegistry(), getGson(),
                    getClock());
        } else {
            return new DefaultResmiService(resmiDao, getClock());
        }
    }

    @Bean
    public Clock getClock() {
        return Clock.systemUTC();
    }

    @Bean
    @Lazy
    public SearchableFieldsRegistry getSearchableFieldsRegistry() {
        return new InMemorySearchableFieldsRegistry();
    }

    @Bean
    @Lazy
    public ResmiSearch resmiSearch(AggregationResultsFactory<JsonElement> aggregationResultsFactory) {
        return new DefaultResmiSearch(getElasticeSearchService(), getNamespaceNormilizer(), elasticSearchIndexSettings,
                aggregationResultsFactory, getClock(), elasticSearchDefaultMappingSettings);
    }

    @Bean
    @Lazy
    public ElasticSearchService getElasticeSearchService() {
        return new DefaultElasticSearchService(applicationContext.getBean(Client.class), getGson());
    }

    @Bean(name = ResmiRemNames.ELASTICSEARCH_HEALTHCHECK)
    @Lazy
    public ElasticSearchHealthCheck getElasticSearchHealthCheck() {
        return new ElasticSearchHealthCheck(applicationContext.getBean(Client.class));
    }

    @Bean
    public AggregationResultsFactory<JsonElement> aggregationResultsFactory(Gson gson) {
        return new JsonAggregationResultsFactory(gson);
    }

    @Bean
    public Gson getGson() {
        return new Gson();
    }

    @Bean
    public NamespaceNormalizer getNamespaceNormilizer() {
        return new DefaultNamespaceNormalizer();
    }
}
