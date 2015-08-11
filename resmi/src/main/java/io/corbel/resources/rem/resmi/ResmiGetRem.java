package io.corbel.resources.rem.resmi;

import java.net.URI;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.ws.rs.core.Response;

import io.corbel.resources.rem.model.ResourceUri;
import io.corbel.resources.rem.request.*;
import io.corbel.resources.rem.service.BadConfigurationException;
import io.corbel.resources.rem.service.ResmiService;
import io.corbel.lib.ws.api.error.ErrorResponseFactory;
import io.corbel.lib.ws.model.Error;
import com.google.gson.JsonObject;

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
            if (parameters.getOptionalApiParameters().flatMap(params -> params.getAggregation()).isPresent()) {
                return buildResponse(resmiService.aggregate(resourceUri, parameters.getOptionalApiParameters().get()));
            }
            else if(parameters.getCustomParameterValue("api:distinct") != null) {
                List<String> fields = getDistinctFields(parameters.getCustomParameterValue("api:distinct"));
                return buildResponse(resmiService.findCollectionDistinct(resourceUri, parameters.getOptionalApiParameters(), fields, true));
            }
            else {
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
        ResourceUri resourceUri = buildRelationUri(type, id.getId(), relation, parameters.getOptionalApiParameters().flatMap(params -> params.getPredicateResource()));
        try {
            if (parameters.getOptionalApiParameters().flatMap(params -> params.getAggregation()).isPresent()) {
                return buildResponse(resmiService.aggregate(resourceUri, parameters.getOptionalApiParameters().get()));
            }
            else if(parameters.getCustomParameterValue("api:distinct") != null) {
                List<String> fields = getDistinctFields(parameters.getCustomParameterValue("api:distinct"));
                return buildResponse(resmiService.findRelationDistinct(resourceUri, parameters.getOptionalApiParameters(), fields, true));
            }
            else {
                return buildResponse(resmiService.findRelation(resourceUri, parameters.getOptionalApiParameters()));
            }
        } catch (Exception e) {
            return ErrorResponseFactory.getInstance().badRequest();
        }
    }

    private List<String> getDistinctFields(String serializedParameter) {
        List<String> fields = Arrays.asList(serializedParameter.split(","));
        return fields.stream().map(val -> val.trim()).collect(Collectors.toList());
    }

}
