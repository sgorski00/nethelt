package pl.sgorski.nethelt.webapi.features.auth.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pl.sgorski.nethelt.webapi.exception.application.NotFoundException;
import pl.sgorski.nethelt.webapi.features.auth.config.RefreshTokenProperties;
import pl.sgorski.nethelt.webapi.features.auth.domain.RefreshToken;
import pl.sgorski.nethelt.webapi.features.auth.repository.RefreshTokenRepository;
import pl.sgorski.nethelt.webapi.features.user.domain.User;

import java.time.Instant;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class RefreshTokenService {

    private final RefreshTokenProperties refreshTokenProperties;
    private final RefreshTokenRepository refreshTokenRepository;

    @Transactional
    public RefreshToken generateRefreshToken(User user) {
        var tokenStr = UUID.randomUUID().toString();
        var token = new RefreshToken(tokenStr, user, refreshTokenProperties.expirationTimeInMs());
        return refreshTokenRepository.save(token);
    }

    /**
     * Validates the refresh token, throws exception if invalid.
     *
     * @param tokenStr the token string
     * @param user     the user
     * @throws NotFoundException if token not found or invalid
     */
    public void validateToken(String tokenStr, User user) {
        if (!isTokenValid(tokenStr, user)) {
            throw new NotFoundException("Invalid or expired refresh token");
        }
    }

    @Transactional
    public void revokeToken(String tokenStr) {
        refreshTokenRepository.findByToken(tokenStr).ifPresent(t -> {
            t.setRevoked(true);
            refreshTokenRepository.save(t);
        });
    }

    public long getExpirationSecond() {
        return refreshTokenProperties.expirationTimeInMs() / 1000;
    }


    private boolean isTokenValid(String tokenStr, User user) {
        var token = refreshTokenRepository.findByToken(tokenStr)
                .orElseThrow(() -> new NotFoundException("Token not found"));
        return !token.isRevoked() &&
                token.getExpiresAt().isAfter(Instant.now()) &&
                token.getUser().getId().equals(user.getId());
    }

    @Transactional
    public void deletedInvalidTokens() {
        refreshTokenRepository.deleteAllByExpiresAtBeforeOrIsRevokedTrue(Instant.now());
    }
}
