package com.expensemanager.app.profile;

import com.expensemanager.app.models.User;

public class ProfileBuilder {
    private User user;
    private byte[] profileImage;

    public ProfileBuilder() {

    }

    public User getUser() {
        return user;
    }

    public ProfileBuilder setUser(User user) {
        this.user = user;
        return this;
    }

    public byte[] getProfileImage() {
        return profileImage;
    }

    public ProfileBuilder setProfileImage(byte[] profileImage) {
        this.profileImage = profileImage;
        return this;
    }
}
