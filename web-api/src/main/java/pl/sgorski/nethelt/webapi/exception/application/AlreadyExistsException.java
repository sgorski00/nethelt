package pl.sgorski.nethelt.webapi.exception.application;

public class AlreadyExistsException extends RuntimeException {
    public AlreadyExistsException(String message) {
        super(message);
    }
}
