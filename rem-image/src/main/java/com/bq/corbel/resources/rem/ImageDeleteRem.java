package com.bq.corbel.resources.rem;

import java.io.InputStream;
import java.net.URI;
import java.util.Optional;

import javax.ws.rs.core.Response;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.bq.corbel.lib.ws.api.error.ErrorResponseFactory;
import com.bq.corbel.resources.rem.plugin.RestorRemNames;
import com.bq.corbel.resources.rem.request.CollectionParameters;
import com.bq.corbel.resources.rem.request.RequestParameters;
import com.bq.corbel.resources.rem.request.ResourceId;
import com.bq.corbel.resources.rem.request.ResourceParameters;
import com.bq.corbel.resources.rem.service.ImageCacheService;

public class ImageDeleteRem extends ImageBaseRem {

    private static final Logger LOG = LoggerFactory.getLogger(ImageDeleteRem.class);
    private final ImageCacheService imageCacheService;

    public ImageDeleteRem(ImageCacheService imageCacheService) {
        this.imageCacheService = imageCacheService;
    }

    @Override
    public Response collection(String collection, RequestParameters<CollectionParameters> requestParameters, URI uri,
            Optional<InputStream> entity) {

        @SuppressWarnings("unchecked")
        Rem<InputStream> restorDeleteRem = remService.getRem(RestorRemNames.RESTOR_DELETE);

        if (restorDeleteRem == null) {
            LOG.warn("RESTOR not found. May  be is needed to install it?");
            return ErrorResponseFactory.getInstance().notFound();
        }

        imageCacheService.removeFromCollectionCache(restorDeleteRem, requestParameters, collection);
        restorDeleteRem.collection(collection, requestParameters, uri, Optional.empty());

        return Response.noContent().build();
    }

    @Override
    public Response resource(String collection, ResourceId resourceId, RequestParameters<ResourceParameters> requestParameters,
            Optional<InputStream> entity) {

        @SuppressWarnings("unchecked")
        Rem<InputStream> restorDeleteRem = remService.getRem(RestorRemNames.RESTOR_DELETE);

        if (restorDeleteRem == null) {
            LOG.warn("RESTOR not found. May  be is needed to install it?");
            return ErrorResponseFactory.getInstance().notFound();
        }

        imageCacheService.removeFromCache(restorDeleteRem, requestParameters, resourceId, collection);
        restorDeleteRem.resource(collection, resourceId, requestParameters, Optional.empty());

        return Response.noContent().build();
    }

    @Override
    public Class<InputStream> getType() {
        return InputStream.class;
    }
}
