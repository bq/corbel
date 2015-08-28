package io.corbel.resources.rem.operation;

import io.corbel.resources.rem.exception.ImageOperationsException;
import org.im4java.core.IMOperation;
import org.im4java.core.IMOps;

public class ResizeWidth extends BaseResize {

    @Override
    public IMOps apply(String parameter) throws ImageOperationsException {
        int width;

        try {
            width = getSafeResizeParameter(parameter);
        } catch (NumberFormatException e) {
            throw new ImageOperationsException("Bad image width: " + parameter, e);
        }

        if (width <= 0) {
            throw new ImageOperationsException("Width for resizeWidth must be greater than 0: " + parameter);
        }
        return new IMOperation().resize(width);
    }

    @Override
    public String getOperationName() {
        return "resizeWidth";
    }
}
