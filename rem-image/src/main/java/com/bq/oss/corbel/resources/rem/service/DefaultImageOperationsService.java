package com.bq.oss.corbel.resources.rem.service;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.im4java.core.ConvertCmd;
import org.im4java.core.IM4JavaException;
import org.im4java.core.IMOperation;
import org.im4java.process.Pipe;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import com.bq.oss.corbel.resources.rem.exception.ImageOperationsException;
import com.bq.oss.corbel.resources.rem.operation.ImageOperation;
import com.bq.oss.corbel.resources.rem.operation.ImageOperationName;

public class DefaultImageOperationsService implements ImageOperationsService {

    private final IMOperationFactory imOperationFactory;
    private final ConvertCmdFactory convertCmdFactory;
    private final Map<String, ImageOperation> operations;

    public DefaultImageOperationsService(IMOperationFactory imOperationFactory, ConvertCmdFactory convertCmdFactory,
            Map<String, ImageOperation> operations) {
        this.imOperationFactory = imOperationFactory;
        this.convertCmdFactory = convertCmdFactory;
        this.operations = operations;
    }

    public static Map<String, ImageOperation> getOperations(Class<?> ioc) {
        final AnnotationConfigApplicationContext applicationContext = new AnnotationConfigApplicationContext(ioc);

        return applicationContext.getBeansWithAnnotation(ImageOperationName.class)
                .entrySet()
                .stream()
                .filter(entry -> entry.getValue() instanceof ImageOperation)
                .collect(
                        Collectors.toMap(
                                entry -> applicationContext.findAnnotationOnBean(entry.getKey(), ImageOperationName.class).value(),
                                entry -> (ImageOperation) entry.getValue()));
    }

    @Override
    public void applyConversion(List<List<String>> parameters, InputStream image, OutputStream out) throws ImageOperationsException,
            InterruptedException, IOException, IM4JavaException {

        IMOperation imOperation = imOperationFactory.create();
        imOperation.addImage("-");

        for (List<String> parameter : parameters) {
            if (parameter.size() != 2) {
                throw new ImageOperationsException("Bad operation: " + parameter.toString());
            }

            String requestedOperation = parameter.get(0);
            ImageOperation currentOperation = operations.get(requestedOperation);

            if (currentOperation == null) {
                throw new ImageOperationsException("Unknown operation: " + requestedOperation);
            }

            imOperation.addSubOperation(currentOperation.apply(parameter.get(1)));
        }

        imOperation.addImage("-");

        ConvertCmd convertCmd = convertCmdFactory.create(image, out);
        convertCmd.run(imOperation);
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

}
