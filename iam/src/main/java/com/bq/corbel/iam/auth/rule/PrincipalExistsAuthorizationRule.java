package com.bq.corbel.iam.auth.rule;

import com.bq.corbel.iam.auth.AuthorizationRequestContext;
import com.bq.corbel.iam.auth.AuthorizationRule;
import com.bq.corbel.iam.exception.NoSuchPrincipalException;
import com.bq.corbel.iam.exception.UnauthorizedException;
import com.bq.corbel.iam.utils.Message;

/**
 * @author Alberto J. Rubio
 */
public class PrincipalExistsAuthorizationRule implements AuthorizationRule {

    @Override
    public void process(AuthorizationRequestContext context) throws UnauthorizedException {
        if (context.hasPrincipal() && context.getPrincipal() == null) {
            throw new NoSuchPrincipalException(Message.PRINCIPAL_EXISTS_UNAUTHORIZED.getMessage(context.getPrincipalId(), context
                    .getIssuerClient().getDomain()));
        }
    }
}
