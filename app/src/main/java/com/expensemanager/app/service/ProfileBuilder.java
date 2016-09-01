package com.expensemanager.app.service;

import com.expensemanager.app.models.User;

public class ProfileBuilder {
    private User user;
    private String userId;
    private byte[] profileImage;

    public ProfileBuilder() {

    }

    public User getUser() {
        return user;
    }

    public ProfileBuilder setUser(User user) {
        this.user = user;
        this.userId = user.getId();
        return this;
    }

    public byte[] getProfileImage() {
        return profileImage;
    }

    public ProfileBuilder setProfileImage(byte[] profileImage) {
        this.profileImage = profileImage;
        return this;
    }

    public String getUserId() {
        return userId;
    }
}
