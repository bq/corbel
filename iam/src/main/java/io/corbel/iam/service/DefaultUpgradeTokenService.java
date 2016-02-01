package io.corbel.iam.service;

import java.security.SignatureException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import io.corbel.iam.model.Scope;
import net.oauth.jsontoken.JsonToken;
import net.oauth.jsontoken.JsonTokenParser;

import io.corbel.iam.exception.UnauthorizedException;
import io.corbel.lib.token.reader.TokenReader;
import com.google.gson.JsonObject;

public class DefaultUpgradeTokenService implements UpgradeTokenService {

    private static final String SCOPE = "scope";
    private final JsonTokenParser jsonTokenParser;
    private final ScopeService scopeService;

    public DefaultUpgradeTokenService(JsonTokenParser jsonTokenParser, ScopeService scopeService) {
        this.jsonTokenParser = jsonTokenParser;
        this.scopeService = scopeService;
    }

    @Override
    public void upgradeToken(String assertion, TokenReader tokenReader) throws UnauthorizedException {
        try {
            JsonToken jwt = jsonTokenParser.verifyAndDeserialize(assertion);
            JsonObject payload = jwt.getPayloadAsJsonObject();
            String[] scopesToAdd = new String[0];
            if (payload.has(SCOPE) && payload.get(SCOPE).isJsonPrimitive()) {
                String scopesToAddFromToken = payload.get(SCOPE).getAsString();
                if (!scopesToAddFromToken.isEmpty()) {
                    scopesToAdd = scopesToAddFromToken.split(" ");
                }
            }

            publishScopes(new HashSet<>(Arrays.asList(scopesToAdd)), tokenReader);
        } catch (IllegalStateException | SignatureException e) {
            throw new UnauthorizedException(e.getMessage());
        }
    }

    private void publishScopes(Set<String> scopesIds, TokenReader tokenReader) {
        Set<Scope> scopes = scopeService.expandScopes(scopesIds);
        scopes = scopeService.fillScopes(scopes, tokenReader.getInfo().getUserId(), tokenReader.getInfo().getClientId(),
                tokenReader.getInfo().getDomainId());
        scopeService.addAuthorizationRules(tokenReader.getToken(), scopes);
    }
}
