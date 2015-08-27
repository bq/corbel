package io.corbel.resources.rem.operation;

public abstract class BaseResize implements ImageOperation {

    public static final int MAX_SIZE_VALUE = 4096; //4K

    public int getSafeResizeParameter(String parameterStringValue) throws NumberFormatException {
        int paramWidth = Integer.parseInt(parameterStringValue);
        return Math.min(paramWidth, MAX_SIZE_VALUE);
    }
}
