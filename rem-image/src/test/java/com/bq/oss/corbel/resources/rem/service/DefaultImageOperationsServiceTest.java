package com.bq.oss.corbel.resources.rem.service;

import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.*;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.im4java.core.IM4JavaException;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import com.bq.oss.corbel.resources.rem.exception.ImageOperationsException;
import com.bq.oss.corbel.resources.rem.util.ChainedImageOperations;

@RunWith(MockitoJUnitRunner.class) public class DefaultImageOperationsServiceTest {

    @Mock private DefaultImageOperationsService.ChainedImageOperationsFactory chainedImageOperationsFactory;

    private DefaultImageOperationsService defaultImageOperationsService;

    @Before
    public void setUp() {
        defaultImageOperationsService = new DefaultImageOperationsService(chainedImageOperationsFactory);
    }

    @Test
    public void applyConversionTest() throws InterruptedException, IM4JavaException, ImageOperationsException, IOException {
        ChainedImageOperations chainedImageOperations = mock(ChainedImageOperations.class);
        InputStream image = mock(InputStream.class);
        OutputStream out = mock(OutputStream.class);

        List<List<String>> parameters = Arrays.asList(Arrays.asList("resizeWidth", "10"), Arrays.asList("resizeHeight", "20"),
                Arrays.asList("resizeAndFill", "(70, blue)"), Arrays.asList("crop", "(10, 20, 30, 40)"),
                Arrays.asList("cropFromCenter", "(20, 25)"));

        when(chainedImageOperationsFactory.build()).thenReturn(chainedImageOperations);


        defaultImageOperationsService.applyConversion(parameters, image, out);

        verify(chainedImageOperationsFactory).build();
        verify(chainedImageOperations).resize(eq(10), eq(null));
        verify(chainedImageOperations).resize(eq(null), eq(20));
        verify(chainedImageOperations).resizeAndFill(eq(70), eq("blue"));
        verify(chainedImageOperations).crop(eq(10), eq(20), eq(30), eq(40));
        verify(chainedImageOperations).crop(eq(20), eq(25));
        verify(chainedImageOperations).run(image, out);

        verifyNoMoreInteractions(chainedImageOperationsFactory, chainedImageOperations, image, out);
    }

    @Test(expected = ImageOperationsException.class)
    public void applyConversionAndFailTest() throws InterruptedException, IM4JavaException, ImageOperationsException, IOException {
        ChainedImageOperations chainedImageOperations = mock(ChainedImageOperations.class);
        InputStream image = mock(InputStream.class);
        OutputStream out = mock(OutputStream.class);

        List<List<String>> parameters = Collections.singletonList(Arrays.asList("resizeAndFill", "(70a, blue)"));

        when(chainedImageOperationsFactory.build()).thenReturn(chainedImageOperations);

        try {
            defaultImageOperationsService.applyConversion(parameters, image, out);
        }

        catch (ImageOperationsException e) {
            verify(chainedImageOperationsFactory).build();
            verifyNoMoreInteractions(chainedImageOperationsFactory, chainedImageOperations, image, out);

            throw e;
        }
    }

}
