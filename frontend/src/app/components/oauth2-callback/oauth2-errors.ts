export const OAUTH2_ERRORS = {
  OAUTH2_LINK_ERROR: 'oauth2-link-error',
};

export type OAuth2Error = (typeof OAUTH2_ERRORS)[keyof typeof OAUTH2_ERRORS];
