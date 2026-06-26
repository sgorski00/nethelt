package pl.sgorski.nethelt.webapi.exception.domain;

public final class UserAlreadyExistsException extends RuntimeException {
    public UserAlreadyExistsException() {
        super("User with passed identifier already exists");
    }
}
