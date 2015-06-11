package com.bq.oss.corbel.resources.rem.service;

import com.bq.oss.corbel.resources.rem.exception.ImageOperationsException;
import com.bq.oss.corbel.resources.rem.model.ImageOperationDescription;
import com.bq.oss.corbel.resources.rem.operation.ImageOperation;
import org.im4java.core.ConvertCmd;
import org.im4java.core.IM4JavaException;
import org.im4java.core.IMOperation;
import org.im4java.process.Pipe;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class DefaultImageOperationsService implements ImageOperationsService {

    private final IMOperationFactory imOperationFactory;
    private final ConvertCmdFactory convertCmdFactory;
    private final Map<String, ImageOperation> operations;
    private final String DEF_IMAGE_ARG = "-";
    public static final String[] VALID_FORMATS = new String[]{"jpg", "jpeg", "tif", "tiff", "png", "gif", "bmp", "3fr", "arw", "srf", "sr2", "bay", "crw", "cr2", "cap", "tif", "iiq", "eip", "dcs", "dcr", "drf", "k25", "kdc", "dng", "erf", "fff", "mef", "mos", "mrw", "nef", "nrw", "orf", "ptx", "pef", "pxn", "R3D", "raf", "raw", "rw2", "rwl", "rwz", "x3f"};

    public DefaultImageOperationsService(IMOperationFactory imOperationFactory, ConvertCmdFactory convertCmdFactory,
                                         Map<String, ImageOperation> operations) {
        this.imOperationFactory = imOperationFactory;
        this.convertCmdFactory = convertCmdFactory;
        this.operations = operations;
    }

    @Override
    public void applyConversion(List<ImageOperationDescription> parameters, InputStream image, OutputStream out, Optional<String> format) throws ImageOperationsException, InterruptedException, IOException, IM4JavaException {
        String finalOutputExtension = getOutputFormatParameter(format);
        IMOperation imOperation = imOperationFactory.create();
        addDefaultImageToIMOperation(imOperation);
        setOperations(imOperation, parameters);
        addImageToIMOperation(imOperation, finalOutputExtension);

        ConvertCmd convertCmd = convertCmdFactory.create(image, out);
        convertCmd.run(imOperation);
    }

    private void setOperations(IMOperation imOperation, List<ImageOperationDescription> parameters) throws ImageOperationsException {
        for (ImageOperationDescription parameter : parameters) {

            String operationName = parameter.getName();
            ImageOperation currentOperation = operations.get(operationName);

            if (currentOperation == null) {
                throw new ImageOperationsException("Unknown operation: " + operationName);
            }

            imOperation.addSubOperation(currentOperation.apply(parameter.getParameters()));
        }
    }

    private void addDefaultImageToIMOperation(IMOperation imOperation) {
        addImageToIMOperation(imOperation, DEF_IMAGE_ARG);
    }

    private void addImageToIMOperation(IMOperation imOperation, String extension) {
        imOperation.addImage(extension);
    }

    public static class IMOperationFactory {
        public IMOperation create() {
            return new IMOperation();
        }
    }

    public static class ConvertCmdFactory {
        public ConvertCmd create(InputStream in, OutputStream out) {
            ConvertCmd convertCmd = new ConvertCmd();
            convertCmd.setInputProvider(new Pipe(in, null));
            convertCmd.setOutputConsumer(new Pipe(null, out));
            return convertCmd;
        }
    }

    public String getOutputFormatParameter(Optional<String> format) throws ImageOperationsException {
        checkImageFormat(format);
        return format.map(formatIn -> formatIn+":-").orElse(DEF_IMAGE_ARG);
    }

    public void checkImageFormat(Optional<String> format) throws ImageOperationsException {
        if ( format.isPresent() && !Arrays.asList(VALID_FORMATS).contains(format.get().toLowerCase()))
            throw new ImageOperationsException("Unknown image format: " + format);
    }

}
