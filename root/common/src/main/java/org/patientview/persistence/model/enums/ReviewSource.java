package org.patientview.persistence.model.enums;

/**
 * Review Source, for product reviews
 */
public enum ReviewSource {
    FACEBOOK("Facebook"),
    IOS("ios"),
    GOOGLE_PLAY("GooglePlay");

    private String name;
    ReviewSource(String name) { this.name = name; }
    public String getName() { return this.name; }
    public String getId() { return this.name(); }
}
