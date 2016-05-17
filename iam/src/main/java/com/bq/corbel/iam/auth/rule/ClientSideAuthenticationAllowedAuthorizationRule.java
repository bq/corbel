package com.bq.corbel.iam.auth.rule;

import com.bq.corbel.iam.auth.AuthorizationRequestContext;
import com.bq.corbel.iam.auth.AuthorizationRule;
import com.bq.corbel.iam.exception.UnauthorizedException;
import com.bq.corbel.iam.utils.Message;

public class ClientSideAuthenticationAllowedAuthorizationRule implements AuthorizationRule {

    @Override
    public void process(AuthorizationRequestContext context) throws UnauthorizedException {
        if (!context.isOAuth() && !context.isBasic() && context.hasPrincipal() && !context.hasRefreshToken()) {
            if (context.getIssuerClient().getClientSideAuthentication() == null || !context.getIssuerClient().getClientSideAuthentication()) {
                throw new UnauthorizedException(Message.CLIENT_SIDE_AUTHENTICATION_NOT_ALLOWED.getMessage());
            }
        }
    }

}
