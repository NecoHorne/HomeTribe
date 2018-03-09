package com.necohorne.hometribe.Models;

import com.google.android.gms.maps.model.LatLng;

/**
 * Created by necoh on 2018/02/02.
 * camelCase will not be used in the naming of the fields of this class as it may lead to issues down
 * the line with the Firebase database. Use all lowercase letters, words can be separated by an underscore.
 */

public class UserProfile {

    private String user_name;
    private String user_email;
    private String phone_number;
    private String profile_image;
    private String user_id;
    private LatLng home_location;
    private String bio;
    private String website;
    private String fcm_token;

    //------------CONSTRUCTORS------------//

    public UserProfile() {
    }

    public UserProfile(String user_id) {
        this.user_id = user_id;
    }

    public UserProfile(String user_name, String user_email, String phone_number, String profile_image, String user_id, LatLng home_location, String bio, String website, String fcm_token) {
        this.user_name = user_name;
        this.user_email = user_email;
        this.phone_number = phone_number;
        this.profile_image = profile_image;
        this.user_id = user_id;
        this.home_location = home_location;
        this.bio = bio;
        this.website = website;
        this.fcm_token = fcm_token;
    }

    //------------GETTERS AND SETTERS------------//
    public String getUser_name() {
        return user_name;
    }

    public void setUser_name(String user_name) {
        this.user_name = user_name;
    }

    public String getUser_email() {
        return user_email;
    }

    public void setUser_email(String user_email) {
        this.user_email = user_email;
    }

    public String getPhone_number() {
        return phone_number;
    }

    public void setPhone_number(String phone_number) {
        this.phone_number = phone_number;
    }

    public String getProfile_image() {
        return profile_image;
    }

    public void setProfile_image(String profile_image) {
        this.profile_image = profile_image;
    }

    public String getUser_id() {
        return user_id;
    }

    public void setUser_id(String user_id) {
        this.user_id = user_id;
    }

    public LatLng getHome_location() {
        return home_location;
    }

    public void setHome_location(LatLng home_location) {
        this.home_location = home_location;
    }

    public String getBio() {
        return bio;
    }

    public void setBio(String bio) {
        this.bio = bio;
    }

    public String getWebsite() {
        return website;
    }

    public void setWebsite(String website) {
        this.website = website;
    }

    public String getFcm_token() {
        return fcm_token;
    }

    public void setFcm_token(String fcm_token) {
        this.fcm_token = fcm_token;
    }

    @Override
    public String toString() {
        return "UserProfile{" +
                "user_name='" + user_name + '\'' +
                ", user_email='" + user_email + '\'' +
                ", phone_number='" + phone_number + '\'' +
                ", profile_image='" + profile_image + '\'' +
                ", user_id='" + user_id + '\'' +
                ", home_location=" + home_location +
                ", bio='" + bio + '\'' +
                ", website='" + website + '\'' +
                ", fcm_token='" + fcm_token + '\'' +
                '}';
    }
}
