package io.corbel.resources.rem.search;

import io.corbel.lib.queries.request.AggregationResult;
import io.corbel.lib.queries.request.CountResult;
import io.corbel.lib.queries.request.Pagination;
import io.corbel.lib.queries.request.ResourceQuery;
import io.corbel.lib.queries.request.Sort;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

import org.elasticsearch.action.admin.indices.alias.IndicesAliasesRequest;
import org.elasticsearch.action.admin.indices.close.CloseIndexRequest;
import org.elasticsearch.action.admin.indices.create.CreateIndexRequest;
import org.elasticsearch.action.admin.indices.mapping.put.PutMappingRequest;
import org.elasticsearch.action.admin.indices.open.OpenIndexRequest;
import org.elasticsearch.action.search.SearchRequestBuilder;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.search.SearchType;
import org.elasticsearch.action.update.UpdateRequest;
import org.elasticsearch.client.Client;
import org.elasticsearch.script.ScriptService.ScriptType;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.bucket.terms.Terms;
import org.elasticsearch.search.aggregations.bucket.terms.TermsBuilder;
import org.elasticsearch.search.aggregations.metrics.tophits.TopHits;
import org.elasticsearch.search.aggregations.metrics.tophits.TopHitsBuilder;
import org.elasticsearch.search.sort.SortOrder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

/**
 * @author Rubén Carrasco
 *
 */
public class ElasticSearchService {

    private static final String RAW_EXTENSION = ".raw";
    private static final String RAW_EXTENSION_WILDCARD = "*.raw";
    private static final String TOP_HITS_KEY = "top";
    private static final String AGGREGATION_KEY = "agg";
    private static final String MUSTACHE = "mustache";
    private static final Logger LOG = LoggerFactory.getLogger(ElasticSearchResmiSearch.class);

    private final Client client;
    private final Gson gson;

    public ElasticSearchService(Client client, Gson gson) {
        this.client = client;
        this.gson = gson;
    }

    public boolean indexExists(String name) {
        return client.admin().indices().prepareExists(name).execute().actionGet().isExists();
    }

    public void createIndex(String name, String settings) {
        CreateIndexRequest indexRequest = new CreateIndexRequest(name).settings(settings);
        client.admin().indices().create(indexRequest).actionGet();
    }

    public void addAlias(String index, String alias) {
        IndicesAliasesRequest request = new IndicesAliasesRequest().addAlias(alias, index);
        client.admin().indices().aliases(request).actionGet();
    }

    public void removeAlias(String index, String alias) {
        IndicesAliasesRequest request = new IndicesAliasesRequest().removeAlias(alias, index);
        client.admin().indices().aliases(request).actionGet();
    }

    public void setupMapping(String index, String type, String source) {
        if (indexExists(index)) {
            client.admin().indices().close(new CloseIndexRequest(index)).actionGet();
            PutMappingRequest mappingRequest = new PutMappingRequest(index).type(type).source(source).ignoreConflicts(true);
            client.admin().indices().putMapping(mappingRequest).actionGet();
            client.admin().indices().open(new OpenIndexRequest(index)).actionGet();
        }
    }

    public void addTemplate(String name, String source) {
        client.preparePutIndexedScript(MUSTACHE, name, source).get();
    }

    public JsonArray search(String index, String type, String search, List<ResourceQuery> queries, Pagination pagination,
            Optional<Sort> sort) {
        SearchRequestBuilder request = client.prepareSearch(index).setTypes(type).setSearchType(SearchType.DFS_QUERY_THEN_FETCH)
                .setQuery(ElasticSearchResourceQueryBuilder.build(search, queries));

        if (sort.isPresent()) {
            request.addSort(sort.get().getField(), SortOrder.valueOf(sort.get().getDirection().name()));
        }
        request.setFetchSource(null, RAW_EXTENSION_WILDCARD);
        request.setFrom(pagination.getPage()).setSize(pagination.getPageSize());

        return extractResponse(request.execute().actionGet());
    }

