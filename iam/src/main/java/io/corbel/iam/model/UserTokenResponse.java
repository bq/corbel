package io.corbel.iam.model;


import java.util.Date;
import java.util.Set;
import java.util.stream.Collectors;

public class UserTokenResponse {

    private String token;
    private String userId;
    private String deviceId;
    private Date expireAt;
    private Set<String> scopes;

    public UserTokenResponse() {}

    public UserTokenResponse(UserToken userToken) {
        this.token = userToken.getToken();
        this.userId = userToken.getUserId();
        this.deviceId = userToken.getDeviceId();
        this.expireAt = userToken.getExpireAt();
        this.scopes = userToken.getScopes().stream().map(scope -> scope.getId()).collect(Collectors.toSet());
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public Set<String> getScopes() {
        return scopes;
    }

    public void setScopes(Set<String> scopes) {
        this.scopes = scopes;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(String deviceId) {
        this.deviceId = deviceId;
    }

    public Date getExpireAt() {
        return expireAt;
    }

    public void setExpireAt(Date expireAt) {
        this.expireAt = expireAt;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        UserTokenResponse that = (UserTokenResponse) o;

        if (token != null ? !token.equals(that.token) : that.token != null) return false;
        if (userId != null ? !userId.equals(that.userId) : that.userId != null) return false;
        if (deviceId != null ? !deviceId.equals(that.deviceId) : that.deviceId != null) return false;
        if (expireAt != null ? !expireAt.equals(that.expireAt) : that.expireAt != null) return false;
        return !(scopes != null ? !scopes.equals(that.scopes) : that.scopes != null);

    }

    @Override
    public int hashCode() {
        int result = token != null ? token.hashCode() : 0;
        result = 31 * result + (userId != null ? userId.hashCode() : 0);
        result = 31 * result + (deviceId != null ? deviceId.hashCode() : 0);
        result = 31 * result + (expireAt != null ? expireAt.hashCode() : 0);
        result = 31 * result + (scopes != null ? scopes.hashCode() : 0);
        return result;
    }
}
