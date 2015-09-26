package de.kopis.timeclicker.exceptions;

public class NotAuthenticatedException extends Exception {
    public NotAuthenticatedException() {
        super("No authentication");
    }
}
