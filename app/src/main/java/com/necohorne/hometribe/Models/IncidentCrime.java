package com.necohorne.hometribe.Models;

import com.google.android.gms.maps.model.LatLng;

import java.util.Date;

/**
 * Created by necoh on 2018/02/02.
 * camelCase will not be used in the naming of the fields of this class as it may lead to issues down
 * the line with the Firebase database. Use all lowercase letters, words can be separated by an underscore.
 */

public class IncidentCrime {

    private String incident_type;
    private String incident_date;
    private String country;
    private String state_province;
    private String town;
    private String street_address;
    private LatLng incident_location;
    private String incident_description;
    private String police_cas_number;
    private String reported_by;

    //------------CONSTRUCTORS------------//
    public IncidentCrime() {
    }

    public IncidentCrime(String incident_type) {
        this.incident_type = incident_type;
    }

    //------------GETTERS AND SETTERS------------//
    public String getIncident_type() {
        return incident_type;
    }

    public void setIncident_type(String incident_type) {
        this.incident_type = incident_type;
    }

    public String getIncident_date() {
        return incident_date;
    }

    public void setIncident_date(String incident_date) {
        this.incident_date = incident_date;
    }

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

    public String getTown() {
        return town;
    }

    public void setTown(String town) {
        this.town = town;
    }

    public String getStreet_address() {
        return street_address;
    }

    public void setStreet_address(String street_address) {
        this.street_address = street_address;
    }

    public LatLng getIncident_location() {
        return incident_location;
    }

    public void setIncident_location(LatLng incident_location) {
        this.incident_location = incident_location;
    }

    public String getIncident_description() {
        return incident_description;
    }

    public void setIncident_description(String incident_description) {
        this.incident_description = incident_description;
    }

    public String getPolice_cas_number() {
        return police_cas_number;
    }

    public void setPolice_cas_number(String police_cas_number) {
        this.police_cas_number = police_cas_number;
    }

    public String getReported_by() {
        return reported_by;
    }

    public void setReported_by(String reported_by) {
        this.reported_by = reported_by;
    }
}
