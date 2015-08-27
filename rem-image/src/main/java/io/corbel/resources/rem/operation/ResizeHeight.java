package io.corbel.resources.rem.operation;

import io.corbel.resources.rem.exception.ImageOperationsException;
import org.im4java.core.IMOperation;
import org.im4java.core.IMOps;

import java.awt.image.BufferedImage;

public class ResizeHeight extends BaseResize {

    @Override
    public IMOps apply(String parameter, BufferedImage bufferedImage) throws ImageOperationsException {
        int height;

        try {
            height = getBoundedHeightValue(parameter, bufferedImage);
        } catch (NumberFormatException e) {
            throw new ImageOperationsException("Bad image height: " + parameter, e);
        }

        if (height <= 0) {
            throw new ImageOperationsException("Height for resizeHeight must be greater than 0: " + parameter);
        }

        return new IMOperation().resize(null, height);
    }

    @Override
    public String getOperationName() {
        return "resizeHeight";
    }
}
