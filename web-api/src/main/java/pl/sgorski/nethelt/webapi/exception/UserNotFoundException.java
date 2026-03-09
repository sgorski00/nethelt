package pl.sgorski.nethelt.webapi.exception;

public final class UserNotFoundException extends NotFoundException {
    public UserNotFoundException(String message) {
        super(message);
    }
}
