package com.bq.oss.corbel.resources.rem.operation;

import static org.fest.assertions.api.Assertions.assertThat;

import java.util.Collections;
import java.util.List;

import org.im4java.core.IMOps;
import org.junit.Test;

import com.bq.oss.corbel.resources.rem.exception.ImageOperationsException;

public class ImageOperationsTest {

    @Test
    public void cropTest() throws ImageOperationsException {
        List<String> inputParameters = Collections.singletonList("(10, 20, 30, 40)");
        List<String> expectedOutputs = Collections.singletonList("[-crop, 20x20+10+20]");

        operationTest(inputParameters, expectedOutputs, new Crop());
    }

    @Test
    public void cropFromCenterTest() throws ImageOperationsException {
        List<String> inputParameters = Collections.singletonList("(10, 20)");
        List<String> expectedOutputs = Collections.singletonList("[-gravity, center, -crop, 10x20-5-10]");

        operationTest(inputParameters, expectedOutputs, new CropFromCenter());
    }

    @Test
    public void resizeTest() throws ImageOperationsException {
        List<String> inputParameters = Collections.singletonList("(10, 20)");
        List<String> expectedOutputs = Collections.singletonList("[-resize, 10x20!]");

        operationTest(inputParameters, expectedOutputs, new Resize());
    }

    @Test
    public void resizeAndFillTest() throws ImageOperationsException {
        List<String> inputParameters = Collections.singletonList("(10, orange)");
        List<String> expectedOutputs = Collections.singletonList("[-resize, 10x10, -background, orange, -gravity, center, -extent, 10x10]");

        operationTest(inputParameters, expectedOutputs, new ResizeAndFill());
    }

    @Test
    public void resizeHeightTest() throws ImageOperationsException {
        List<String> inputParameters = Collections.singletonList("10");
        List<String> expectedOutputs = Collections.singletonList("[-resize, x10]");

        operationTest(inputParameters, expectedOutputs, new ResizeHeight());
    }

    @Test
    public void resizeWidthTest() throws ImageOperationsException {
        List<String> inputParameters = Collections.singletonList("10");
        List<String> expectedOutputs = Collections.singletonList("[-resize, 10]");

        operationTest(inputParameters, expectedOutputs, new ResizeWidth());
    }

    private void operationTest(List<String> inputParameters, List<String> expectedOutputs, ImageOperation operation)
            throws ImageOperationsException {
        assertThat(inputParameters.size()).isEqualTo(expectedOutputs.size());

        for (int i = 0; i < inputParameters.size(); ++i) {
            IMOps imOps = operation.apply(inputParameters.get(i));
            assertThat(imOps.getCmdArgs().toString()).isEqualTo(expectedOutputs.get(i));
        }
    }

}
