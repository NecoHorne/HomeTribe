package com.necohorne.hometribe.Models;

import com.google.android.gms.maps.model.LatLng;

/**
 * Created by necoh on 2018/02/20.
 * camelCase will not be used in the naming of the fields of this class as it may lead to issues down
 * the line with the Firebase database. Use all lowercase letters, words can be separated by an underscore.
 */

public class Home {

    private String country;
    private String state_province;
    private String town_city;
    private String street_address;
    private String postal_code;
    private LatLng location;

    //------------CONSTRUCTORS------------//
    public Home() {
    }

    public Home(String country, String state_province, String town_city, String street_address, String postal_code, LatLng location) {
        this.country = country;
        this.state_province = state_province;
        this.town_city = town_city;
        this.street_address = street_address;
        this.postal_code = postal_code;
        this.location = location;
    }

    //------------GETTERS AND SETTERS------------//
    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public String getState_province() {
        return state_province;
    }

    public void setState_province(String state_province) {
        this.state_province = state_province;
    }

    public String getTown_city() {
        return town_city;
    }

    public void setTown_city(String town_city) {
        this.town_city = town_city;
    }

    public String getStreet_address() {
        return street_address;
    }

    public void setStreet_address(String street_address) {
        this.street_address = street_address;
    }

    public String getPostal_code() {
        return postal_code;
    }

    public void setPostal_code(String postal_code) {
        this.postal_code = postal_code;
    }

    public LatLng getLocation() {
        return location;
    }

    public void setLocation(LatLng location) {
        this.location = location;
    }
}
