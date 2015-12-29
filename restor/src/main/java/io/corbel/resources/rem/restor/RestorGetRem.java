package io.corbel.resources.rem.restor;

import java.io.InputStream;
import java.util.Optional;

import javax.ws.rs.core.Response;

import io.corbel.lib.ws.api.error.ErrorResponseFactory;
import io.corbel.resources.rem.model.RestorResourceUri;
import io.corbel.resources.rem.request.RequestParameters;
import io.corbel.resources.rem.dao.RestorDao;
import io.corbel.resources.rem.request.ResourceId;
import io.corbel.resources.rem.request.ResourceParameters;
import io.corbel.resources.rem.model.RestorObject;

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

        RestorResourceUri resourceUri = new RestorResourceUri(parameters.getRequestDomain(), getMediaType(parameters), collection, resource.getId());

        RestorObject object = dao.getObject(resourceUri);
		if (object != null) {
			return Response.ok().type(object.getMediaType().toString()).entity(object.getInputStream()).build();
		}
		return ErrorResponseFactory.getInstance().notFound();
	}
}
