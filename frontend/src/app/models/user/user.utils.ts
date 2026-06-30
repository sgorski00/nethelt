import { IdentityProvider } from "./identity-provider"
import { DetailedUser } from "./user-response"

export function hasIdentity(
    user: DetailedUser | null,
    identityProvider: IdentityProvider
): boolean {
    return !!user?.identities?.some(i => i.provider.toLocaleLowerCase() === identityProvider);
}