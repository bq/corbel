package io.corbel.resources.rem.acl;

import java.net.URI;
import java.util.Optional;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;

import io.corbel.lib.ws.api.error.ErrorResponseFactory;
import io.corbel.resources.rem.BaseRem;
import io.corbel.resources.rem.model.ManagedCollection;
import io.corbel.resources.rem.request.*;
import io.corbel.resources.rem.service.AclResourcesService;

public class AclPostManagedCollectionRem extends BaseRem<ManagedCollection> {

    private final AclResourcesService aclResourcesService;

    public AclPostManagedCollectionRem(AclResourcesService aclResourcesService) {
        this.aclResourcesService = aclResourcesService;
    }

    @Override
    public Response collection(String type, RequestParameters<CollectionParameters> parameters, URI uri,
            Optional<ManagedCollection> entity) {

        return entity.map(managedCollection -> {
            ResourceParameters resourceParameters = new ResourceParametersImpl(null, Optional.empty(), Optional.empty(), Optional.empty(),
                    Optional.empty(), Optional.empty());
            RequestParameters<ResourceParameters> requestParameters = new RequestParametersImpl<>(resourceParameters,
                    parameters.getTokenInfo(), parameters.getAcceptedMediaTypes(), parameters.getContentLength(), parameters.getParams(),
                    parameters.getHeaders());
            String id = managedCollection.getId();
            Response response = aclResourcesService.updateConfiguration(new ResourceId(id), requestParameters, managedCollection);

            if (response.getStatus() != Response.Status.NO_CONTENT.getStatusCode()) {
                return response;
            }

            return Response.created(UriBuilder.fromUri(uri).path("/{id}").build(id)).build();
        }).orElseGet(() -> ErrorResponseFactory.getInstance().badRequest());

    }

    @Override
    public Class<ManagedCollection> getType() {
        return ManagedCollection.class;
    }

}
