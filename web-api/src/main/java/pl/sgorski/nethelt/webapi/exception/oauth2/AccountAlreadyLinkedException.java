package pl.sgorski.nethelt.webapi.exception.oauth2;

public final class AccountAlreadyLinkedException extends RuntimeException {

  public AccountAlreadyLinkedException() {
    super("This social media account is already linked to another user.");
  }
}
