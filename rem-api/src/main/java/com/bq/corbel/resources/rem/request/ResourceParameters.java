package com.bq.corbel.resources.rem.request;

import java.util.List;
import java.util.Optional;

import com.bq.corbel.lib.queries.request.ResourceQuery;

/**
 * @author Alexander De Leon
 */
public interface ResourceParameters {

    Optional<List<ResourceQuery>> getConditions();

    void setConditions(Optional<List<ResourceQuery>> resources);

}
