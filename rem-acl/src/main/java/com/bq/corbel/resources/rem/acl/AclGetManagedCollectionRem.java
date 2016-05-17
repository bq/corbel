package com.bq.corbel.resources.rem.acl;

import com.bq.corbel.resources.rem.BaseRem;
import com.bq.corbel.resources.rem.request.CollectionParameters;
import com.bq.corbel.resources.rem.request.RequestParameters;
import com.bq.corbel.resources.rem.request.ResourceId;
import com.bq.corbel.resources.rem.request.ResourceParameters;
import com.bq.corbel.resources.rem.service.AclConfigurationService;

import java.net.URI;
import java.util.Optional;

import javax.ws.rs.core.Response;

public class AclGetManagedCollectionRem extends BaseRem<Void> {

    private final AclConfigurationService aclConfigurationService;

    public AclGetManagedCollectionRem(AclConfigurationService aclResourcesService) {
        this.aclConfigurationService = aclResourcesService;
    }

    @Override
    public Response collection(String type, RequestParameters<CollectionParameters> parameters, URI uri, Optional<Void> entity) {
        return aclConfigurationService.getConfigurations(parameters.getRequestedDomain());
    }

    @Override
    public Response resource(String type, ResourceId id, RequestParameters<ResourceParameters> parameters, Optional<Void> entity) {
        return aclConfigurationService.getConfiguration(id.getId(), parameters.getRequestedDomain());
    }

    @Override
    public Class<Void> getType() {
        return Void.class;
    }

}
