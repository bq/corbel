package io.corbel.oauth.filter;

import java.util.HashSet;
import java.util.Set;

import javax.ws.rs.core.MultivaluedMap;

public class InMemoryFilterRegistry implements FilterRegistry {

    private final Set<AuthFilter> filters;

    public InMemoryFilterRegistry() {
        this.filters = new HashSet<AuthFilter>();
    }

    @Override
    public void registerFilter(AuthFilter filter) {
        filters.add(filter);
    }

    @Override
    public boolean filter(String username, String password, String clientId, MultivaluedMap<String, String> form) {
        return filters.stream().allMatch(filter -> filter.filter(username, password, clientId, form));

    }

}
