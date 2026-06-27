package pl.sgorski.nethelt.webapi.features.user.domain;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertSame;

public class ProfileTests {

    @Test
    void setUser_shouldMapBidirectional_whenUserProfileIsNull() {
        var profile = new Profile();
        var user = new User();

        profile.setUser(user);

        assertSame(user, profile.getUser());
        assertSame(profile, user.getProfile());
    }

    @Test
    void setUser_shouldMapBidirectional_whenUserProfileIsNotSame() {
        var oldProfile = new Profile();
        var newProfile = new Profile();
        var user = new User();
        user.setProfile(oldProfile);

        newProfile.setUser(user);

        assertSame(user, newProfile.getUser());
        assertSame(newProfile, user.getProfile());
    }
}
