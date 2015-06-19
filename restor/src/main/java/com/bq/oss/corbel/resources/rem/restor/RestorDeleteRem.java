package com.bq.oss.corbel.resources.rem.restor;

import java.io.InputStream;
import java.net.URI;
import java.util.Optional;

import javax.ws.rs.core.Response;

import com.bq.oss.corbel.resources.rem.dao.RestorDao;
import com.bq.oss.corbel.resources.rem.request.CollectionParameters;
import com.bq.oss.corbel.resources.rem.request.RequestParameters;
import com.bq.oss.corbel.resources.rem.request.ResourceId;
import com.bq.oss.corbel.resources.rem.request.ResourceParameters;

/**
 * @author Rubén Carrasco
 */
public class RestorDeleteRem extends AbstractRestorRem {

    public RestorDeleteRem(RestorDao dao) {
        super(dao);
    }

    @Override
    public Response collection(String type, RequestParameters<CollectionParameters> parameters, URI uri, Optional<InputStream> entity) {

        return Optional.ofNullable(parameters.getCustomParameterValue("prefix")).map(prefix -> {
            dao.deleteObjectWithPrefix(getMediaType(parameters), type, prefix);
            return Response.noContent().build();
        }).orElseGet(() -> super.collection(type, parameters, uri, entity));

    }

    @Override
    public Response resource(String collection, ResourceId resource, RequestParameters<ResourceParameters> parameters,
            Optional<InputStream> entity) {

        dao.deleteObject(getMediaType(parameters), collection, resource.getId());
        return Response.noContent().build();

    }

}
