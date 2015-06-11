package com.bq.oss.corbel.resources.rem.format;

import com.bq.oss.corbel.resources.rem.exception.ImageOperationsException;
import org.junit.Test;

import static org.fest.assertions.api.Assertions.assertThat;


public class ImageFormatTest {

    @Test
    public void nullFormatTest() throws ImageOperationsException {
        ImageFormat imageFormat = new ImageFormat();
        assertThat(imageFormat.hasFormat()).isFalse();
    }

    @Test
    public void existingFormatTest() throws ImageOperationsException {
        final String PNG = "png";
        final String JPG = "jpg";
        ImageFormat imageFormat = new ImageFormat(PNG);
        assertThat(imageFormat.get()).isEqualTo(PNG);

        imageFormat = new ImageFormat(JPG);
        assertThat(imageFormat.get()).isEqualTo(JPG);
    }

    @Test(expected = ImageOperationsException.class)
    public void nonExistingFormatTest() throws ImageOperationsException {
        final String NOVALIDFORMAT = "NOVALIDFORMAT";
        ImageFormat imageFormat = new ImageFormat(NOVALIDFORMAT);
    }

}
