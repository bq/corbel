package com.bq.corbel.oauth.session;

import com.bq.corbel.lib.token.TokenGrant;
import com.bq.corbel.lib.token.TokenInfo;
import com.bq.corbel.lib.token.factory.TokenFactory;
import com.bq.corbel.lib.token.model.TokenType;

/**
 * @author Alberto J. Rubio
 */
public class DefaultSessionBuilder implements SessionBuilder {

    private final TokenFactory tokenFactory;
    private final long sessionMaxAge;

    public DefaultSessionBuilder(TokenFactory tokenFactory, long sessionMaxAge) {
        this.tokenFactory = tokenFactory;
        this.sessionMaxAge = sessionMaxAge;
    }

    @Override
    public String createNewSession(String clientId, String userId) {
        TokenInfo newSession = TokenInfo.newBuilder().setType(TokenType.REFRESH).setClientId(clientId).setUserId(userId)
                .setState(Long.toString(System.currentTimeMillis())).setOneUseToken(true).build();
        TokenGrant token = tokenFactory.createToken(newSession, sessionMaxAge);
        return token.getAccessToken();
    }
}
