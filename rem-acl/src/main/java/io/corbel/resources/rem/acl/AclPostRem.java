package io.corbel.resources.rem.acl;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;

import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.stream.JsonReader;

import io.corbel.lib.ws.api.error.ErrorResponseFactory;
import io.corbel.resources.rem.Rem;
import io.corbel.resources.rem.request.*;
import io.corbel.resources.rem.service.AclResourcesService;
import io.corbel.resources.rem.service.DefaultAclResourcesService;
import io.corbel.resources.rem.utils.AclUtils;

/**
 * @author Cristian del Cerro
 */
public class AclPostRem extends AclBaseRem {

    private final String aclConfigurationCollection;
    private final Rem aclPutRem;

    public AclPostRem(AclResourcesService aclResourcesService, List<Rem> remsToExclude, String aclConfigurationCollection, Rem aclPutRem) {
        super(aclResourcesService, remsToExclude);
        this.aclConfigurationCollection = aclConfigurationCollection;
        this.aclPutRem = aclPutRem;
    }

    @Override
    public Response collection(String type, RequestParameters<CollectionParameters> parameters, URI uri, Optional<InputStream> entity) {

        if (type.equals(aclConfigurationCollection)) {
            return entity.map(e -> configureManageableCollection(e, parameters, uri))
                    .orElseGet(() -> ErrorResponseFactory.getInstance().badRequest());
        }

        if (entity.map(AclUtils::entityIsEmpty).orElse(true)) {
            return ErrorResponseFactory.getInstance().badRequest();
        }

        InputStream requestBody = entity.get();

        boolean jsonMediaTypeAccepted = parameters.getAcceptedMediaTypes().contains(MediaType.APPLICATION_JSON);

        JsonObject jsonObject = new JsonObject();

        if (jsonMediaTypeAccepted) {
            JsonReader reader = new JsonReader(new InputStreamReader(requestBody));
            jsonObject = new JsonParser().parse(reader).getAsJsonObject();
            jsonObject.remove(DefaultAclResourcesService._ACL);
        }

        JsonObject acl = new JsonObject();

        Optional.ofNullable(parameters.getTokenInfo().getUserId()).ifPresent(userId -> {
            JsonObject userAcl = new JsonObject();
            userAcl.addProperty(DefaultAclResourcesService.PERMISSION, AclPermission.ADMIN.toString());
            userAcl.add(DefaultAclResourcesService.PROPERTIES, new JsonObject());

            acl.add(DefaultAclResourcesService.USER_PREFIX + userId, userAcl);
        });

        jsonObject.add(DefaultAclResourcesService._ACL, acl);

        Rem resmiPostRem = remService.getRem(type, JSON_MEDIATYPE, HttpMethod.POST, Collections.singletonList(this));
        Response response = aclResourcesService.saveResource(resmiPostRem, parameters, type, uri, jsonObject);

        if (!jsonMediaTypeAccepted) {
            String path = response.getMetadata().getFirst("Location").toString();
            String id = path.substring(path.lastIndexOf("/") + 1);
            Rem rem = remService.getRem(type, parameters.getAcceptedMediaTypes(), HttpMethod.PUT, REMS_TO_EXCLUDE);

            RequestParameters<ResourceParameters> requestParameters = new RequestParametersImpl<>(null, null,
                    parameters.getAcceptedMediaTypes(), null, parameters.getHeaders(), null);
            Response responsePutNotJsonResource = aclResourcesService.updateResource(rem, type, new ResourceId(id), requestParameters,
                    requestBody);
            if (responsePutNotJsonResource.getStatus() != Response.Status.NO_CONTENT.getStatusCode()) {
                return responsePutNotJsonResource;
            }
        }

        return response;

    }

    private Response configureManageableCollection(InputStream entity, RequestParameters<CollectionParameters> parameters, URI uri) {
        ResourceParameters resourceParameters = new ResourceParametersImpl(null, Optional.empty(), Optional.empty(), Optional.empty(),
                Optional.empty(), Optional.empty());
        RequestParameters<ResourceParameters> requestParameters = new RequestParametersImpl<>(resourceParameters, parameters.getTokenInfo(),
                parameters.getAcceptedMediaTypes(), parameters.getContentLength(), parameters.getParams(), parameters.getHeaders());

        String id;
        JsonNode json;

        try {
            json = new ObjectMapper().readTree(entity);
            id = Optional.of(json).filter(jsonNode -> jsonNode.has("id")).map(jsonNode -> jsonNode.get("id")).filter(JsonNode::isTextual)
                    .map(JsonNode::asText).orElseThrow(IOException::new);
        } catch (IOException e) {
            return ErrorResponseFactory.getInstance().badRequest();
        }

        Response response = aclResourcesService.updateConfiguration(new ResourceId(id), requestParameters, json);

        if (response.getStatus() != Response.Status.NO_CONTENT.getStatusCode()) {
            return response;
        }

        return Response.created(UriBuilder.fromUri(uri).path("/{id}").build(id)).build();
    }

}
