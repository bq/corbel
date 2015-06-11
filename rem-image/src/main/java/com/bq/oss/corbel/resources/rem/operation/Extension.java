package com.bq.oss.corbel.resources.rem.operation;

import com.bq.oss.corbel.resources.rem.exception.ImageOperationsException;
import org.im4java.core.IMOperation;
import org.im4java.core.IMOps;

import java.util.Arrays;

/**
 * This is not a existing (bash) convert parameter.
 * However, is
 */
public class Extension implements ImageOperation {

    public static final String[] validExtensions = new String[]{"jpg", "jpeg", "tif", "tiff", "png", "gif", "bmp", "3fr", "arw", "srf", "sr2", "bay", "crw", "cr2", "cap", "tif", "iiq", "eip", "dcs", "dcr", "drf", "k25", "kdc", "dng", "erf", "fff", "mef", "mos", "mrw", "nef", "nrw", "orf", "ptx", "pef", "pxn", "R3D", "raf", "raw", "rw2", "rwl", "rwz", "x3f"};
    public static final String operationName = "extension";

    @Override
    public IMOps apply(String parameter) throws ImageOperationsException {
        IMOperation op = new IMOperation();
        op.addRawArgs(operationName);

        if (!checkExtension(parameter))
            throw new ImageOperationsException("Unknown extension: " + operationName);
        return op;
    }

    public String getOperationName() {
        return operationName;
    }

    @Override
    public boolean isRealOperation() {
        return false;
    }

    private boolean checkExtension(String extension) {
        return Arrays.asList(validExtensions).contains(extension.toLowerCase());
    }
}
