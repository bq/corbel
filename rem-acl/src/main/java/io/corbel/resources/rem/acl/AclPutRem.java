package io.corbel.resources.rem.acl;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.stream.JsonReader;

import io.corbel.lib.token.TokenInfo;
import io.corbel.lib.ws.api.error.ErrorResponseFactory;
import io.corbel.resources.rem.Rem;
import io.corbel.resources.rem.request.RelationParameters;
import io.corbel.resources.rem.request.RequestParameters;
import io.corbel.resources.rem.request.ResourceId;
import io.corbel.resources.rem.request.ResourceParameters;
import io.corbel.resources.rem.service.AclResourcesService;
import io.corbel.resources.rem.service.DefaultAclResourcesService;
import io.corbel.resources.rem.utils.AclUtils;

/**
 * @author Rubén Carrasco
 */
public class AclPutRem extends AclBaseRem {

    private final String aclConfigurationCollection;

    public AclPutRem(AclResourcesService aclResourcesService, List<Rem> remsToExclude, String aclConfigurationCollection) {
        super(aclResourcesService, remsToExclude);
        this.aclConfigurationCollection = aclConfigurationCollection;
    }

    @Override
    public Response resource(String type, ResourceId id, RequestParameters<ResourceParameters> parameters, Optional<InputStream> entity) {

        if (type.equals(aclConfigurationCollection)) {
            return entity.flatMap(inputStreamEntity -> {
                try {
                    return Optional.of(aclResourcesService.updateConfiguration(id, parameters, new ObjectMapper().readTree(new InputStreamReader(inputStreamEntity))));
                } catch (IOException e) {
                    return Optional.empty();
                }
            }).orElseGet(() -> ErrorResponseFactory.getInstance().badRequest());
        }

        TokenInfo tokenInfo = parameters.getTokenInfo();
        Optional<String> userId = Optional.ofNullable(tokenInfo.getUserId());

        Collection<String> groupIds = tokenInfo.getGroups();
        String domainId = tokenInfo.getDomainId();

        boolean adminAuthorized = aclResourcesService.isManagedBy(domainId, userId, groupIds, type);

        InputStream requestBody = entity.get();
        if (!adminAuthorized && AclUtils.entityIsEmpty(requestBody)) {
            return ErrorResponseFactory.getInstance().badRequest();
        }

        boolean newResource = false;
        Optional<JsonObject> originalObject = Optional.empty();

        try {
            originalObject = adminAuthorized ? aclResourcesService.getResource(type, id)
                    : aclResourcesService.getResourceIfIsAuthorized(userId, groupIds, type, id, AclPermission.WRITE);
        } catch (WebApplicationException exception) {
            if (exception.getResponse().getStatus() == Status.NOT_FOUND.getStatusCode()) {
                newResource = true;
            } else {
                return exception.getResponse();
            }
        }

        if (newResource) {
            JsonObject aclValue = new JsonObject();
            aclValue.addProperty(DefaultAclResourcesService.PERMISSION, AclPermission.ADMIN.toString());
            aclValue.add(DefaultAclResourcesService.PROPERTIES, new JsonObject());

            JsonObject user = new JsonObject();
            userId.ifPresent(presentUserId -> user.add(DefaultAclResourcesService.USER_PREFIX + presentUserId, aclValue));

            JsonObject acl = new JsonObject();
            acl.add(DefaultAclResourcesService._ACL, user);

            Rem rem = remService.getRem(type, JSON_MEDIATYPE, HttpMethod.PUT, Collections.singletonList(this));
            Response responseSetAcl = aclResourcesService.updateResource(rem, type, id, parameters, acl);
            if (responseSetAcl.getStatus() != Status.NO_CONTENT.getStatusCode()) {
                return responseSetAcl;
            }
        } else if (!originalObject.isPresent()) {
            return ErrorResponseFactory.getInstance().unauthorized(AclUtils.buildMessage(AclPermission.WRITE));
        }

        Rem rem = remService.getRem(type, parameters.getAcceptedMediaTypes(), HttpMethod.PUT, Collections.singletonList(this));

        if (parameters.getAcceptedMediaTypes().contains(MediaType.APPLICATION_JSON)) {
            JsonReader reader = new JsonReader(new InputStreamReader(requestBody));
            JsonObject jsonObject = new JsonParser().parse(reader).getAsJsonObject();
            jsonObject.remove(DefaultAclResourcesService._ACL);
            return aclResourcesService.updateResource(rem, type, id, parameters, jsonObject);
        }
        return aclResourcesService.updateResource(rem, type, id, parameters, requestBody);
    }

    @Override
    public Response relation(String type, ResourceId id, String relation, RequestParameters<RelationParameters> parameters,
            Optional<InputStream> entity) {

        TokenInfo tokenInfo = parameters.getTokenInfo();
        Optional<String> userId = Optional.ofNullable(tokenInfo.getUserId());

        if (id.isWildcard()) {
            return ErrorResponseFactory.getInstance().methodNotAllowed();
        }

        Collection<String> groupIds = tokenInfo.getGroups();
        String domainId = tokenInfo.getDomainId();

        InputStream requestBody = entity.get();

        if (aclResourcesService.isManagedBy(domainId, userId, groupIds, type)
                || aclResourcesService.isAuthorized(userId, groupIds, type, id, AclPermission.WRITE)) {

            Rem rem = remService.getRem(type, parameters.getAcceptedMediaTypes(), HttpMethod.PUT, Collections.singletonList(this));
            JsonObject jsonObject = new JsonObject();

            if (!AclUtils.entityIsEmpty(requestBody)) {
                JsonReader reader = new JsonReader(new InputStreamReader(requestBody));
                jsonObject = new JsonParser().parse(reader).getAsJsonObject();
            }

            return aclResourcesService.putRelation(rem, type, id, relation, parameters, jsonObject);
        }

        else {
            return ErrorResponseFactory.getInstance().unauthorized(AclUtils.buildMessage(AclPermission.WRITE));
        }

    }
}
