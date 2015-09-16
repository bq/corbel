package io.corbel.iam.exception;

public class BadSystemClockException extends Exception{

    private static final long serialVersionUID = -822589169427171311L;

    public BadSystemClockException() {
        super("Authorization request is now past. Check your system clock.");
    }
}
