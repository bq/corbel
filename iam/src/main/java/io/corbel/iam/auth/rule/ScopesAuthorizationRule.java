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
        Set<String> allowedScopes = getAllowedScopes(context);
        if (context.getRequestedScopes().isEmpty()) {
            context.setExpandedRequestedScopes(scopeService.expandScopes(allowedScopes));
            context.setTokenScopes(allowedScopes);
        } else {
            Set<Scope> requestedScopes = scopeService.expandScopes(context.getRequestedScopes());
            checkRequestedScopes(requestedScopes, scopeService.expandScopes(allowedScopes));
            context.setExpandedRequestedScopes(requestedScopes);
            context.setTokenScopes(context.getRequestedScopes());
        }
    }

    private Set<String> getAllowedScopes(AuthorizationRequestContext context) {
        Set<String> domainScopes = context.getRequestedDomain().getScopes();
        if(context.isCrossDomain()) {
            return domainScopes;
        } else {
            Set<String> requestedScopes = context.getIssuerClient().getScopes();
            if (context.hasPrincipal()) {
                Set<String> userScopes = context.getPrincipal().getScopes();
                Set<String> groupScopes = groupService.getGroupScopes(context.getPrincipal().getGroups());
                requestedScopes = Sets.union(requestedScopes, Sets.union(userScopes, groupScopes));
            }
            return Sets.intersection(requestedScopes, domainScopes);
        }
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
