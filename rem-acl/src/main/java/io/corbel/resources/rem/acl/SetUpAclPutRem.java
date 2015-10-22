package io.corbel.resources.rem.acl;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.ws.rs.core.Response;

import org.springframework.http.HttpMethod;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonPrimitive;
import com.google.gson.stream.JsonReader;

import io.corbel.lib.ws.api.error.ErrorResponseFactory;
import io.corbel.resources.rem.Rem;
import io.corbel.resources.rem.request.RequestParameters;
import io.corbel.resources.rem.request.ResourceId;
import io.corbel.resources.rem.request.ResourceParameters;
import io.corbel.resources.rem.service.AclResourcesService;
import io.corbel.resources.rem.service.DefaultAclResourcesService;
import io.corbel.resources.rem.utils.AclUtils;

/**
 * @author Cristian del Cerro
 */
public class SetUpAclPutRem extends AclBaseRem {

    private Pattern prefixPattern = Pattern
            .compile("(?:(?:" + DefaultAclResourcesService.USER_PREFIX + ")|(?:" + DefaultAclResourcesService.GROUP_PREFIX + "))\\S+");

    public SetUpAclPutRem(AclResourcesService aclResourcesService, List<Rem> remsToExclude) {
        super(aclResourcesService, remsToExclude);
    }

    @Override
    public Response resource(String type, ResourceId id, RequestParameters<ResourceParameters> parameters, Optional<InputStream> entity) {

        String userId = parameters.getTokenInfo().getUserId();
        if (userId == null) {
            return ErrorResponseFactory.getInstance().methodNotAllowed();
        }

        Collection<String> groupIds = parameters.getTokenInfo().getGroups();

        InputStream requestBody = entity.get();
        if (AclUtils.entityIsEmpty(requestBody)) {
            return ErrorResponseFactory.getInstance().badRequest();
        }

        JsonReader reader = new JsonReader(new InputStreamReader(requestBody));
        JsonObject jsonObject = new JsonParser().parse(reader).getAsJsonObject();

        if (!aclResourcesService.isAuthorized(userId, groupIds, type, id, AclPermission.ADMIN)) {
            return ErrorResponseFactory.getInstance().unauthorized(AclUtils.buildMessage(AclPermission.ADMIN));
        }

        JsonObject filteredAclObject = getFilteredAclObject(jsonObject);

        if (filteredAclObject.entrySet().isEmpty() || !hasAdminPermission(userId, groupIds, filteredAclObject)) {
            return ErrorResponseFactory.getInstance().badRequest();
        }

        JsonObject objectToSave = new JsonObject();
        objectToSave.add(DefaultAclResourcesService._ACL, filteredAclObject);

        Rem rem = remService.getRem(type, JSON_MEDIATYPE, HttpMethod.PUT, REMS_TO_EXCLUDE);
        return aclResourcesService.updateResource(rem, type, id, parameters, objectToSave);

    }

    private JsonObject getFilteredAclObject(JsonObject aclObject) {
        List<String> validFieldNames = aclObject.entrySet().stream().map(Map.Entry::getKey)
                .filter(key -> key.equals(DefaultAclResourcesService.ALL) || prefixPattern.matcher(key).matches())
                .collect(Collectors.toList());

        JsonObject filteredAcl = new JsonObject();

        validFieldNames.forEach(
                field -> Optional.ofNullable(aclObject.get(field)).filter(JsonElement::isJsonObject).map(JsonElement::getAsJsonObject)
                        .flatMap(this::filterAclValue).ifPresent(filteredAclValue -> filteredAcl.add(field, filteredAclValue)));

        return filteredAcl;
    }

    private Optional<JsonObject> filterAclValue(JsonObject jsonObject) {
        Optional<String> optionalPermission = Optional.ofNullable(jsonObject.get(DefaultAclResourcesService.PERMISSION))
                .filter(JsonElement::isJsonPrimitive).map(JsonElement::getAsJsonPrimitive).filter(JsonPrimitive::isString)
                .map(JsonPrimitive::getAsString);

        try {
            optionalPermission.map(AclPermission::valueOf);
        } catch (IllegalArgumentException e) {
            return Optional.empty();
        }

        String permission;

        if (optionalPermission.isPresent()) {
            permission = optionalPermission.get();
        } else {
            return Optional.empty();
        }

        JsonObject properties = Optional.ofNullable(jsonObject.get(DefaultAclResourcesService.PROPERTIES)).filter(JsonElement::isJsonObject)
                .map(JsonElement::getAsJsonObject).orElseGet(JsonObject::new);

        JsonObject objectToReturn = new JsonObject();
        objectToReturn.addProperty(DefaultAclResourcesService.PERMISSION, permission);
        objectToReturn.add(DefaultAclResourcesService.PROPERTIES, properties);

        return Optional.of(objectToReturn);
    }

    private boolean idHasAdminPermission(String id, JsonObject acl) {
        return Optional.ofNullable(acl.get(id)).map(JsonElement::getAsJsonObject)
                .map(jsonObject -> jsonObject.get(DefaultAclResourcesService.PERMISSION)).map(JsonElement::getAsString)
                .filter(permissionValue -> permissionValue.equals(AclPermission.ADMIN.toString())).isPresent();
    }

    private boolean hasAdminPermission(String userId, Collection<String> groupIds, JsonObject acl) {
        Stream<String> userAndAllStream = Arrays.asList(DefaultAclResourcesService.ALL, DefaultAclResourcesService.USER_PREFIX + userId)
                .stream();
        Stream<String> groupsStream = groupIds.stream().map(id -> DefaultAclResourcesService.GROUP_PREFIX + id);

        return Stream.concat(userAndAllStream, groupsStream).anyMatch(id -> idHasAdminPermission(id, acl));
    }

}
