package io.corbel.iam.exception;

public class IllegalExpireTimeException extends Exception{

    private static final long serialVersionUID = -822589169427171311L;

    public IllegalExpireTimeException() {
        super("Authorization request is now past. Check your system clock.");
    }
}
