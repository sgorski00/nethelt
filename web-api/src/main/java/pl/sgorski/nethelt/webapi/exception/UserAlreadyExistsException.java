package pl.sgorski.nethelt.webapi.exception;

public final class UserAlreadyExistsException extends RuntimeException {
    public UserAlreadyExistsException() {
        super("User with passed email/identifier already exists");
    }
}
