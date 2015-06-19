package com.bq.oss.corbel.resources.rem.ioc;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.bq.oss.corbel.resources.rem.ImageDeleteRem;
import com.bq.oss.corbel.resources.rem.ImagePutRem;
import com.bq.oss.corbel.resources.rem.service.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.core.env.Environment;
import org.springframework.scheduling.annotation.EnableAsync;

import com.bq.oss.corbel.resources.rem.ImageGetRem;
import com.bq.oss.corbel.resources.rem.Rem;
import com.bq.oss.corbel.resources.rem.operation.*;
import com.bq.oss.lib.config.ConfigurationIoC;

@SuppressWarnings("unused") @Configuration @EnableAsync @Import({ConfigurationIoC.class}) public class RemImageIoc {

    @Autowired private Environment env;
    @Autowired private RemService remService;

    @Bean
    public static Map<String, ImageOperation> getOperations(List<ImageOperation> imageOperationList) {
        return imageOperationList.stream().collect(Collectors.toMap(ImageOperation::getOperationName, imageOperation -> imageOperation));
    }

    @Bean
    public ImageOperationsService getImageOperationsService(List<ImageOperation> imageOperationList) {
        return new DefaultImageOperationsService(new DefaultImageOperationsService.IMOperationFactory(),
                new DefaultImageOperationsService.ConvertCmdFactory(), getOperations(imageOperationList));
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

    @Bean(name = RemImageIocNames.REM_PUT)
    public Rem getImagePutRem(RemService remService) {
        return new ImagePutRem(remService, env.getProperty("image.cache.collection", "image:ImageCache"));
    }

    @Bean(name = RemImageIocNames.REM_DELETE)
    public Rem getImageDeleteRem(RemService remService) {
        return new ImageDeleteRem(remService, env.getProperty("image.cache.collection", "image:ImageCache"));
    }

}
