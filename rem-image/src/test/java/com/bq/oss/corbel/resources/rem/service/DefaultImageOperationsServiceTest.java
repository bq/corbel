package com.bq.oss.corbel.resources.rem.service;

import static org.fest.assertions.api.Assertions.assertThat;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.*;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.im4java.core.ConvertCmd;
import org.im4java.core.IM4JavaException;
import org.im4java.core.IMOperation;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import com.bq.oss.corbel.resources.rem.exception.ImageOperationsException;
import com.bq.oss.corbel.resources.rem.operation.ImageOperation;
import com.google.common.collect.ImmutableMap;

@RunWith(MockitoJUnitRunner.class) public class DefaultImageOperationsServiceTest {

    private static final Map<String, ImageOperation> operations = ImmutableMap.<String, ImageOperation>builder()
            .put("resizeWidth", parameter -> {
                try {
                    return new IMOperation().resize(Integer.parseInt(parameter));
                } catch (NumberFormatException e) {
                    throw new ImageOperationsException("Bad parameter: " + parameter);
                }
            }).build();
    @Mock private DefaultImageOperationsService.IMOperationFactory imOperationFactory;
    @Mock private DefaultImageOperationsService.ConvertCmdFactory convertCmdFactory;
    private DefaultImageOperationsService defaultImageOperationsService;

    @Before
    public void setUp() {
        defaultImageOperationsService = new DefaultImageOperationsService(imOperationFactory, convertCmdFactory, operations);
    }

    @Test
    public void applyConversionTest() throws InterruptedException, IM4JavaException, ImageOperationsException, IOException {
        InputStream image = mock(InputStream.class);
        OutputStream out = mock(OutputStream.class);
        IMOperation imOperation = mock(IMOperation.class);
        ConvertCmd convertCmd = mock(ConvertCmd.class);

        List<List<String>> parameters = Collections.singletonList(Arrays.asList("resizeWidth", "10"));

        when(imOperationFactory.create()).thenReturn(imOperation);
        when(convertCmdFactory.create(any(), any())).thenReturn(convertCmd);

        defaultImageOperationsService.applyConversion(parameters, image, out);

        verify(imOperationFactory).create();
        verify(imOperation, times(2)).addImage(eq("-"));

        ArgumentCaptor<IMOperation> capturedIMOperation = ArgumentCaptor.forClass(IMOperation.class);
        verify(imOperation).addSubOperation(capturedIMOperation.capture());
        assertThat(capturedIMOperation.getValue().getCmdArgs().toString()).isEqualTo("[-resize, 10]");

        verify(convertCmdFactory).create(any(), any());
        verify(convertCmd).run(imOperation);

        verifyNoMoreInteractions(imOperationFactory, convertCmdFactory, imOperation, convertCmd, image, out);
    }

    @Test(expected = ImageOperationsException.class)
    public void applyConversionWithBadOperationTest() throws InterruptedException, IM4JavaException, ImageOperationsException, IOException {
        try {
            applyConversionAndFailTest(Collections.singletonList(Arrays.asList("resizeWidth", "10", "30")));
        } catch (ImageOperationsException e) {
            assertThat(e.getMessage()).isEqualTo("Bad operation: [resizeWidth, 10, 30]");
            throw e;
        }
    }

    @Test(expected = ImageOperationsException.class)
    public void applyConversionWithUnknownOperationTest() throws InterruptedException, IM4JavaException, ImageOperationsException,
            IOException {
        try {
            applyConversionAndFailTest(Collections.singletonList(Arrays.asList("gaussianBlur", "10")));
        } catch (ImageOperationsException e) {
            assertThat(e.getMessage()).isEqualTo("Unknown operation: gaussianBlur");
            throw e;
        }
    }

    private void applyConversionAndFailTest(List<List<String>> parameters) throws InterruptedException, IM4JavaException,
            ImageOperationsException, IOException {
        InputStream image = mock(InputStream.class);
        OutputStream out = mock(OutputStream.class);
        IMOperation imOperation = mock(IMOperation.class);
        ConvertCmd convertCmd = mock(ConvertCmd.class);

        when(imOperationFactory.create()).thenReturn(imOperation);

        try {
            defaultImageOperationsService.applyConversion(parameters, image, out);
        } catch (ImageOperationsException e) {
            verify(imOperationFactory).create();
            verify(imOperation).addImage(eq("-"));

            verifyNoMoreInteractions(imOperationFactory, convertCmdFactory, imOperation, convertCmd, image, out);

            throw e;
        }
    }

}
