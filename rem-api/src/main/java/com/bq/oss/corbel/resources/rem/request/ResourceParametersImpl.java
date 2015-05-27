package com.bq.oss.corbel.resources.rem.request;

import java.util.List;
import java.util.Optional;

import com.bq.oss.lib.queries.jaxrs.QueryParameters;
import com.bq.oss.lib.queries.parser.AggregationParser;
import com.bq.oss.lib.queries.parser.QueryParser;
import com.bq.oss.lib.queries.parser.SortParser;
import com.bq.oss.lib.queries.request.ResourceQuery;

/**
 * Created by Francisco Sanchez on 26/05/15.
 */
public class ResourceParametersImpl extends QueryParameters implements ResourceParameters {

    public ResourceParametersImpl(int pageSize, int page, int maxPageSize, Optional<String> sort, Optional<List<String>> query,
            QueryParser queryParser, Optional<String> aggregation, AggregationParser aggregationParser, SortParser sortParser,
            Optional<String> search) {
        super(pageSize, page, maxPageSize, sort, query, queryParser, aggregation, aggregationParser, sortParser, search);
    }

    public ResourceParametersImpl(QueryParameters queryParameters) {
        super(queryParameters);
    }

    @Override
    public Optional<List<ResourceQuery>> getConditions() {
        return Optional.empty();
    }

    @Override
    public void setConditions(Optional<List<ResourceQuery>> resources) {

    }
}
