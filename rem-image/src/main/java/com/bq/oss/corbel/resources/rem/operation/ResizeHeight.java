package com.bq.oss.corbel.resources.rem.operation;

import com.bq.oss.corbel.resources.rem.exception.ImageOperationsException;
import org.im4java.core.IMOperation;
import org.im4java.core.IMOps;

public class ResizeHeight implements ImageOperation {

    @Override
    public IMOps apply(String parameter) throws ImageOperationsException {
        try {

            return new IMOperation().resize(null, Integer.parseInt(parameter));

        } catch (NumberFormatException e) {
            throw new ImageOperationsException("Bad image height: " + parameter, e);
        }
    }

    @Override
    public String getOperationName() {
        return "resizeHeight";
    }

    @Override
    public boolean isRealOperation() {
        return true;
    }
}
