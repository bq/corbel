package io.corbel.resources.rem.service;

import java.net.URI;
import java.util.Collection;
import java.util.Optional;

import javax.ws.rs.core.Response;

import com.fasterxml.jackson.databind.JsonNode;
import com.google.gson.JsonObject;

import io.corbel.resources.rem.Rem;
import io.corbel.resources.rem.acl.AclPermission;
import io.corbel.resources.rem.request.*;

/**
 * @author Cristian del Cerro
 */
public interface AclResourcesService {

    Response saveResource(Rem rem, RequestParameters<CollectionParameters> parameters, String type, URI uri, Object entity);

    Response getResource(Rem rem, String type, ResourceId id, RequestParameters<ResourceParameters> parameters);

    Response getCollection(Rem rem, String type, RequestParameters<CollectionParameters> parameters);

    Response getRelation(Rem rem, String type, ResourceId id, String relation, RequestParameters<RelationParameters> parameters);

    Response updateResource(Rem rem, String type, ResourceId id, RequestParameters<ResourceParameters> parameters, Object entity);

    Response putRelation(Rem rem, String type, ResourceId id, String relation, RequestParameters<RelationParameters> parameters,
            Object entity);

    Response deleteResource(Rem rem, String type, ResourceId id, RequestParameters<ResourceParameters> parameters);

    Response deleteRelation(Rem rem, String type, ResourceId id, String relation, RequestParameters<RelationParameters> parameters);

    boolean isAuthorized(Optional<String> userId, Collection<String> groupIds, String type, ResourceId resourceId, AclPermission operation);

    boolean isManagedBy(String domainId, Optional<String> userId, Collection<String> groupIds, String collection);

    Optional<JsonObject> getResource(String type, ResourceId resourceId);

    Optional<JsonObject> getResourceIfIsAuthorized(Optional<String> userId, Collection<String> groupIds, String type, ResourceId resourceId,
            AclPermission operation);

    Response updateConfiguration(ResourceId id, RequestParameters<ResourceParameters> parameters, JsonNode json);

    void setRemService(RemService remService);
}
