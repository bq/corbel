package io.corbel.resources.rem.operation;

import io.corbel.resources.rem.exception.ImageOperationsException;
import org.im4java.core.IMOps;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import java.awt.image.BufferedImage;
import java.util.Collections;
import java.util.List;

import static org.fest.assertions.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class ImageOperationsTest {

    @Mock
    private static BufferedImage image = mock(BufferedImage.class);
    private final int IMAGE_WIDTH = 120;
    private final int IMAGE_HEIGHT = 100;

    @Before
    public void setUp() {
        when(image.getWidth()).thenReturn(IMAGE_WIDTH);
        when(image.getHeight()).thenReturn(IMAGE_HEIGHT);
    }

    @Test
    public void cropTest() throws ImageOperationsException {
        List<String> inputParameters = Collections.singletonList("(10, 20, 30, 40)");
        List<String> expectedOutputs = Collections.singletonList("[-crop, 20x20+10+20]");

        operationTest(inputParameters, expectedOutputs, new Crop());
    }

    @Test
    public void cropFromCenterTest() throws ImageOperationsException {
        List<String> inputParameters = Collections.singletonList("(10, 20)");
        List<String> expectedOutputs = Collections.singletonList("[-gravity, center, -crop, 10x20+0+0]");

        operationTest(inputParameters, expectedOutputs, new CropFromCenter());
    }

    @Test
    public void resizeTest() throws ImageOperationsException {
        List<String> inputParameters = Collections.singletonList("(10, 20)");
        List<String> expectedOutputs = Collections.singletonList("[-resize, 10x20!]");

        operationTest(inputParameters, expectedOutputs, new Resize());
    }

    @Test
    public void resizeTestExceedingImageValues() throws ImageOperationsException {
        List<String> inputParameters = Collections.singletonList("(" + (IMAGE_WIDTH + 1) + ", " + (IMAGE_HEIGHT + 1) + ")");
        List<String> expectedOutputs = Collections.singletonList("[-resize, " + IMAGE_WIDTH + "x" + IMAGE_HEIGHT + "!]");

        operationTest(inputParameters, expectedOutputs, new Resize());
    }

    @Test
    public void resizeWidthTestExceedingImageValues() throws ImageOperationsException {
        List<String> inputParameters = Collections.singletonList(String.valueOf(IMAGE_WIDTH + 1));
        List<String> expectedOutputs = Collections.singletonList("[-resize, " + IMAGE_WIDTH + "]");

        operationTest(inputParameters, expectedOutputs, new ResizeWidth());
    }

    @Test
    public void resizeHeightTestExceedingImageValues() throws ImageOperationsException {
        List<String> inputParameters = Collections.singletonList(String.valueOf(IMAGE_HEIGHT + 1));
        List<String> expectedOutputs = Collections.singletonList("[-resize, x" + IMAGE_HEIGHT + "]");

        operationTest(inputParameters, expectedOutputs, new ResizeHeight());
    }

    @Test
    public void resizeAndFillTest() throws ImageOperationsException {
        List<String> inputParameters = Collections.singletonList("(10, FF7300)");
        List<String> expectedOutputs = Collections.singletonList("[-resize, 10x10, -background, #FF7300, -gravity, center, -extent, 10x10]");

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
            IMOps imOps = operation.apply(inputParameters.get(i), image);
            assertThat(imOps.getCmdArgs().toString()).isEqualTo(expectedOutputs.get(i));
        }
    }

}
