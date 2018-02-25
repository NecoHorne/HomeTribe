package com.necohorne.hometribe.Models;

import com.google.android.gms.maps.model.LatLng;

/**
 * Created by necoh on 2018/02/02.
 * camelCase will not be used in the naming of the fields of this class as it may lead to issues down
 * the line with the Firebase database. Use all lowercase letters, words can be separated by an underscore.
 */

public class UserProfile {

    private String user_name;
    private String user_last_name;
    private String user_email;
    private String phone_number;
    private String profile_image;
    private String user_id;
    private Home home_location;
    private LatLng user_current_location;

    //------------CONSTRUCTORS------------//
    public UserProfile(String user_id) {
        this.user_id = user_id;
    }

    public UserProfile(String user_name, String user_last_name, String user_email, String phone_number, String profile_image, String user_id, Home home_location, LatLng user_current_location) {
        this.user_name = user_name;
        this.user_last_name = user_last_name;
        this.user_email = user_email;
        this.phone_number = phone_number;
        this.profile_image = profile_image;
        this.user_id = user_id;
        this.home_location = home_location;
        this.user_current_location = user_current_location;
    }

    //------------GETTERS AND SETTERS------------//
    public String getUser_name() {
        return user_name;
    }

    public void setUser_name(String user_name) {
        this.user_name = user_name;
    }

    public String getUser_last_name() {
        return user_last_name;
    }

    public void setUser_last_name(String user_last_name) {
        this.user_last_name = user_last_name;
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

    public Home getHome_location() {
        return home_location;
    }

    public void setHome_location(Home home_location) {
        this.home_location = home_location;
    }

    public LatLng getUser_current_location() {
        return user_current_location;
    }

    public void setUser_current_location(LatLng user_current_location) {
        this.user_current_location = user_current_location;
    }
}
