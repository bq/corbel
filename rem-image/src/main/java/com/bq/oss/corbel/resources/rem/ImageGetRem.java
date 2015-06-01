package com.bq.oss.corbel.resources.rem;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.StreamingOutput;

import org.apache.commons.io.output.TeeOutputStream;
import org.im4java.core.IM4JavaException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;

import com.bq.oss.corbel.resources.rem.exception.ImageOperationsException;
import com.bq.oss.corbel.resources.rem.request.RequestParameters;
import com.bq.oss.corbel.resources.rem.request.ResourceId;
import com.bq.oss.corbel.resources.rem.request.ResourceParameters;
import com.bq.oss.corbel.resources.rem.service.ImageCacheService;
import com.bq.oss.corbel.resources.rem.service.ImageOperationsService;
import com.bq.oss.corbel.resources.rem.service.RemService;
import com.bq.oss.lib.ws.api.error.ErrorResponseFactory;
import com.bq.oss.lib.ws.model.Error;


public class ImageGetRem extends BaseRem<Void> {

    private static final Logger LOG = LoggerFactory.getLogger(ImageGetRem.class);
    private static final String RESIZE_IMAGE_NAME = "resize";

    private final ImageOperationsService imageOperationsService;
    private final ImageCacheService imageCacheService;
    private RemService remService;

    public ImageGetRem(ImageOperationsService imageOperationsService, ImageCacheService imageCacheService) {
        this.imageOperationsService = imageOperationsService;
        this.imageCacheService = imageCacheService;
    }

    @Override
    public Response resource(String collection, ResourceId resourceId, RequestParameters<ResourceParameters> requestParameters,
            Optional<Void> entity) {

        Rem<?> restorGetRem = remService.getRem(collection, requestParameters.getAcceptedMediaTypes(), HttpMethod.GET,
                Collections.singletonList(this));

        if (restorGetRem == null) {
            LOG.warn("RESTOR not found. May be is needed to install it?");
            return ErrorResponseFactory.getInstance().notFound();
        }

        String operationsChain = requestParameters.getCustomParameterValue("operations");
        MediaType mediaType = requestParameters.getAcceptedMediaTypes().get(0);

        InputStream inputStream = imageCacheService.getFromCache(restorGetRem, resourceId, operationsChain, collection, requestParameters);

        if (inputStream != null) {
            return Response.ok(inputStream).type(javax.ws.rs.core.MediaType.valueOf(mediaType.toString())).build();
        }

        Response response = restorGetRem.resource(collection, resourceId, requestParameters, Optional.empty());

        @SuppressWarnings("unchecked")
        Rem<InputStream> restorPutRem = (Rem<InputStream>) remService.getRem(collection, requestParameters.getAcceptedMediaTypes(),
                HttpMethod.PUT);

        if (response.getStatus() != 200) {
            return response;
        }

        List<List<String>> operations;

        try {
            operations = getParameters(operationsChain);
        } catch (ImageOperationsException e) {
            return ErrorResponseFactory.getInstance().badRequest(new Error("bad_request", e.getMessage()));
        }

        StreamingOutput outputStream = output -> {

            File file = File.createTempFile(RESIZE_IMAGE_NAME, "");

            try (FileOutputStream fileOutputStream = new FileOutputStream(file);
                    TeeOutputStream teeOutputStream = new TeeOutputStream(output, fileOutputStream);
                    InputStream input = (InputStream) response.getEntity()) {
                imageOperationsService.applyConversion(operations, input, teeOutputStream);
            } catch (IOException | InterruptedException | IM4JavaException | ImageOperationsException e) {
                LOG.error("Error while resizing a image", e);
                throw new WebApplicationException(ErrorResponseFactory.getInstance().serverError(e));
            }

            imageCacheService.saveInCacheAsync(restorPutRem, resourceId, operationsChain, file.length(), collection, requestParameters,
                    file);
        };

        return Response.ok(outputStream).type(javax.ws.rs.core.MediaType.valueOf(mediaType.toString())).build();
    }

    @Override
    public Class<Void> getType() {
        return Void.class;
    }

    public void setRemService(RemService remService) {
        this.remService = remService;
    }

    public List<List<String>> getParameters(String parametersString) throws ImageOperationsException {
        List<List<String>> parameters = new LinkedList<>();

        for (String rawParameter : parametersString.split(";")) {
            String splittedParameter[] = rawParameter.split("=");

            if (splittedParameter.length != 2) {
                throw new ImageOperationsException("Invalid image operation: " + rawParameter);
            }

            parameters.add(Arrays.asList(splittedParameter[0], splittedParameter[1]));
        }

        return parameters;
    }
}
