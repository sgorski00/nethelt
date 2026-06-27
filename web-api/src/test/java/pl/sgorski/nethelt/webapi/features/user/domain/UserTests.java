package pl.sgorski.nethelt.webapi.features.user.domain;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertSame;

public class UserTests {

    @Test
    void setProfile_shouldMapBidirectional() {
        var user = new User();
        var profile = new Profile();

        user.setProfile(profile);

        assertSame(user, profile.getUser());
        assertSame(profile, user.getProfile());
    }
}
