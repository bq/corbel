package io.corbel.resources.rem.search;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import org.elasticsearch.index.query.*;

import io.corbel.lib.queries.request.QueryLiteral;
import io.corbel.lib.queries.request.QueryNode;
import io.corbel.lib.queries.request.ResourceQuery;

/**
 * @author Rubén Carrasco
 *
 */
public class ElasticSearchResourceQueryBuilder {

    public static QueryBuilder build(String search, ResourceQuery query) {
        return build(search, query != null ? Collections.singletonList(query) : Collections.emptyList());
    }

    public static QueryBuilder build(String search, List<ResourceQuery> queries) {
        if (queries == null || queries.isEmpty()) {
            return QueryBuilders.queryStringQuery(search);
        } else {
            OrQueryBuilder orBuilder = QueryBuilders.orQuery();
            for (ResourceQuery query : queries) {
                orBuilder.add(getFilterBuilder(query));
            }
            return QueryBuilders.filteredQuery(QueryBuilders.queryStringQuery(search), orBuilder);
        }
    }

    private static QueryBuilder getFilterBuilder(ResourceQuery query) {
        AndQueryBuilder andFilterBuilder = QueryBuilders.andQuery();
        for (QueryNode node : query) {
            andFilterBuilder.add(getFilterBuilder(node));
        }
        return andFilterBuilder;
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    private static QueryBuilder getFilterBuilder(QueryNode node) {
        switch (node.getOperator()) {
            case $EQ:
                return QueryBuilders.termQuery(node.getField(), node.getValue().getLiteral());
            case $NE:
                return QueryBuilders.notQuery(QueryBuilders.termQuery(node.getField(), node.getValue().getLiteral()));
            case $GT:
                return QueryBuilders.rangeQuery(node.getField()).gt(node.getValue().getLiteral());
            case $GTE:
                return QueryBuilders.rangeQuery(node.getField()).gte(node.getValue().getLiteral());
            case $LT:
                return QueryBuilders.rangeQuery(node.getField()).lt(node.getValue().getLiteral());
            case $LTE:
                return QueryBuilders.rangeQuery(node.getField()).lte(node.getValue().getLiteral());
            case $EXISTS:
                return (Boolean) node.getValue().getLiteral() ? QueryBuilders.existsQuery(node.getField())
                        : QueryBuilders.notQuery(QueryBuilders.existsQuery(node.getField()));
            case $IN:
                return QueryBuilders.termsQuery(node.getField(), getValues((List<QueryLiteral>) node.getValue().getLiteral()));
            case $NIN:
                return QueryBuilders.notQuery(QueryBuilders.termsQuery(node.getField(), getValues((List<QueryLiteral>) node.getValue().getLiteral())));
            default:
                return null;
        }
    }

    private static Object[] getValues(@SuppressWarnings("rawtypes") List<QueryLiteral> literals) {
        return literals.stream().map(QueryLiteral::getLiteral).collect(Collectors.toList()).toArray();
    }
}
