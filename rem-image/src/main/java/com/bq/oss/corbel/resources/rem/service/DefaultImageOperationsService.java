package com.bq.oss.corbel.resources.rem.service;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.im4java.core.IM4JavaException;

import com.bq.oss.corbel.resources.rem.exception.ImageOperationsException;
import com.bq.oss.corbel.resources.rem.util.ChainedImageOperations;
import com.google.common.collect.ImmutableMap;

public class DefaultImageOperationsService implements ImageOperationsService {

    private static final Map<String, Operation> operations = new ImmutableMap.Builder<String, Operation>()
            .put("resize", DefaultImageOperationsService::resize).put("resizeWidth", DefaultImageOperationsService::resizeWidth)
            .put("resizeHeight", DefaultImageOperationsService::resizeHeight)
            .put("resizeAndFill", DefaultImageOperationsService::resizeAndFill).put("crop", DefaultImageOperationsService::crop)
            .put("cropFromCenter", DefaultImageOperationsService::cropFromCenter).build();
    private ChainedImageOperationsFactory chainedImageOperationsFactory;

    public DefaultImageOperationsService(ChainedImageOperationsFactory chainedImageOperationsFactory) {
        this.chainedImageOperationsFactory = chainedImageOperationsFactory;
    }

    private static void resize(ChainedImageOperations chainedImageOperations, String param) throws ImageOperationsException {
        try {
            Matcher matcher = Pattern.compile("^\\((\\d+) *, *(\\d+)\\)$").matcher(param);

            if (!matcher.matches()) {
                throw new ImageOperationsException("Bad parameter resize: " + param);
            }

            List<String> values = getValues(param, matcher);

            chainedImageOperations.resize(Integer.parseInt(values.get(0)), Integer.parseInt(values.get(1)));

        } catch (NumberFormatException e) {
            throw new ImageOperationsException("Bad dimension parameter in resize: " + param, e);
        }
    }

    private static void resizeWidth(ChainedImageOperations chainedImageOperations, String param) throws ImageOperationsException {
        try {
            chainedImageOperations.resize(Integer.parseInt(param), null);

        } catch (NumberFormatException e) {
            throw new ImageOperationsException("Bad image width: " + param, e);
        }
    }

    private static void resizeHeight(ChainedImageOperations chainedImageOperations, String param) throws ImageOperationsException {
        try {
            chainedImageOperations.resize(null, Integer.parseInt(param));

        } catch (NumberFormatException e) {
            throw new ImageOperationsException("Bad image height: " + param, e);
        }
    }

    private static void resizeAndFill(ChainedImageOperations chainedImageOperations, String param) throws ImageOperationsException {
        try {
            Matcher matcher = Pattern.compile("^\\((\\d+) *, *(\\w+)\\)$").matcher(param);

            if (!matcher.matches()) {
                throw new ImageOperationsException("Bad parameter resizeAndFill: " + param);
            }

            List<String> values = getValues(param, matcher);

            chainedImageOperations.resizeAndFill(Integer.parseInt(values.get(0)), values.get(1));

        } catch (NumberFormatException e) {
            throw new ImageOperationsException("Bad width parameter: " + param, e);
        }
    }

    private static void crop(ChainedImageOperations chainedImageOperations, String param) throws ImageOperationsException {
        try {
            Matcher matcher = Pattern.compile("\\((\\d+) *, *(\\d+) *, *(\\d+) *, *(\\d+)\\)").matcher(param);

            if (!matcher.matches()) {
                throw new ImageOperationsException("Bad parameter crop: " + param);
            }

            List<String> values = getValues(param, matcher);

            chainedImageOperations.crop(Integer.parseInt(values.get(0)), Integer.parseInt(values.get(1)), Integer.parseInt(values.get(2)),
                    Integer.parseInt(values.get(3)));

        } catch (NumberFormatException e) {
            throw new ImageOperationsException("Bad dimension parameter in crop operation: " + param, e);
        }
    }

    private static void cropFromCenter(ChainedImageOperations chainedImageOperations, String param) throws ImageOperationsException {
        try {

            Matcher matcher = Pattern.compile("\\((\\d+) *, *(\\d+)\\)").matcher(param);

            if (!matcher.matches()) {
                throw new ImageOperationsException("Bad parameter cropFromCenter: " + param);
            }

            List<String> values = getValues(param, matcher);

            chainedImageOperations.crop(Integer.parseInt(values.get(0)), Integer.parseInt(values.get(1)));

        } catch (NumberFormatException e) {
            throw new ImageOperationsException("Bad dimension parameter in crop from center operation: " + param, e);
        }
    }

    private static List<String> getValues(String param, Matcher matcher) {
        int groupCount = matcher.groupCount() + 1;
        List<String> valuesToReturn = new ArrayList<>(groupCount);

        for (int i = 1; i < groupCount; ++i) {
            valuesToReturn.add(param.substring(matcher.start(i), matcher.end(i)));
        }

        return valuesToReturn;
    }

    @Override
    public void applyConversion(List<List<String>> parameters, InputStream image, OutputStream out) throws ImageOperationsException,
            InterruptedException, IOException, IM4JavaException {

        ChainedImageOperations chainedImageOperations = chainedImageOperationsFactory.build();

        for (List<String> parameter : parameters) {
            if (parameter.size() != 2) {
                throw new ImageOperationsException("Bad operation: " + parameter.toString());
            }

            String requestedOperation = parameter.get(0);
            Operation currentOperation = operations.get(requestedOperation);

            if (currentOperation == null) {
                throw new ImageOperationsException("Unknown operation: " + requestedOperation);
            }

            currentOperation.apply(chainedImageOperations, parameter.get(1));
        }

        chainedImageOperations.run(image, out);
    }

    public interface Operation {
        void apply(ChainedImageOperations chainedImageOperations, String param) throws ImageOperationsException;
    }

    public static class ChainedImageOperationsFactory {
        public ChainedImageOperations build() {
            return new ChainedImageOperations();
        }
    }

}
