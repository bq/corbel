package com.bq.oss.corbel.resources.rem.format;

import com.bq.oss.corbel.resources.rem.exception.ImageOperationsException;
import org.junit.Test;

import static org.fest.assertions.api.Assertions.assertThat;


public class ImageFormatTest {

    @Test(expected = IllegalArgumentException.class)
    public void nonExistingFormatTest(){
        ImageFormat imageFormat = ImageFormat.safeValueOf("NOT_A_REAL_FORMAT");
    }

    @Test
    public void existingFormatTest() throws ImageOperationsException {
        final String PNG = "PNG";
        final String JPG = "JPG";
        ImageFormat imageFormat = ImageFormat.safeValueOf(PNG);
        assertThat(imageFormat.toString()).isEqualTo(PNG);

        imageFormat = ImageFormat.safeValueOf(JPG);
        assertThat(imageFormat.toString()).isEqualTo(JPG);
    }
}
