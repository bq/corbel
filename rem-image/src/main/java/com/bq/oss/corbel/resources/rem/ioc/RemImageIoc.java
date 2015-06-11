package com.bq.oss.corbel.resources.rem.ioc;

import com.bq.oss.corbel.resources.rem.ImageGetRem;
import com.bq.oss.corbel.resources.rem.Rem;
import com.bq.oss.corbel.resources.rem.operation.*;
import com.bq.oss.corbel.resources.rem.service.DefaultImageCacheService;
import com.bq.oss.corbel.resources.rem.service.DefaultImageOperationsService;
import com.bq.oss.corbel.resources.rem.service.ImageCacheService;
import com.bq.oss.corbel.resources.rem.service.ImageOperationsService;
import com.bq.oss.lib.config.ConfigurationIoC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.core.env.Environment;
import org.springframework.scheduling.annotation.EnableAsync;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@SuppressWarnings("unused")
@Configuration
@EnableAsync
@Import({ConfigurationIoC.class})
public class RemImageIoc {

    @Autowired
    private Environment env;

    @Bean
    public static Map<String, ImageOperation> getOperations(List<ImageOperation> ImageOperationList) {
        return ImageOperationList.stream().collect(Collectors.toMap(ImageOperation::getOperationName, imageOperation -> imageOperation));
    }

    @Bean
    public ImageOperationsService getImageOperationsService(List<ImageOperation> ImageOperationList) {
        return new DefaultImageOperationsService(new DefaultImageOperationsService.IMOperationFactory(),
                new DefaultImageOperationsService.ConvertCmdFactory(), getOperations(ImageOperationList));
    }

    @Bean
    public Extension getExtensionOperation() {
        return new Extension();
    }

    @Bean
    public Crop getCropOperation() {
        return new Crop();
    }

    @Bean
    public CropFromCenter getCropFromCenterOperation() {
        return new CropFromCenter();
    }

    @Bean
    public Resize getResizeOperation() {
        return new Resize();
    }

    @Bean
    public ResizeAndFill getResizeAndFillOperation() {
        return new ResizeAndFill();
    }

    @Bean
    public ResizeHeight getResizeheight() {
        return new ResizeHeight();
    }

    @Bean
    public ResizeWidth getResizeWidth() {
        return new ResizeWidth();
    }

    @Bean
    public ImageCacheService getImageCacheService() {
        return new DefaultImageCacheService(env.getProperty("image.cache.collection"));
    }

    @Bean(name = RemImageIocNames.REM_GET)
    public Rem getImageGetRem(ImageOperationsService imageOperationsService, ImageCacheService imageCacheService) {
        return new ImageGetRem(imageOperationsService, imageCacheService);
    }

}
