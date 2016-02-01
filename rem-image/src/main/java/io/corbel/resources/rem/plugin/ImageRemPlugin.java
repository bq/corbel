package io.corbel.resources.rem.plugin;

import io.corbel.resources.rem.*;
import io.corbel.resources.rem.ioc.RemImageIoc;
import io.corbel.resources.rem.ioc.RemImageIocNames;
import io.corbel.lib.config.ConfigurationHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;

@Component
public class ImageRemPlugin extends RemPlugin {

    private static final Logger LOG = LoggerFactory.getLogger(ImageRemPlugin.class);
    private static final String IMAGE_CACHE_COLLECTION = "image.cache.collection";
    private static final String IMAGE_PATH = "image/*";
    private final String ARTIFACT_ID = "rem-image";

    @Override
    protected void init() {
        LOG.info("Initializing Image plugin.");
        super.init();
        ConfigurationHelper.setConfigurationNamespace(ARTIFACT_ID);
        context = new AnnotationConfigApplicationContext(RemImageIoc.class);
    }

    @Override
    protected void register(RemRegistry registry) {
        ImageGetRem beanImageGetRem = (ImageGetRem) context.getBean(RemImageIocNames.REM_GET, Rem.class);
        beanImageGetRem.setRemService(remService);

        ImagePutRem beanImagePutRem = (ImagePutRem) context.getBean(RemImageIocNames.REM_PUT, Rem.class);
        beanImagePutRem.setRemService(remService);

        ImageDeleteRem beanImageDeleteRem = (ImageDeleteRem) context.getBean(RemImageIocNames.REM_DELETE, Rem.class);
        beanImageDeleteRem.setRemService(remService);

        registry.registerRem(beanImageGetRem, "^(?!" + context.getEnvironment().getProperty(IMAGE_CACHE_COLLECTION) + "$).*",
                MediaType.parseMediaType(IMAGE_PATH), HttpMethod.GET);
        registry.registerRem(beanImagePutRem, "^(?!" + context.getEnvironment().getProperty(IMAGE_CACHE_COLLECTION) + "$).*",
                MediaType.parseMediaType(IMAGE_PATH), HttpMethod.PUT);
        registry.registerRem(beanImageDeleteRem, "^(?!" + context.getEnvironment().getProperty(IMAGE_CACHE_COLLECTION) + "$).*",
                MediaType.parseMediaType(IMAGE_PATH), HttpMethod.DELETE);
    }

    @Override
    protected String getArtifactName() {
        return ARTIFACT_ID;
    }

}
