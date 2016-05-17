package com.bq.corbel.resources.rem.dao;

import java.util.Date;
import java.util.function.Function;

import com.bq.corbel.lib.queries.DateQueryLiteral;
import com.bq.corbel.lib.queries.LongQueryLiteral;
import com.bq.corbel.lib.queries.QueryNodeImpl;
import com.bq.corbel.lib.queries.request.QueryLiteral;
import com.bq.corbel.lib.queries.request.QueryNode;
import com.bq.corbel.lib.queries.request.QueryOperator;

/**
 * @author Rubén Carrasco
 *
 */
public class DateQueryNodeTransformer implements Function<QueryNode, QueryNode> {

    private enum VALID_OPERATORS { $EQ, $GT, $GTE, $LT, $LTE, $NE }

    @Override
    public QueryNode apply(QueryNode t) {
        return new QueryNodeImpl(t.getOperator(), t.getField(), transformDateValue(t.getField(), t.getOperator(), t.getValue()));
    }

    private QueryLiteral<?> transformDateValue(String field, QueryOperator operator, QueryLiteral<?> value) {
        if (field.equals(ReservedFields._UPDATED_AT) || field.equals(ReservedFields._CREATED_AT)) {
            if (VALID_OPERATORS.valueOf(operator.toString()) != null) {
                try {
                    return new DateQueryLiteral(new Date(((LongQueryLiteral) value).getLiteral()));
                } catch (ClassCastException ignored) {}
            }
        }
        return value;
    }
}
