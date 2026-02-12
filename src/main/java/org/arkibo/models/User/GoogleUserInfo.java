package org.arkibo.models.User;

public class GoogleUserInfo {

    private final String email;
    private final String name;
    private final String googleId;
    private final String pictureUrl;

    public GoogleUserInfo(String email,
                          String name,
                          String googleId,
                          String pictureUrl) {
        this.email = email;
        this.name = name;
        this.googleId = googleId;
        this.pictureUrl = pictureUrl;
    }

    public String getEmail() {
        return email;
    }

    public String getName() {
        return name;
    }

    public String getGoogleId() {
        return googleId;
    }

    public String getPictureUrl() {
        return pictureUrl;
    }
}