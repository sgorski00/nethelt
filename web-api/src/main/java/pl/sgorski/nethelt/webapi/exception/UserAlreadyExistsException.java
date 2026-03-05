package pl.sgorski.nethelt.webapi.exception;

public class UserAlreadyExistsException extends RuntimeException {
    public UserAlreadyExistsException() {
        super("User with passed email/username already exists");
    }
}
