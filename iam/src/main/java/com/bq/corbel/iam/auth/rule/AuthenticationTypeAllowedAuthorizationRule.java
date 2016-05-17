package com.bq.corbel.iam.auth.rule;

import com.bq.corbel.iam.auth.AuthorizationRequestContext;
import com.bq.corbel.iam.auth.AuthorizationRule;
import com.bq.corbel.iam.exception.UnauthorizedException;
import com.bq.corbel.iam.utils.Message;

public class AuthenticationTypeAllowedAuthorizationRule implements AuthorizationRule {

    @Override
    public void process(AuthorizationRequestContext context) throws UnauthorizedException {
        if (context.isOAuth() && !context.getRequestedDomain().getCapabilities().getOrDefault("oauth", true)
                || context.isBasic() && !context.getRequestedDomain().getCapabilities().getOrDefault("basic", true)) {
            throw new UnauthorizedException(Message.AUTHENTICATION_TYPE_NOT_ALLOWED.getMessage());
        }
    }

}
