package com.bq.oss.corbel.resources.rem.util;

import static org.fest.assertions.api.Assertions.assertThat;

import java.io.IOException;
import java.util.Arrays;

import org.im4java.core.IM4JavaException;
import org.junit.Test;

public class ChainedImageOperationsTest {

    @Test
    public void resizeTest() throws InterruptedException, IOException, IM4JavaException {
        ChainedImageOperations chainedImageOperations = new ChainedImageOperations().resize(10, 10).resize(null, 10).resize(10, null);

        assertThat(chainedImageOperations.getImOperationCmdArgs()).isEqualTo(
                Arrays.asList("-", "(", "-resize", "10x10!", ")", "(", "-resize", "x10", ")", "(", "-resize", "10x", ")"));
    }

    @Test
    public void cropTest() {
        ChainedImageOperations chainedImageOperations = new ChainedImageOperations().crop(10, 10, 30, 15).crop(10, 10);

        assertThat(chainedImageOperations.getImOperationCmdArgs()).isEqualTo(
                Arrays.asList("-", "(", "-crop", "20x5+10+10", ")", "(", "-gravity", "center", "-crop", "10x10-5-5", ")"));
    }

    @Test
    public void fillTest() {
        ChainedImageOperations chainedImageOperations = new ChainedImageOperations().resizeAndFill(10, "blue");

        assertThat(chainedImageOperations.getImOperationCmdArgs()).isEqualTo(
                Arrays.asList("-", "(", "-resize", "10x10", "-background", "blue", "-gravity", "center", "-extent", "10x10", ")"));
    }

    @Test
    public void combinationTest() {
        ChainedImageOperations chainedImageOperations = new ChainedImageOperations().resizeAndFill(10, "orange").crop(5, 5);

        assertThat(chainedImageOperations.getImOperationCmdArgs()).isEqualTo(
                Arrays.asList("-", "(", "-resize", "10x10", "-background", "orange", "-gravity", "center", "-extent", "10x10", ")", "(",
                        "-gravity", "center", "-crop", "5x5-2-2", ")"));
    }
}
