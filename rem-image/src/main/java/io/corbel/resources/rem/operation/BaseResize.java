package io.corbel.resources.rem.operation;

import java.awt.image.BufferedImage;

public abstract class BaseResize implements ImageOperation {

    protected int getBoundedWidthValue(String paramWidthStr, BufferedImage image) throws NumberFormatException {
        try {
            int paramWidth = Integer.parseInt(paramWidthStr);
            return Math.min(paramWidth, image.getWidth());
        } catch (NullPointerException n) {
            return MAX_SIZE_VALUE;
        }
    }

    protected int getBoundedHeightValue(String paramHeightStr, BufferedImage image) throws NumberFormatException {
        try {
            int paramHeight = Integer.parseInt(paramHeightStr);
            return Math.min(paramHeight, image.getHeight());
        } catch (NullPointerException e) {
            return MAX_SIZE_VALUE;
        }
    }
}
