package com.necohorne.hometribe.Models.Incidents;

import com.google.android.gms.maps.model.LatLng;

/**
 * Created by necoh on 2018/02/02.
 */

public class Murder extends Crimes {

    public Murder(String incidentType, String incidentDate, String incidentTime, LatLng incidentLocation, String incidentDescription, String policeCASNumber, String reportedBy) {
        super( incidentType, incidentDate, incidentTime, incidentLocation, incidentDescription, policeCASNumber, reportedBy );
    }

    public Murder(String incidentType, String incidentDate, String incidentTime, LatLng incidentLocation, String incidentDescription, String reportedBy) {
        super( incidentType, incidentDate, incidentTime, incidentLocation, incidentDescription, reportedBy );
    }
}
