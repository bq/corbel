package com.bq.oss.corbel.resources.rem;

import java.io.InputStream;
import java.util.Collections;
import java.util.Optional;

import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpMethod;

import com.bq.oss.corbel.resources.rem.request.RequestParameters;
import com.bq.oss.corbel.resources.rem.request.RequestParametersImpl;
import com.bq.oss.corbel.resources.rem.request.ResourceId;
import com.bq.oss.corbel.resources.rem.request.ResourceParameters;
import com.bq.oss.corbel.resources.rem.service.RemService;
import com.bq.oss.lib.ws.api.error.ErrorResponseFactory;

public class ImagePutRem extends BaseRem<InputStream> {

    private static final Logger LOG = LoggerFactory.getLogger(ImagePutRem.class);

    private final RemService remService;
    private final String cacheCollection;

    public ImagePutRem(RemService remService, String cacheCollection) {
        this.remService = remService;
        this.cacheCollection = cacheCollection;
    }

    @Override
    public Response resource(String collection, ResourceId resourceId, RequestParameters<ResourceParameters> requestParameters,
            Optional<InputStream> entity) {

        @SuppressWarnings("unchecked")
        Rem<InputStream> restorDeleteRem = (Rem<InputStream>) remService.getRem(collection, requestParameters.getAcceptedMediaTypes(),
                HttpMethod.DELETE, Collections.singletonList(this));

        @SuppressWarnings("unchecked")
        Rem<InputStream> restorPutRem = (Rem<InputStream>) remService.getRem(collection, requestParameters.getAcceptedMediaTypes(),
                HttpMethod.PUT, Collections.singletonList(this));

        if (restorDeleteRem == null || restorPutRem == null) {
            LOG.warn("RESTOR not found. May  be is needed to install it?");
            return ErrorResponseFactory.getInstance().notFound();
        }

        restorDeleteRem.resource(cacheCollection, resourceId, getRequestParametersWithPrefix(requestParameters), Optional.empty());
        restorPutRem.resource(collection, resourceId, requestParameters, entity);

        return Response.noContent().build();
    }

    public static RequestParameters<ResourceParameters> getRequestParametersWithPrefix(
            RequestParameters<ResourceParameters> requestParameters) {
        MultivaluedMap<String, String> newParameters = requestParameters.getParams();
        newParameters.putSingle("withPrefix", "true");

        return new RequestParametersImpl<>(requestParameters.getApiParameters(), requestParameters.getTokenInfo(),
                requestParameters.getAcceptedMediaTypes(), requestParameters.getContentLength(), newParameters,
                requestParameters.getHeaders());
    }

    @Override
    public Class<InputStream> getType() {
        return InputStream.class;
    }
}
