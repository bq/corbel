package io.corbel.iam.service;

import java.security.SignatureException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import io.corbel.iam.model.Scope;
import io.corbel.iam.model.UserToken;
import io.corbel.iam.repository.UserTokenRepository;
import net.oauth.jsontoken.JsonToken;
import net.oauth.jsontoken.JsonTokenParser;

import io.corbel.iam.exception.UnauthorizedException;
import io.corbel.lib.token.reader.TokenReader;
import com.google.gson.JsonObject;

public class DefaultUpgradeTokenService implements UpgradeTokenService {

    private static final String SCOPE = "scope";
    private final JsonTokenParser jsonTokenParser;
    private final ScopeService scopeService;
    private final UserTokenRepository userTokenRepository;

    public DefaultUpgradeTokenService(JsonTokenParser jsonTokenParser, ScopeService scopeService, UserTokenRepository userTokenRepository) {
        this.jsonTokenParser = jsonTokenParser;
        this.scopeService = scopeService;
        this.userTokenRepository = userTokenRepository;
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

            Set<Scope> scopes = getUpgradedScopes(new HashSet<>(Arrays.asList(scopesToAdd)), tokenReader);
            publishScopes(scopes, tokenReader);
            saveUserToken(tokenReader.getToken(), scopes);
        } catch (IllegalStateException | SignatureException e) {
            throw new UnauthorizedException(e.getMessage());
        }
    }

    private void saveUserToken(String token, Set<Scope> scopes){
        UserToken userToken = userTokenRepository.findByToken(token);
        userToken.getScopes().addAll(scopes);
        userTokenRepository.save(userToken);
    }

    private void publishScopes(Set<Scope> scopes, TokenReader tokenReader) {
        scopeService.addAuthorizationRules(tokenReader.getToken(), scopes);
    }

    private Set<Scope> getUpgradedScopes(Set<String> scopesIds, TokenReader tokenReader){
        Set<Scope> scopes = scopeService.expandScopes(scopesIds, true);
        return scopeService.fillScopes(scopes, tokenReader.getInfo().getUserId(), tokenReader.getInfo().getClientId(),
                tokenReader.getInfo().getDomainId());
    }
}
