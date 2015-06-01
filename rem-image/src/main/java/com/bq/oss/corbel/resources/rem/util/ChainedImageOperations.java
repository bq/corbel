package com.bq.oss.corbel.resources.rem.util;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Collections;
import java.util.List;

import org.im4java.core.ConvertCmd;
import org.im4java.core.IM4JavaException;
import org.im4java.core.IMOperation;
import org.im4java.process.Pipe;

public class ChainedImageOperations {

    private IMOperation imOperation = null;

    private void initialize() {
        imOperation = new IMOperation();
        imOperation.addImage("-");
    }

    public ChainedImageOperations resize(Integer width, Integer height) {
        if (imOperation == null)
            initialize();

        IMOperation subOperation = new IMOperation();

        if (width != null && height != null) {
            subOperation.resize(width, height, '!');
        } else {
            subOperation.resize(width, height);
        }

        imOperation.addSubOperation(subOperation);

        return this;
    }

    public ChainedImageOperations crop(int xorig, int yorig, int xdest, int ydest) {
        if (imOperation == null)
            initialize();

        int xsize = xdest - xorig;
        int ysize = ydest - yorig;

        IMOperation subOperation = new IMOperation();
        subOperation.crop(xsize, ysize, xorig, yorig);

        imOperation.addSubOperation(subOperation);

        return this;
    }

    public ChainedImageOperations crop(int xratio, int yratio) {
        if (imOperation == null)
            initialize();

        IMOperation subOperation = new IMOperation();
        subOperation.gravity("center");
        subOperation.crop(xratio, yratio, -xratio / 2, -yratio / 2);

        imOperation.addSubOperation(subOperation);

        return this;
    }

    public ChainedImageOperations resizeAndFill(int width, String color) {
        if (imOperation == null)
            initialize();

        IMOperation subOperation = new IMOperation();
        subOperation.resize(width, width);
        subOperation.background(color);
        subOperation.gravity("center");
        subOperation.extent(width, width);

        imOperation.addSubOperation(subOperation);

        return this;
    }

    public void run(InputStream image, OutputStream out) throws InterruptedException, IOException, IM4JavaException {
        if (imOperation == null)
            return;

        imOperation.addImage("-");

        ConvertCmd convertCmd = new ConvertCmd();
        convertCmd.setInputProvider(new Pipe(image, null));
        convertCmd.setOutputConsumer(new Pipe(null, out));
        convertCmd.run(imOperation);

        imOperation = null;
    }

    public void reset() {
        imOperation = null;
    }

    public List<String> getImOperationCmdArgs() {
        return (imOperation == null) ? Collections.emptyList() : imOperation.getCmdArgs();
    }
}
