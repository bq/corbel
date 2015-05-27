package com.bq.oss.corbel.resources.rem.request;

import java.util.List;
import java.util.Optional;

import com.bq.oss.lib.queries.request.ResourceQuery;

/**
 * @author Alexander De Leon
 */
public interface ResourceParameters {


    default Optional<List<ResourceQuery>> getConditions() {
        return Optional.empty();
    }

    default void setConditions(Optional<List<ResourceQuery>> resources) {}

}
