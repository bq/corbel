package com.bqreaders.silkroad.resources.rem.i18n;

import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import javax.ws.rs.core.Response;

import com.google.gson.JsonArray;
import org.eclipse.jetty.http.HttpStatus;
import org.springframework.http.HttpMethod;

import com.bq.oss.lib.queries.QueryNodeImpl;
import com.bq.oss.lib.queries.StringQueryLiteral;
import com.bq.oss.lib.queries.request.QueryOperator;
import com.bq.oss.lib.queries.request.ResourceQuery;
import com.bqreaders.silkroad.resources.rem.i18n.api.I18nErrorResponseFactory;
import com.bqreaders.silkroad.resources.rem.i18n.model.I18n;
import com.bq.oss.corbel.resources.rem.request.CollectionParameters;
import com.bq.oss.corbel.resources.rem.request.RequestParameters;
import com.bq.oss.corbel.resources.rem.request.ResourceId;
import com.bq.oss.corbel.resources.rem.request.ResourceParameters;

/**
 * Created by Francisco Sanchez on 15/04/15.
 */
public class I18nGetRem extends I18nBaseRem {

    @Override
    public Response collection(String type, RequestParameters<CollectionParameters> parameters, URI uri, Optional<I18n> entity) {
        return Optional.ofNullable(getLanguage(parameters.getHeaders())).map(languageHeader -> {
            List<ResourceQuery> baseQueries = parameters.getApiParameters().getQueries().orElse(Arrays.asList(new ResourceQuery()));
            for (String language : getProcessedLanguage(languageHeader)) {
                List<ResourceQuery> resourceQueries = new ArrayList<>();
                baseQueries.forEach(baseQuery -> {
                    ResourceQuery resourceQuery = new ResourceQuery();
                    baseQuery.forEach(resourceQuery::addQueryNode);
                    addQueryLanguage(language, resourceQuery);
                    resourceQueries.add(resourceQuery);
                });

                parameters.getApiParameters().setQueries(Optional.of(resourceQueries));

                Response response = getJsonRem(type, HttpMethod.GET).collection(type, parameters, uri, entity);

                if (response.getStatus() == HttpStatus.OK_200) {
                    JsonArray dict = (JsonArray) response.getEntity();
                    if (dict.size() > 0) {
                        return response;
                    }
                }
            }
            return I18nErrorResponseFactory.getInstance().notFound();
        }).orElse(I18nErrorResponseFactory.getInstance().errorNotLanguageHeader());
    }

    @Override
    public Response resource(String type, ResourceId id, RequestParameters<ResourceParameters> parameters, Optional<I18n> entity) {
        return Optional.ofNullable(getLanguage(parameters.getHeaders())).map(languageHeader -> {
            Response response;
            ResourceId newId;
            for (String language : getProcessedLanguage(languageHeader)) {
                newId = new ResourceId(language + ":" + id.getId());
                response = getJsonRem(type, HttpMethod.GET).resource(type, newId, parameters, entity);
                if (response.getStatus() == HttpStatus.OK_200) {
                    return response;
                }
            }
            return I18nErrorResponseFactory.getInstance().notFound();
        }).orElse(I18nErrorResponseFactory.getInstance().errorNotLanguageHeader());
    }

    private void addQueryLanguage(String language, ResourceQuery resourceQuery) {
        resourceQuery.addQueryNode(new QueryNodeImpl(QueryOperator.$LIKE, "id", new StringQueryLiteral(language + ":")));
    }

    @Override
    public Class<I18n> getType() {
        return I18n.class;
    }

}
