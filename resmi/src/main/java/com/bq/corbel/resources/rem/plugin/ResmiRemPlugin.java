package com.bq.corbel.resources.rem.plugin;

import com.bq.corbel.lib.config.ConfigurationHelper;
import com.bq.corbel.resources.cli.dsl.ResmiShell;
import com.bq.corbel.resources.rem.Rem;
import com.bq.corbel.resources.rem.RemRegistry;
import com.bq.corbel.resources.rem.resmi.ioc.ResmiIoc;
import com.bq.corbel.resources.rem.resmi.ioc.ResmiRemNames;
import com.bq.corbel.resources.rem.search.ElasticSearchService;
import com.bq.corbel.resources.rem.search.ResmiSearch;
import com.bq.corbel.resources.rem.service.ResmiService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;

import com.codahale.metrics.health.HealthCheck;

@Component public class ResmiRemPlugin extends RemPlugin {

    private static final Logger LOG = LoggerFactory.getLogger(ResmiRemPlugin.class);
    private static final String ARTIFACT_ID = "resmi";

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
        shell.setElasticSearchService(context.getBean(ElasticSearchService.class));
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

    @Override
    protected void addHealthCheck(HealthCheckRegistry healthCheckRegistry) {
        healthCheckRegistry.addHealthCheck(ResmiRemNames.ELASTICSEARCH_HEALTHCHECK,
                context.getBean(ResmiRemNames.ELASTICSEARCH_HEALTHCHECK, HealthCheck.class));
    }

}
