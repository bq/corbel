package com.bq.corbel.iam.auth.rule;

import com.bq.corbel.iam.auth.AuthorizationRequestContext;
import com.bq.corbel.iam.auth.AuthorizationRule;
import com.bq.corbel.iam.exception.UnauthorizedException;
import com.bq.corbel.iam.exception.UnauthorizedTimeException;

/**
 * Ensures that the requested expiration time does not exceeds the maximum allowed expiration.
 * 
 * @author Alexander De Leon
 * 
 */
public class MaxExpireAuthorizationRule implements AuthorizationRule {

    private final int maxExpirationInMillis;

    public MaxExpireAuthorizationRule(int maxExpirationInMillis) {
        this.maxExpirationInMillis = maxExpirationInMillis;
    }

    @Override
    public void process(AuthorizationRequestContext context) throws UnauthorizedException {
        long currentTime = System.currentTimeMillis();
        if (context.getAuthorizationExpiration() > currentTime + maxExpirationInMillis) {
            throw new UnauthorizedTimeException("Authorization request exceds maximum expiration. Maximum is " + maxExpirationInMillis
                    + " milliseconds since its issued time");
        }
    }
}
