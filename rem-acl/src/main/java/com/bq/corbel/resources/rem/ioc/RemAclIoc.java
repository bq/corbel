package com.bq.corbel.resources.rem.ioc;

import com.bq.corbel.event.ResourceEvent;
import com.bq.corbel.eventbus.EventHandler;
import com.bq.corbel.eventbus.ioc.EventBusListeningIoc;
import com.bq.corbel.lib.config.ConfigurationIoC;
import com.bq.corbel.resources.rem.Rem;
import com.bq.corbel.resources.rem.acl.AclDeleteRem;
import com.bq.corbel.resources.rem.acl.AclGetManagedCollectionRem;
import com.bq.corbel.resources.rem.acl.AclGetRem;
import com.bq.corbel.resources.rem.acl.AclPostManagedCollectionRem;
import com.bq.corbel.resources.rem.acl.AclPostRem;
import com.bq.corbel.resources.rem.acl.AclPutManagedCollectionRem;
import com.bq.corbel.resources.rem.acl.AclPutRem;
import com.bq.corbel.resources.rem.acl.SetUpAclPutRem;
import com.bq.corbel.resources.rem.eventbus.AclConfigurationEventHandler;
import com.bq.corbel.resources.rem.service.AclConfigurationService;
import com.bq.corbel.resources.rem.service.AclResourcesService;
import com.bq.corbel.resources.rem.service.DefaultAclConfigurationService;
import com.bq.corbel.resources.rem.service.DefaultAclResourcesService;

import java.util.Arrays;
import java.util.Collections;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.core.env.Environment;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;

/**
 * @author Cristian del Cerro
 */
@Configuration @Import({ConfigurationIoC.class, EventBusListeningIoc.class}) public class RemAclIoc {

    @Autowired private Environment env;

    @Bean
    public EventHandler<ResourceEvent> getAclConfigurationEventHandlerAsEventHandler() {
        return new AclConfigurationEventHandler(env.getProperty("rem.acl.configuration.collection"));
    }

    @Bean(name = AclRemNames.POST)
    public Rem getAclPostRem() {
        return new AclPostRem(getAclResourceService(), Collections.singletonList(getAclPutRem()));
    }

    @Bean(name = AclRemNames.GET)
    public Rem getAclGetRem() {
        return new AclGetRem(getAclResourceService());
    }

    @Bean(name = AclRemNames.PUT)
    public Rem getAclPutRem() {
        return new AclPutRem(getAclResourceService(), Collections.singletonList(getAclGetRem()));
    }

    @Bean(name = AclRemNames.DELETE)
    public Rem getAclDeleteRem() {
        return new AclDeleteRem(getAclResourceService(), Collections.singletonList(getAclGetRem()));
    }

    @Bean(name = AclRemNames.SETUP_PUT)
    public Rem getAclSetUpPutRem() {
        return new SetUpAclPutRem(getAclResourceService(), Arrays.asList(getAclGetRem(), getAclPutRem()));
    }

    @Bean(name = AclRemNames.ADMIN_POST)
    public Rem getAclPostManagedCollectionRem() {
        return new AclPostManagedCollectionRem(getAclConfigurationService());
    }

    @Bean(name = AclRemNames.ADMIN_PUT)
    public Rem getAclPutManagedCollectionRem() {
        return new AclPutManagedCollectionRem(getAclConfigurationService());
    }

    @Bean(name = AclRemNames.ADMIN_GET)
    public Rem getAclGetManagedCollectionRem() {
        return new AclGetManagedCollectionRem(getAclConfigurationService());
    }

    @Bean
    public Gson getGson() {
        return new Gson();
    }

    @Bean
    public ObjectMapper getObjectMapper() {
        return new ObjectMapper();
    }

    @Bean
    public AclResourcesService getAclResourceService() {
        return new DefaultAclResourcesService(getGson(), env.getProperty("rem.acl.configuration.collection"));
    }

    @Bean
    public AclConfigurationService getAclConfigurationService() {
        return new DefaultAclConfigurationService(getGson(), env.getProperty("rem.acl.configuration.collection"), getAclResourceService());
    }

}
