package com.bq.corbel.resources.rem.restor;

import com.bq.corbel.lib.ws.api.error.ErrorResponseFactory;
import com.bq.corbel.resources.rem.dao.RestorDao;
import com.bq.corbel.resources.rem.model.RestorObject;
import com.bq.corbel.resources.rem.model.RestorResourceUri;
import com.bq.corbel.resources.rem.request.RequestParameters;
import com.bq.corbel.resources.rem.request.ResourceId;
import com.bq.corbel.resources.rem.request.ResourceParameters;

import java.io.InputStream;
import java.util.Optional;

import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Response;

/**
 * @author Rubén Carrasco
 */
public class RestorGetRem extends AbstractRestorRem {

    public RestorGetRem(RestorDao dao) {
        super(dao);
    }

    @Override
    public Response resource(String collection, ResourceId resource, RequestParameters<ResourceParameters> parameters,
            Optional<InputStream> entity) {

        RestorResourceUri resourceUri = new RestorResourceUri(parameters.getRequestedDomain(), getMediaType(parameters), collection,
                resource.getId());

        RestorObject object = dao.getObject(resourceUri);
        if (object != null) {
            return Response.ok().type(object.getMediaType().toString()).entity(object.getInputStream())
                    .header(HttpHeaders.CONTENT_LENGTH, object.getContentLength()).header(HttpHeaders.ETAG, object.getEtag()).build();
        }
        return ErrorResponseFactory.getInstance().notFound();
    }
}
