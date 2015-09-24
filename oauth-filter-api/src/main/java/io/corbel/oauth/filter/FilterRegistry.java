package io.corbel.oauth.filter;

import javax.ws.rs.core.MultivaluedMap;


public interface FilterRegistry {

    void registerFilter(AuthFilter filter);

    boolean filter(String username, String password, String clientId, MultivaluedMap<String, String> form);
}
