package com.bq.oss.corbel.resources.rem.operation;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.im4java.core.IMOperation;
import org.im4java.core.IMOps;

import com.bq.oss.corbel.resources.rem.exception.ImageOperationsException;

public class Crop implements ImageOperation {

    private final Pattern pattern = Pattern.compile("\\((\\d+) *, *(\\d+) *, *(\\d+) *, *(\\d+)\\)");

    @Override
    public IMOps apply(String parameter) throws ImageOperationsException {

        int xorig, yorig, xdest, ydest;

        try {
            Matcher matcher = pattern.matcher(parameter);

            if (!matcher.matches()) {
                throw new ImageOperationsException("Bad parameter crop: " + parameter);
            }

            List<String> values = getValues(parameter, matcher);

            xorig = Integer.parseInt(values.get(0));
            yorig = Integer.parseInt(values.get(1));
            xdest = Integer.parseInt(values.get(2));
            ydest = Integer.parseInt(values.get(3));


        } catch (NumberFormatException e) {
            throw new ImageOperationsException("Bad dimension parameter in crop operation: " + parameter, e);
        }

        return new IMOperation().crop(xdest - xorig, ydest - yorig, xorig, yorig);

    }

    @Override
    public String getOperationName() {
        return "crop";
    }

}
