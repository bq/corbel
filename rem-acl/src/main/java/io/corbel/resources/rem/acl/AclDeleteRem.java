package io.corbel.resources.rem.acl;

import java.io.InputStream;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import javax.ws.rs.core.Response;

import org.springframework.http.HttpMethod;

import io.corbel.lib.token.TokenInfo;
import io.corbel.lib.ws.api.error.ErrorResponseFactory;
import io.corbel.resources.rem.Rem;
import io.corbel.resources.rem.request.RelationParameters;
import io.corbel.resources.rem.request.RequestParameters;
import io.corbel.resources.rem.request.ResourceId;
import io.corbel.resources.rem.request.ResourceParameters;
import io.corbel.resources.rem.service.AclResourcesService;
import io.corbel.resources.rem.utils.AclUtils;


/**
 * @author Rubén Carrasco
 */
public class AclDeleteRem extends AclBaseRem {

    public AclDeleteRem(AclResourcesService aclResourcesService, List<Rem> remsToExclude) {
        super(aclResourcesService, remsToExclude);
    }

    @Override
    public Response resource(String type, ResourceId id, RequestParameters<ResourceParameters> parameters, Optional<InputStream> entity) {

        TokenInfo tokenInfo = parameters.getTokenInfo();

        if (!aclResourcesService.isManagedBy(tokenInfo, type)
                && !aclResourcesService.isAuthorized(tokenInfo, type, id, AclPermission.ADMIN)) {
            return ErrorResponseFactory.getInstance().unauthorized(AclUtils.buildMessage(AclPermission.ADMIN));
        }

        Rem rem = remService.getRem(type, parameters.getAcceptedMediaTypes(), HttpMethod.DELETE, Collections.singletonList(this));
        return aclResourcesService.deleteResource(rem, type, id, parameters);

    }

    @Override
    public Response relation(String type, ResourceId id, String relation, RequestParameters<RelationParameters> parameters,
            Optional<InputStream> entity) {

        if (id.isWildcard()) {
            return ErrorResponseFactory.getInstance().methodNotAllowed();
        }

        TokenInfo tokenInfo = parameters.getTokenInfo();
        Optional<String> userId = Optional.ofNullable(tokenInfo.getUserId());


        Collection<String> groupIds = tokenInfo.getGroups();
        String domainId = tokenInfo.getDomainId();

        if (!aclResourcesService.isManagedBy(domainId, userId, groupIds, type)
                && !aclResourcesService.isAuthorized(userId, groupIds, type, id, AclPermission.ADMIN)) {
            return ErrorResponseFactory.getInstance().unauthorized(AclUtils.buildMessage(AclPermission.ADMIN));
        }

        Rem rem = remService.getRem(type, parameters.getAcceptedMediaTypes(), HttpMethod.DELETE, Collections.singletonList(this));
        return aclResourcesService.deleteRelation(rem, type, id, relation, parameters);
    }

}
