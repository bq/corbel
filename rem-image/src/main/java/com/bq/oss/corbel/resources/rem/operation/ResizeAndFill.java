package com.bq.oss.corbel.resources.rem.operation;

import com.bq.oss.corbel.resources.rem.exception.ImageOperationsException;
import org.im4java.core.IMOperation;
import org.im4java.core.IMOps;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ResizeAndFill implements ImageOperation {

    private static final Pattern pattern = Pattern.compile("^\\((\\d+) *, *(\\w+)\\)$");

    @Override
    public IMOps apply(String parameter) throws ImageOperationsException {

        int width;
        String color;

        try {
            Matcher matcher = pattern.matcher(parameter);

            if (!matcher.matches()) {
                throw new ImageOperationsException("Bad parameter resizeAndFill: " + parameter);
            }

            List<String> values = getValues(parameter, matcher);

            width = Integer.parseInt(values.get(0));
            color = values.get(1);

        } catch (NumberFormatException e) {
            throw new ImageOperationsException("Bad width parameter: " + parameter, e);
        }

        IMOperation subOperation = new IMOperation();
        subOperation.resize(width, width);
        subOperation.background(color);
        subOperation.gravity("center");
        subOperation.extent(width, width);

        return subOperation;
    }

    @Override
    public String getOperationName() {
        return "resizeAndFill";
    }

    @Override
    public boolean isRealOperation() {
        return true;
    }
}
