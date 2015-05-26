package com.bq.oss.corbel.resources.rem.request;

import com.bq.oss.lib.queries.request.ResourceQuery;

import java.util.List;
import java.util.Optional;

/**
 * Created by Francisco Sanchez on 26/05/15.
 */
public class ResourceParametersImpl implements ResourceParameters {

    private Optional <List<ResourceQuery>> queries;

    public ResourceParametersImpl () {
        queries = Optional.empty();
    }

    public ResourceParametersImpl (Optional<List<ResourceQuery>> queries) {
        this.queries = queries;
    }

    @Override
    public Optional<List<ResourceQuery>> getQueries() {
        return queries;
    }

    @Override
    public ResourceParametersImpl setQueries(Optional<List<ResourceQuery>> queries) {
        this.queries = queries;
        return this;
    }

}
