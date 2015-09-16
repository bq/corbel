package io.corbel.iam.exception;

/**
 * @author Alexander De Leon
 */
public class BadSystemClockException extends Exception {

    private static final long serialVersionUID = 1L;

    public BadSystemClockException() {
        super("Authorization request is now past. Check your system clock.");
    }
}
