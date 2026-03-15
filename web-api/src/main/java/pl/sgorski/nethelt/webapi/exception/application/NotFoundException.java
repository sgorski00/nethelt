package pl.sgorski.nethelt.webapi.exception.application;

public class NotFoundException extends RuntimeException {
    public NotFoundException(String message) {
        super(message);
    }
}
