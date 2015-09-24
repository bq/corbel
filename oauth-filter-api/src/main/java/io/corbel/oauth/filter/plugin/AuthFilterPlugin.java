package io.corbel.oauth.filter.plugin;

import io.corbel.oauth.filter.FilterRegistry;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;

public abstract class AuthFilterPlugin implements InitializingBean {

    @Autowired private FilterRegistry registry;

    @Override
    public final void afterPropertiesSet() throws Exception {
        register(registry);
    }

    protected abstract void register(FilterRegistry registry);
}
