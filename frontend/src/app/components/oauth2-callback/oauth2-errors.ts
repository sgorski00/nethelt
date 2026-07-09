export const OAUTH2_ERRORS = {
  OAUTH2_LINK_ERROR: 'account-already-linked',
  OAUTH2_MISSING_DATA: 'oauth2-incomplete-data',
  ACCOUNT_LINK_REQUIRED: 'account-link-required',
};

export type OAuth2Error = (typeof OAUTH2_ERRORS)[keyof typeof OAUTH2_ERRORS];

export function getOAuth2ErrorMessage(error: OAuth2Error): string {
  switch (error) {
    case OAUTH2_ERRORS.OAUTH2_LINK_ERROR:
      return 'Failed to link social media account. Please try again.';
    case OAUTH2_ERRORS.OAUTH2_MISSING_DATA:
      return 'Some data is missing. Please try again.';
    case OAUTH2_ERRORS.ACCOUNT_LINK_REQUIRED:
      return "Failed to link this account. Probably it's already linked to another user.";
    default:
      return 'Something went wrong. Please try again.';
  }
}
