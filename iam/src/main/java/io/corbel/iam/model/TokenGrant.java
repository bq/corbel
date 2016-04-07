package io.corbel.iam.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Objects;
import java.util.Set;

/**
 * @author Alexander De Leon
 */
public class TokenGrant {

    private final String accessToken;
    private final String refreshToken;
    private final long expiresAt;
    private final Set<String> scopes;

    @JsonCreator
    public TokenGrant(@JsonProperty("accessToken") String accessToken, @JsonProperty("expiresAt") long expiresAt,
                      @JsonProperty("refreshToken") String refreshToken, @JsonProperty("scopes") Set<String> scopes) {
        this.accessToken = accessToken;
        this.refreshToken = refreshToken;
        this.expiresAt = expiresAt;
        this.scopes = scopes;
    }

    public String getAccessToken() {
        return accessToken;
    }

    public String getRefreshToken() {
        return refreshToken;
    }

    public long getExpiresAt() {
        return expiresAt;
    }

    public Set<String> getScopes() {
        return scopes;
    }

    @Override
    public int hashCode() {
        return Objects.hash(accessToken, refreshToken, expiresAt, scopes);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        TokenGrant that = (TokenGrant) o;

        if (expiresAt != that.expiresAt) return false;
        if (accessToken != null ? !accessToken.equals(that.accessToken) : that.accessToken != null) return false;
        if (refreshToken != null ? !refreshToken.equals(that.refreshToken) : that.refreshToken != null) return false;
        if (scopes != null ? !scopes.equals(that.scopes) : that.scopes != null) return false;

        return true;
    }
}
