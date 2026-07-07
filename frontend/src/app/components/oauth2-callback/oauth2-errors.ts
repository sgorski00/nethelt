export const OAUTH2_ERRORS = {
  OAUTH2_LINK_ERROR: 'account-already-linked',
  OAUTH2_MISSING_DATA: 'oauth2-incomplete-data',
  ACCOUNT_LINK_REQUIRED: 'account-link-required',
};

export type OAuth2Error = (typeof OAUTH2_ERRORS)[keyof typeof OAUTH2_ERRORS];
