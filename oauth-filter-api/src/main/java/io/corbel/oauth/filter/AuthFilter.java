package io.corbel.oauth.filter;

import javax.ws.rs.core.MultivaluedMap;

public interface AuthFilter {

    boolean filter(String username, String password, String clientId, MultivaluedMap<String, String> form);

    String getDomain();
}
