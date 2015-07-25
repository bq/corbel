package com.bq.oss.corbel.resources.rem.plugin;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;

import com.bq.oss.corbel.resources.cli.dsl.ResmiShell;
import com.bq.oss.corbel.resources.rem.Rem;
import com.bq.oss.corbel.resources.rem.RemRegistry;
import com.bq.oss.corbel.resources.rem.resmi.ioc.ResmiIoc;
import com.bq.oss.corbel.resources.rem.resmi.ioc.ResmiRemNames;
import com.bq.oss.corbel.resources.rem.search.ResmiSearch;
import com.bq.oss.corbel.resources.rem.service.ResmiService;
import io.corbel.lib.config.ConfigurationHelper;

@Component public class ResmiRemPlugin extends RemPlugin {

    private static final Logger LOG = LoggerFactory.getLogger(ResmiRemPlugin.class);
    private final String ARTIFACT_ID = "resmi";

    @Autowired private ResmiShell shell;

    @Override
    protected void init() {
        LOG.info("Initializing RESMI plugin.");
        super.init();
        ConfigurationHelper.setConfigurationNamespace(ARTIFACT_ID);
        context = new AnnotationConfigApplicationContext(ResmiIoc.class);
    }

    @Override
    protected void console() {
        init();
        shell.setResmiService(context.getBean(ResmiService.class));
        shell.setResmiSearch(context.getBean(ResmiSearch.class));
    }

    @Override
    protected void register(RemRegistry registry) {
        registry.registerRem(context.getBean(ResmiRemNames.RESMI_GET, Rem.class), ".*", MediaType.APPLICATION_JSON, HttpMethod.GET);
        registry.registerRem(context.getBean(ResmiRemNames.RESMI_POST, Rem.class), ".*", MediaType.APPLICATION_JSON, HttpMethod.POST);
        registry.registerRem(context.getBean(ResmiRemNames.RESMI_PUT, Rem.class), ".*", MediaType.APPLICATION_JSON, HttpMethod.PUT);
        registry.registerRem(context.getBean(ResmiRemNames.RESMI_DELETE, Rem.class), ".*", MediaType.APPLICATION_JSON, HttpMethod.DELETE);
    }

    @Override
    protected String getArtifactName() {
        return ARTIFACT_ID;
    }

}
