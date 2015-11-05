package io.corbel.resources.rem.plugin;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;

import io.corbel.lib.config.ConfigurationHelper;
import io.corbel.resources.rem.Rem;
import io.corbel.resources.rem.RemRegistry;
import io.corbel.resources.rem.acl.*;
import io.corbel.resources.rem.eventbus.AclConfigurationEventHandler;
import io.corbel.resources.rem.ioc.AclRemNames;
import io.corbel.resources.rem.ioc.RemAclIoc;
import io.corbel.resources.rem.service.AclResourcesService;

/**
 * @author Cristian del Cerro
 */

@Component public class RemAclPlugin extends RemPlugin {

    private static final Logger LOG = LoggerFactory.getLogger(RemAclPlugin.class);

    private static final String ACL_MEDIA_TYPE = "application/corbel.acl+json";
    private static final String ARTIFACT_ID = "rem-acl";
    private static final String ACL_CONFIGURATION_COLLECTION = "acl:Configuration";

    private String aclConfigurationCollection;

    @Override
    protected void init() {
        LOG.info("Initializing ACL plugin.");
        super.init();
        ConfigurationHelper.setConfigurationNamespace(ARTIFACT_ID);
        context = new AnnotationConfigApplicationContext(RemAclIoc.class);
        context.getBean(AclResourcesService.class).setRemService(remService);
        context.getBean(AclConfigurationEventHandler.class).setRemService(remService);
        context.getBean(AclPostRem.class).setRemService(remService);
        context.getBean(AclGetRem.class).setRemService(remService);
        context.getBean(AclPutRem.class).setRemService(remService);
        context.getBean(AclDeleteRem.class).setRemService(remService);
        context.getBean(SetUpAclPutRem.class).setRemService(remService);
        aclConfigurationCollection = context.getEnvironment().getProperty("rem.acl.configuration.collection", ACL_CONFIGURATION_COLLECTION);
    }

    @Override
    protected void register(RemRegistry registry) {
        registry.registerRem(context.getBean(AclRemNames.SETUP_PUT, Rem.class), ".*", MediaType.valueOf(ACL_MEDIA_TYPE), HttpMethod.PUT);
        registry.registerRem(context.getBean(AclRemNames.ADMIN_POST, Rem.class), aclConfigurationCollection, MediaType.ALL,
                HttpMethod.POST);
        registry.registerRem(context.getBean(AclRemNames.ADMIN_PUT, Rem.class), aclConfigurationCollection, MediaType.ALL, HttpMethod.PUT);
    }

    @Override
    protected String getArtifactName() {
        return ARTIFACT_ID;
    }
}
