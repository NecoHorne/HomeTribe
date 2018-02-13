package com.necohorne.hometribe.Models.Incidents;

import com.google.android.gms.maps.model.LatLng;

/**
 * Created by necoh on 2018/02/02.
 */

public abstract class Crimes {

    /**
     * Types of Crimes include:
     *
     * Murder
     * Attempted Murder
     * Rape
     * Kidnapping
     * Assault
     * Sexual Assault
     *
     * House Burglary
     * House Robbery
     * Commercial Burglary
     * Commercial Robbery
     * Robbery with aggravating circumstances
     * Shoplifting
     * Pick-pocketing or bag-snatching
     *
     * Hi-jacking
     * Theft of a Motor Vehicle
     * Theft from a Motor Vehicle
     *
     * Damage to property / Arson
     *
     * Stock-theft
     * Farm Murder
     * Farm attack
     *
     * other...
     *
     */

    private String incidentType;
    private String incidentDate;
    private String incidentTime;
    private LatLng incidentLocation;
    private String incidentDescription;
    private String policeCASNumber;
    private String reportedBy;

    public Crimes(String incidentType, String incidentDate, String incidentTime, LatLng incidentLocation, String incidentDescription, String policeCASNumber, String reportedBy) {
        this.incidentType = incidentType;
        this.incidentDate = incidentDate;
        this.incidentTime = incidentTime;
        this.incidentLocation = incidentLocation;
        this.incidentDescription = incidentDescription;
        this.policeCASNumber = policeCASNumber;
        this.reportedBy = reportedBy;
    }

    public Crimes(String incidentType, String incidentDate, String incidentTime, LatLng incidentLocation, String incidentDescription, String reportedBy) {
        this.incidentType = incidentType;
        this.incidentDate = incidentDate;
        this.incidentTime = incidentTime;
        this.incidentLocation = incidentLocation;
        this.incidentDescription = incidentDescription;
        this.reportedBy = reportedBy;
    }

    public String getIncidentType() {
        return incidentType;
    }

    public void setIncidentType(String incidentType) {
        this.incidentType = incidentType;
    }

    public String getIncidentDate() {
        return incidentDate;
    }

    public void setIncidentDate(String incidentDate) {
        this.incidentDate = incidentDate;
    }

    public String getIncidentTime() {
        return incidentTime;
    }

    public void setIncidentTime(String incidentTime) {
        this.incidentTime = incidentTime;
    }

    public LatLng getIncidentLocation() {
        return incidentLocation;
    }

    public void setIncidentLocation(LatLng incidentLocation) {
        this.incidentLocation = incidentLocation;
    }

    public String getIncidentDescription() {
        return incidentDescription;
    }

    public void setIncidentDescription(String incidentDescription) {
        this.incidentDescription = incidentDescription;
    }

    public String getPoliceCASNumber() {
        return policeCASNumber;
    }

    public void setPoliceCASNumber(String policeCASNumber) {
        this.policeCASNumber = policeCASNumber;
    }

    public String getReportedBy() {
        return reportedBy;
    }

    public void setReportedBy(String reportedBy) {
        this.reportedBy = reportedBy;
    }
}
