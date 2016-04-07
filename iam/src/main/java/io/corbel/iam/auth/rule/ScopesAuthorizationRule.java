package io.corbel.iam.auth.rule;

import java.util.Set;
import java.util.stream.Collectors;

import com.google.common.collect.Sets;

import io.corbel.iam.auth.AuthorizationRequestContext;
import io.corbel.iam.auth.AuthorizationRule;
import io.corbel.iam.exception.UnauthorizedException;
import io.corbel.iam.model.Scope;
import io.corbel.iam.service.GroupService;
import io.corbel.iam.service.ScopeService;
import io.corbel.iam.utils.Message;

/**
 * @author Alberto J. Rubio
 */
public class ScopesAuthorizationRule implements AuthorizationRule {

    private final ScopeService scopeService;
    private final GroupService groupService;

    public ScopesAuthorizationRule(ScopeService scopeService, GroupService groupService) {
        this.scopeService = scopeService;
        this.groupService = groupService;
    }

    @Override
    public void process(AuthorizationRequestContext context) throws UnauthorizedException {
        Set<Scope> allowedScopes = getAllowedScopes(context);
        Set<Scope> requestedScopes;
        if (context.getRequestedScopes().isEmpty()) {
            requestedScopes = allowedScopes;
        } else {
            requestedScopes = scopeService.expandScopes(context.getRequestedScopes(), true);
            checkRequestedScopes(requestedScopes, allowedScopes);
        }
        context.setExpandedRequestedScopes(requestedScopes);
    }

    private Set<Scope> getAllowedScopes(AuthorizationRequestContext context) {
        return scopeService.getAnyLevelScopes(context);
    }

    private void checkRequestedScopes(Set<Scope> requestedExpandScopes, Set<Scope> allowedScopes) throws UnauthorizedException {
        if (!allowedScopes.containsAll(requestedExpandScopes)) {
            Set<String> requestedScopes = requestedExpandScopes.stream().map(Scope::getIdWithParameters).collect(Collectors.toSet());
            Set<String> allowedScopesIds = allowedScopes.stream().map(Scope::getIdWithParameters).collect(Collectors.toSet());
            throw new UnauthorizedException(Message.REQUESTED_SCOPES_UNAUTHORIZED.getMessage(Sets.difference(requestedScopes,
                    allowedScopesIds)));
        }
    }

}
