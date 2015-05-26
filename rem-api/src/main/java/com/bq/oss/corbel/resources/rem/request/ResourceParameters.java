package com.bq.oss.corbel.resources.rem.request;

import com.bq.oss.lib.queries.request.ResourceQuery;

import java.util.List;
import java.util.Optional;

/**
 * @author Alexander De Leon
 */
public interface ResourceParameters {

    Optional<List<ResourceQuery>> getQueries();
    ResourceParametersImpl setQueries(Optional<List<ResourceQuery>> queries);

}
