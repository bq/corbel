package com.bq.oss.corbel.resources.rem.operation;

import com.bq.oss.corbel.resources.rem.exception.ImageOperationsException;
import org.im4java.core.IMOperation;
import org.im4java.core.IMOps;

public class ResizeWidth implements ImageOperation {

    @Override
    public IMOps apply(String parameter) throws ImageOperationsException {
        try {

            return new IMOperation().resize(Integer.parseInt(parameter));

        } catch (NumberFormatException e) {
            throw new ImageOperationsException("Bad image width: " + parameter, e);
        }
    }

    @Override
    public String getOperationName() {
        return "resizeWidth";
    }

    @Override
    public boolean isRealOperation() {
        return true;
    }
}
