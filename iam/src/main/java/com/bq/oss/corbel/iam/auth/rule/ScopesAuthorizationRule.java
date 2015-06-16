package com.bq.oss.corbel.iam.auth.rule;

import java.util.Set;

import com.bq.oss.corbel.iam.auth.AuthorizationRequestContext;
import com.bq.oss.corbel.iam.auth.AuthorizationRule;
import com.bq.oss.corbel.iam.exception.UnauthorizedException;
import com.bq.oss.corbel.iam.model.Scope;
import com.bq.oss.corbel.iam.service.ScopeService;
import com.bq.oss.corbel.iam.utils.Message;
import com.google.common.collect.Sets;

/**
 * @author Alberto J. Rubio
 */
public class ScopesAuthorizationRule implements AuthorizationRule {

    private final ScopeService scopeService;

    public ScopesAuthorizationRule(ScopeService scopeService) {
        this.scopeService = scopeService;
    }

    @Override
    public void process(AuthorizationRequestContext context) throws UnauthorizedException {
        Set<String> requestedScopes = context.getRequestedScopes();
        if (!requestedScopes.isEmpty()) {
            Set<Scope> expandScopes = scopeService.expandScopes(requestedScopes);
            requestedScopes = scopeService.expandScopesIds(expandScopes);

            Set<Scope> domainScopes = scopeService.expandScopes(context.getRequestedDomain().getScopes());
            Set<Scope> clientScopes = !context.isCrossDomain() ? scopeService.expandScopes(context.getIssuerClient().getScopes()) : null;
            Set<Scope> userScopes = !context.isCrossDomain() && context.hasPrincipal() ? scopeService.expandScopes(context.getPrincipal()
                    .getScopes()) : null;

            Set<String> allowedScopes = scopeService.getAllowedScopes(domainScopes, clientScopes, userScopes, context.isCrossDomain(),
                    context.hasPrincipal());
            if (!allowedScopes.containsAll(requestedScopes)) {
                throw new UnauthorizedException(Message.REQUESTED_SCOPES_UNAUTHORIZED.getMessage(Sets.difference(requestedScopes,
                        allowedScopes)));
            }
        }
    }

}
