package com.bq.corbel.resources.rem.service;

import java.io.File;
import java.io.InputStream;
import java.util.Optional;

import com.bq.corbel.resources.rem.Rem;
import com.bq.corbel.resources.rem.format.ImageFormat;
import com.bq.corbel.resources.rem.request.CollectionParameters;
import com.bq.corbel.resources.rem.request.RequestParameters;
import com.bq.corbel.resources.rem.request.ResourceId;
import com.bq.corbel.resources.rem.request.ResourceParameters;

public interface ImageCacheService {

    InputStream getFromCache(Rem<?> restorRem, ResourceId resourceId, String operationsChain, Optional<ImageFormat> imageFormat,
            String type, RequestParameters<ResourceParameters> parameters);

    void saveInCacheAsync(Rem<InputStream> restorPutRem, ResourceId resourceId, String operationsChain, Optional<ImageFormat> imageFormat,
            Long newSize, String collection, RequestParameters<ResourceParameters> parameters, File file);

    void removeFromCache(Rem<InputStream> restorDeleteRem, RequestParameters<ResourceParameters> parameters, ResourceId resourceId,
            String collection);

    void removeFromCollectionCache(Rem<InputStream> restorDeleteRem, RequestParameters<CollectionParameters> parameters, String collection);

}