    public JsonArray distinct(String index, String type, String search, List<ResourceQuery> queries, Pagination pagination,
            Optional<Sort> sort, List<String> fields) {
        SearchRequestBuilder request = client.prepareSearch(index).setTypes(type).setSearchType(SearchType.DFS_QUERY_THEN_FETCH)
                .setQuery(ElasticSearchResourceQueryBuilder.build(search, queries));

        if (sort.isPresent()) {
            request.addSort(sort.get().getField(), SortOrder.valueOf(sort.get().getDirection().name()));
        }

        if (fields == null || fields.isEmpty()) {
            throw new RuntimeException("Cannot build an aggregation from an empty list");
        }

        List<TermsBuilder> aggregations = fields.parallelStream()
                .map(f -> AggregationBuilders.terms(AGGREGATION_KEY).field(f + RAW_EXTENSION).size(0)).collect(Collectors.toList());

        request.addAggregation(buildAggregation(aggregations));

        request.setFrom(pagination.getPage()).setSize(pagination.getPageSize());

        return extractAggregationResponse(request.execute().actionGet());
    }

    private TermsBuilder buildAggregation(List<TermsBuilder> aggregations) {
        if (aggregations.size() == 1) {
            TopHitsBuilder topHits = AggregationBuilders.topHits(TOP_HITS_KEY).setSize(1).setFetchSource(null, RAW_EXTENSION_WILDCARD);
            return aggregations.get(0).subAggregation(topHits);
        } else {
            return aggregations.remove(0).subAggregation(buildAggregation(aggregations));
        }
    }

    private JsonArray extractAggregationResponse(SearchResponse response) {
        JsonArray hits = new JsonArray();
        getHits(response.getAggregations().get(AGGREGATION_KEY), hits);
        return hits;
    }

    private void getHits(Terms terms, JsonArray hits) {
        for (Terms.Bucket bucket : terms.getBuckets()) {
            TopHits topHits = bucket.getAggregations().get(TOP_HITS_KEY);
            if (topHits != null) {
                topHits.getHits().forEach(hit -> hits.add(gson.toJsonTree(hit.getSource())));
            } else {
                getHits(bucket.getAggregations().get(AGGREGATION_KEY), hits);
            }
        }
    }


    public long count(String index, String type, String search, List<ResourceQuery> queries) {
        return client.prepareCount(index).setTypes(type).setQuery(ElasticSearchResourceQueryBuilder.build(search, queries)).execute()
                .actionGet().getCount();
    }

    public JsonArray search(String index, String type, String templateName, Map<String, Object> templateParams, int page, int size) {
        return extractResponse(search(index, type, templateName, templateParams, Optional.of(page), Optional.of(size)));
    }

    public AggregationResult count(String index, String type, String templateName, Map<String, Object> templateParams) {
        SearchResponse response = search(index, type, templateName, templateParams, Optional.empty(), Optional.empty());
        return new CountResult(response.getHits().getTotalHits());
    }

    private SearchResponse search(String index, String type, String templateName, Map<String, Object> templateParams,
            Optional<Integer> page, Optional<Integer> size) {
        SearchRequestBuilder request = client.prepareSearch(index).setTypes(type).setSearchType(SearchType.DFS_QUERY_THEN_FETCH)
                .setTemplateName(templateName).setTemplateType(ScriptType.INDEXED).setTemplateParams(templateParams);
        if (page.isPresent() && size.isPresent()) {
            request.setFrom(page.get()).setSize(size.get());
        }
        return request.execute().actionGet();
    }

    private JsonArray extractResponse(SearchResponse response) {
        JsonArray jsonArray = new JsonArray();
        response.getHits().forEach(hit -> jsonArray.add(gson.toJsonTree(hit.getSource())));
        return jsonArray;
    }

    public void indexDocument(String index, String type, String id, JsonObject source) {
        // JsonObject copy = new JsonObject();
        // source.entrySet().forEach(e -> {
        // copy.add(e.getKey(), e.getValue());
        // copy.add(e.getKey() + RAW_EXTENSION, e.getValue());
        // });
        UpdateRequest updateRequest = new UpdateRequest(index, type, id).doc(source.toString());
        updateRequest.docAsUpsert(true);
        try {
            client.update(updateRequest).get();
        } catch (InterruptedException | ExecutionException e) {
            LOG.error(e.getMessage());
        }
    }

    public void deleteDocument(String index, String type, String id) {
        client.prepareDelete(index, type, id).execute().actionGet();
    }
}
