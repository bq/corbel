package io.corbel.resources.rem.resmi;

import java.net.URI;
import java.util.Optional;

import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import io.corbel.lib.ws.api.error.ErrorResponseFactory;
import io.corbel.resources.rem.Rem;
import io.corbel.resources.rem.model.ResourceUri;
import io.corbel.resources.rem.service.ResmiService;
import org.apache.commons.codec.digest.DigestUtils;

/**
 * @author Francisco Sánchez - Rubén Carrasco
 */
public abstract class AbstractResmiRem implements Rem<JsonObject> {

    protected final ResmiService resmiService;

    public AbstractResmiRem(ResmiService resmiService) {
        this.resmiService = resmiService;
    }

    protected Response buildResponse(JsonElement response) {
        if (response == null) {
            return ErrorResponseFactory.getInstance().notFound();
        } else {
            return Response.ok().type(javax.ws.rs.core.MediaType.APPLICATION_JSON_TYPE).entity(response).build();
        }
    }

    protected Response buildResponseWithCustomEtag(JsonElement response) {
        if (response == null) {
            return ErrorResponseFactory.getInstance().notFound();
        } else if (response instanceof JsonArray) {
            return buildResponseWithCustomEtagArrays(response);
        } else if (response instanceof JsonObject) {
            return buildResponseWithCustomEtagObject(response);
        } else {
            return buildResponse(response);
        }
    }

    private Response buildResponseWithCustomEtagObject(JsonElement response) {
        final JsonObject responseAsJsonObject = response.getAsJsonObject();
        if (responseAsJsonObject.has("_updatedAt") && responseAsJsonObject.has("id")){
            return Response.ok().type(javax.ws.rs.core.MediaType.APPLICATION_JSON_TYPE).entity(response).header(HttpHeaders.ETAG,
                    DigestUtils.md5(responseAsJsonObject.get("_updatedAt").toString() + responseAsJsonObject.get("id").toString()))
                    .build();
        }else {
            return Response.ok().type(javax.ws.rs.core.MediaType.APPLICATION_JSON_TYPE).entity(response).build();
        }
    }

    private Response buildResponseWithCustomEtagArrays(JsonElement response) {
        StringBuilder dataToGenerateEtag = new StringBuilder();
        final JsonArray responseAsJsonArray = response.getAsJsonArray();
        for (JsonElement element:responseAsJsonArray){
            final JsonObject elementAsJsonObject = element.getAsJsonObject();
            if (elementAsJsonObject.has("_updatedAt") && elementAsJsonObject.has("id")){
                dataToGenerateEtag.append(elementAsJsonObject.get("_updatedAt").toString());
                dataToGenerateEtag.append(elementAsJsonObject.get("id").toString());
            }else{
                return Response.ok().type(javax.ws.rs.core.MediaType.APPLICATION_JSON_TYPE).entity(response).build();
            }
        }
        return Response.ok().type(javax.ws.rs.core.MediaType.APPLICATION_JSON_TYPE).entity(response).header(HttpHeaders.ETAG,
                DigestUtils.md5(dataToGenerateEtag.toString()))
                .build();
    }

    protected ResourceUri buildCollectionUri(String domain, String type) {
        return new ResourceUri(domain, type);
    }

    protected ResourceUri buildResourceUri(String domain, String type, String id) {
        return new ResourceUri(domain, type, id);
    }

    protected ResourceUri buildRelationUri(String domain, String type, String id, String relation,Optional<String> predicateResource) {
        return new ResourceUri(domain, type, id, relation, predicateResource.orElse(null));
    }

    protected Response noContent() {
        return Response.noContent().build();
    }

    protected Response created() {
        return Response.status(Status.CREATED).build();
    }

    protected Response created(URI location) {
        return Response.created(location).build();
    }

    @Override
    public Class<JsonObject> getType() {
        return JsonObject.class;
    }

}
