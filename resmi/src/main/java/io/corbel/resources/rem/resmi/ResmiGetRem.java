package io.corbel.resources.rem.resmi;

import java.net.URI;
import java.util.Optional;

import javax.ws.rs.core.Response;

import com.google.gson.JsonObject;

import io.corbel.lib.ws.api.error.ErrorResponseFactory;
import io.corbel.lib.ws.model.Error;
import io.corbel.resources.rem.model.ResourceUri;
import io.corbel.resources.rem.request.*;
import io.corbel.resources.rem.service.BadConfigurationException;
import io.corbel.resources.rem.service.ResmiService;

/**
 * @author Rubén Carrasco
 * 
 */
public class ResmiGetRem extends AbstractResmiRem {

    public ResmiGetRem(ResmiService resmiService) {
        super(resmiService);
    }

    @Override
    public Response collection(String type, RequestParameters<CollectionParameters> parameters, URI uri, Optional<JsonObject> entity) {
        ResourceUri resourceUri = buildCollectionUri(type);
        try {
            if (parameters.getOptionalApiParameters().flatMap(CollectionParameters::getAggregation).isPresent()) {
                return buildResponse(resmiService.aggregate(resourceUri, parameters.getOptionalApiParameters().get()));
            } else {
                return buildResponse(resmiService.findCollection(resourceUri, parameters.getOptionalApiParameters()));
            }

        } catch (BadConfigurationException bce) {
            return ErrorResponseFactory.getInstance().badRequest(new Error("bad_request", bce.getMessage()));
        } catch (Exception e) {
            return ErrorResponseFactory.getInstance().badRequest();
        }
    }

    @Override
    public Response resource(String type, ResourceId id, RequestParameters<ResourceParameters> parameters, Optional<JsonObject> entity) {
        ResourceUri resourceUri = buildResourceUri(type, id.getId());
        return buildResponse(resmiService.findResource(resourceUri));
    }

    @Override
    public Response relation(String type, ResourceId id, String relation, RequestParameters<RelationParameters> parameters,
            Optional<JsonObject> entity) {
        ResourceUri resourceUri = buildRelationUri(type, id.getId(), relation,
                parameters.getOptionalApiParameters().flatMap(RelationParameters::getPredicateResource));
        try {
            if (parameters.getOptionalApiParameters().flatMap(CollectionParameters::getAggregation).isPresent()) {
                return buildResponse(resmiService.aggregate(resourceUri, parameters.getOptionalApiParameters().get()));
            } else {
                return buildResponse(resmiService.findRelation(resourceUri, parameters.getOptionalApiParameters()));
            }
        } catch (Exception e) {
            return ErrorResponseFactory.getInstance().badRequest();
        }
    }

}
