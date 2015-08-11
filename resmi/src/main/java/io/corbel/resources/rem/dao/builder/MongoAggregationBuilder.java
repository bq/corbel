package io.corbel.resources.rem.dao.builder;

import io.corbel.lib.queries.mongo.builder.MongoQueryBuilder;
import io.corbel.lib.queries.request.Pagination;
import io.corbel.lib.queries.request.ResourceQuery;
import io.corbel.lib.queries.request.Sort;
import io.corbel.resources.rem.resmi.exception.MongoAggregationException;

import java.util.ArrayList;
import java.util.List;

import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.AggregationOperation;
import org.springframework.data.mongodb.core.aggregation.GroupOperation;

/**
 * @author Rubén Carrasco
 *
 */
public class MongoAggregationBuilder {

    public static final String REFERENCE = "first";
    private final List<AggregationOperation> operations;

    public MongoAggregationBuilder() {
        operations = new ArrayList<AggregationOperation>();
    }

    public MongoAggregationBuilder match(List<ResourceQuery> resourceQueries) {
        if (resourceQueries != null && !resourceQueries.isEmpty()) {
            operations.add(Aggregation.match(new MongoQueryBuilder().getCriteriaFromResourceQueries(resourceQueries)));
        }
        return this;
    }

    public MongoAggregationBuilder sort(Sort sort) {
        operations.add(Aggregation.sort(Direction.fromString(sort.getDirection().name()), sort.getField()));
        return this;
    }

    public MongoAggregationBuilder group(List<String> fields) {
        return group(fields, false);
    }

    public MongoAggregationBuilder group(List<String> fields, boolean first) {
        if (fields != null && !fields.isEmpty()) {
            GroupOperation group = Aggregation.group(fields.toArray(new String[fields.size()]));
            if (first) {
                group = group.first(Aggregation.ROOT).as(REFERENCE);
            }
            operations.add(group);
        }
        return this;
    }

    public MongoAggregationBuilder pagination(Pagination pagination) {
        operations.add(Aggregation.skip(pagination.getPage() * pagination.getPageSize()));
        operations.add(Aggregation.limit(pagination.getPageSize()));
        return this;
    }

    public Aggregation build() throws MongoAggregationException {
        if (operations.isEmpty()) {
            throw new MongoAggregationException("Cannot build aggregation without operations");
        }
        return Aggregation.newAggregation(operations.toArray(new AggregationOperation[operations.size()]));
    }
}
