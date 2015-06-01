package com.bq.oss.corbel.resources.rem.service;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Arrays;
import java.util.List;

import org.im4java.core.IM4JavaException;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import com.bq.oss.corbel.resources.rem.exception.ImageOperationsException;
import com.bq.oss.corbel.resources.rem.operation.*;
import com.google.common.collect.ImmutableMap;

@Ignore// Needs imagemagick package installed
public class DefaultImageOperationsServiceIntegrationTest {

    private static final String IMAGE_URL = "http://www.7gnow.com/wp-content/uploads/2014/10/20/20-Funniest-Sports-Faces1.jpg";

    private DefaultImageOperationsService imageOperationsService;

    @Before
    public void setUp() {
        imageOperationsService = new DefaultImageOperationsService(new DefaultImageOperationsService.IMOperationFactory(),
                new DefaultImageOperationsService.ConvertCmdFactory(), ImmutableMap.<String, ImageOperation>builder()
                        .put("resizeHeight", new ResizeHeight()).put("resize", new Resize()).put("cropFromCenter", new CropFromCenter())
                        .put("resizeAndFill", new ResizeAndFill()).build());
    }

    @Test
    public void resizeTest() throws IOException, ImageOperationsException, InterruptedException, IM4JavaException {
        try (InputStream image = new URL(IMAGE_URL).openStream(); FileOutputStream out = new FileOutputStream("/tmp/testImage.jpg")) {

            List<List<String>> parameters = Arrays.asList(Arrays.asList("resizeHeight", "300"),
                    Arrays.asList("cropFromCenter", "(50, 50)"), Arrays.asList("resize", "(200, 100)"),
                    Arrays.asList("resizeAndFill", "(200, blue)"));

            imageOperationsService.applyConversion(parameters, image, out);
        }
    }

}
