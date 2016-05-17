package com.bq.corbel.resources.rem.acl;

import com.bq.corbel.lib.ws.api.error.ErrorResponseFactory;
import com.bq.corbel.resources.rem.BaseRem;
import com.bq.corbel.resources.rem.model.ManagedCollection;
import com.bq.corbel.resources.rem.request.RequestParameters;
import com.bq.corbel.resources.rem.request.ResourceId;
import com.bq.corbel.resources.rem.request.ResourceParameters;
import com.bq.corbel.resources.rem.service.AclConfigurationService;

import java.util.Optional;

import javax.ws.rs.core.Response;

public class AclPutManagedCollectionRem extends BaseRem<ManagedCollection> {

    private final AclConfigurationService aclConfigurationService;

    public AclPutManagedCollectionRem(AclConfigurationService aclConfigurationService) {
        this.aclConfigurationService = aclConfigurationService;
    }

    @Override
    public Response resource(String type, ResourceId id, RequestParameters<ResourceParameters> parameters,
            Optional<ManagedCollection> entity) {

        return entity.map(managedCollection -> {
            managedCollection.setDomain(parameters.getRequestedDomain());
            return aclConfigurationService.updateConfiguration(id.getId(), managedCollection);
        }).orElseGet(() -> ErrorResponseFactory.getInstance().badRequest());

    }

    @Override
    public Class<ManagedCollection> getType() {
        return ManagedCollection.class;
    }

}
