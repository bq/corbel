package com.bq.oss.corbel.resources.rem.format;

import com.bq.oss.corbel.resources.rem.exception.ImageOperationsException;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

public class ImageFormat {

    private final String DEF_IMAGE_ARG = "-";
    public static final Set<String> VALID_FORMATS_LIST = new HashSet<String>(Arrays.asList("jpg", "jpeg", "tif", "tiff", "png", "gif", "bmp", "3fr", "arw", "srf", "sr2", "bay", "crw", "cr2", "cap", "tif", "iiq", "eip", "dcs", "dcr", "drf", "k25", "kdc", "dng", "erf", "fff", "mef", "mos", "mrw", "nef", "nrw", "orf", "ptx", "pef", "pxn", "R3D", "raf", "raw", "rw2", "rwl", "rwz", "x3f"));
    private Optional<String> format;

    public ImageFormat() throws ImageOperationsException {
        this(null);
    }

    public ImageFormat(String format) throws ImageOperationsException {
        this.format = Optional.ofNullable(format);
        if (!validImageFormat()) {
            throw new ImageOperationsException("Unknown image format: " + format);
        }
    }

    public String getOutputFormatParameter() {
        return format.map(formatIn -> formatIn + ":-").orElse(DEF_IMAGE_ARG);
    }

    private boolean validImageFormat() {
        return (!format.isPresent() || VALID_FORMATS_LIST.contains(format.get().toLowerCase()));
    }

    public String get() {
        return format.get();
    }

    public boolean hasFormat() {
        return this.format.isPresent();
    }
}
