package io.corbel.resources.rem.operation;

import io.corbel.resources.rem.exception.ImageOperationsException;
import org.im4java.core.IMOperation;
import org.im4java.core.IMOps;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Resize extends BaseResize {

    private final Pattern pattern = Pattern.compile("^\\((\\d+) *, *(\\d+)\\)$");

    @Override
    public IMOps apply(String parameter) throws ImageOperationsException {

        int paramWidth, paramHeight;

        try {
            Matcher matcher = pattern.matcher(parameter);

            if (!matcher.matches()) {
                throw new ImageOperationsException("Bad parameter resize: " + parameter);
            }

            List<String> values = getValues(parameter, matcher);

            paramWidth = getSafeResizeParameter(values.get(0));
            paramHeight = getSafeResizeParameter(values.get(1));
        } catch (NumberFormatException e) {
            throw new ImageOperationsException("Bad dimension parameter in resize: " + parameter, e);
        }

        if (paramWidth <= 0 || paramHeight <= 0) {
            throw new ImageOperationsException("Parameters for resize must be greater than 0: " + parameter);
        }

        return new IMOperation().resize(paramWidth, paramHeight, '!');
    }

    @Override
    public String getOperationName() {
        return "resize";
    }
}
