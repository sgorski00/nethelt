package pl.sgorski.nethelt.webapi.exception.domain;

public final class AccountLinkRequiredException extends RuntimeException {

    public AccountLinkRequiredException() {
        super("An account with the same email already exists. Please link your account to the existing account in the profile.");
    }
}
