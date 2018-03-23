package com.necohorne.hometribe.Activities.AppActivities;

import android.content.SharedPreferences;
import android.graphics.Color;
import android.location.Address;
import android.location.Geocoder;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.google.android.gms.maps.model.LatLng;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.gson.Gson;
import com.necohorne.hometribe.Constants.Constants;
import com.necohorne.hometribe.Models.Home;
import com.necohorne.hometribe.Models.IncidentCrime;
import com.necohorne.hometribe.R;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import static com.facebook.FacebookSdk.getApplicationContext;
import static com.google.maps.android.SphericalUtil.computeDistanceBetween;

public class HomeStatsActivity extends AppCompatActivity {

    private static final String TAG = "HomeStatsActivity";

    private SharedPreferences mHomePrefs;
    private boolean prefBool;
    private Home mHome;

    private TextView townName;
    private PieChart mPieChart;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate( savedInstanceState );
        setContentView( R.layout.activity_home_stats );
        mHomePrefs = getSharedPreferences(Constants.PREFS_HOME, 0);
        prefBool = mHomePrefs.contains(Constants.HOME);

        townName = (TextView) findViewById(R.id.stats_town_textview);
        mPieChart = (PieChart) findViewById(R.id.stats_pie_chart);

        if (prefBool){
            Gson gson = new Gson();
            String json = mHomePrefs.getString(Constants.HOME, "" );
            mHome = gson.fromJson(json, Home.class);
            townName.setText(mHome.getTown_city());
            getIncidentLocations();
        }
    }

    public void getIncidentLocations(){
        final ArrayList<IncidentCrime> crimeArrayList = new ArrayList<>();
        if (crimeArrayList.size() > 0){
            crimeArrayList.clear();
        }
       DatabaseReference mDatabaseReference = FirebaseDatabase.getInstance().getReference();
        Query query = mDatabaseReference.child(getString(R.string.dbnode_incidents));

        mHomePrefs = getSharedPreferences( Constants.PREFS_HOME, 0);
        prefBool = mHomePrefs.contains(Constants.HOME);

        if (prefBool){
            query.addListenerForSingleValueEvent( new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    for (DataSnapshot singleSnapshot: dataSnapshot.getChildren()){
                        IncidentCrime incident = new IncidentCrime();
                        Map<String, Object> objectMap = (HashMap<String, Object>) singleSnapshot.getValue();

                        String sLocation = (objectMap.get(getString(R.string.field_incident_location)).toString());

                        if (checkTown(getLocation(sLocation))){
                            incident.setIncident_type(objectMap.get(getString(R.string.field_incident_type)).toString());
//                            incident.setIncident_location(getLocation(sLocation));
//                            String date = objectMap.get(getString( R.string.field_incident_date)).toString();
//                            incident.setIncident_date(date);
//                            incident.setCountry(objectMap.get(getString(R.string.field_incident_type)).toString());
//                            incident.setState_province(objectMap.get(getString(R.string.field_state_province)).toString());
//                            incident.setTown(objectMap.get(getString(R.string.field_town)).toString());
//                            incident.setStreet_address(objectMap.get(getString(R.string.field_street_address)).toString());
//                            incident.setIncident_description(objectMap.get(getString(R.string.field_incident_description)).toString());
//                            incident.setPolice_cas_number(objectMap.get(getString(R.string.field_police_cas_number)).toString());
//                            incident.setReported_by(objectMap.get(getString(R.string.field_reported_by)).toString());
//                            incident.setReference(singleSnapshot.getKey());

                            crimeArrayList.add(incident);
                        }
                    }
                    sortIncidentTypes(crimeArrayList);
                    Log.d( TAG, "Array size: " + crimeArrayList.size());
                }
                @Override
                public void onCancelled(DatabaseError databaseError) {
                    Toast.makeText( HomeStatsActivity.this, "Database Error, please try again later", Toast.LENGTH_SHORT ).show();
                }
            } );
        }
    }

    private LatLng getLocation(String location){
        String regex = "\\blongitude=\\b";
        String str1 = location.replaceAll( "[{]", "" );
        String str2 = str1.substring(9);
        String str3 = str2.replaceAll( "[}]", "" );
        String str4 = str3.replaceAll( regex, "" );
        String[] latlong =  str4.split(",");
        double latitude = Double.parseDouble(latlong[0]);
        double longitude = Double.parseDouble(latlong[1]);
        return new LatLng(latitude, longitude);
    }

    private double checkDistance(IncidentCrime incident) {
        double distance = 0;
        if (prefBool){
            if (incident.getIncident_location() != null & mHome.getLocation() != null){
                distance = computeDistanceBetween(incident.getIncident_location(), mHome.getLocation());
            }
        }

        return distance / 1000;
    }

    private boolean checkTown(LatLng latLng){

        Geocoder geocoder = new Geocoder( getApplicationContext(), Locale.getDefault() );
        List<Address> addresses;
        try {
            addresses = geocoder.getFromLocation(
                    latLng.latitude,
                    latLng.longitude,
                    1 );
            if (addresses.size() > 0) {
                if (addresses.get( 0 ).getSubLocality() != null) {
                    String town = addresses.get( 0 ).getSubLocality();
                    if (mHome.getTown_city().equals(town)){
                        return true;
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return false;
    }

    private void sortIncidentTypes(ArrayList<IncidentCrime> crimeArrayList){

        float houseBurglary = 0.0f;
        float houseRobbery = 0.0f;
        float theftOfaMotorVehicle = 0.0f;
        float theftFromaMotorVehicle = 0.0f;
        float hijacking = 0.0f;
        float damageToProperty = 0.0f;
        float pickPocketing = 0.0f;
        float armedRobbery = 0.0f;
        float robberyWithAggravating = 0.0f;
        float commercialBurglary = 0.0f;
        float shoplifting = 0.0f;
        float commercialRobbery = 0.0f;
        float dogPoisoning = 0.0f;
        float stockTheft = 0.0f;
        float farmMurder = 0.0f;
        float farmAttack = 0.0f;
        float murder = 0.0f;
        float attemptedMurder = 0.0f;
        float rape = 0.0f;
        float kidnapping = 0.0f;
        float assault = 0.0f;
        float sexualAssault = 0.0f;
        float other = 0.0f;

        for (int i = 0; i < crimeArrayList.size(); i++ ){
            String type = crimeArrayList.get(i).getIncident_type();
            switch (type){
                case "House Burglary":
                    houseBurglary++;
                    break;
                case "House Robbery":
                    houseRobbery++;
                    break;
                case "Theft of a Motor Vehicle":
                    theftOfaMotorVehicle++;
                    break;
                case "Theft from a Motor Vehicle":
                    theftFromaMotorVehicle++;
                    break;
                case "Hi-jacking":
                    hijacking++;
                    break;
                case "Damage to property / Arson":
                    damageToProperty++;
                    break;
                case "Pick-pocketing or bag-snatching":
                    pickPocketing++;
                    break;
                case "Armed Robbery":
                    armedRobbery++;
                    break;
                case "Robbery with aggravating circumstances":
                    robberyWithAggravating++;
                    break;
                case "Commercial Burglary":
                    commercialBurglary++;
                    break;
                case "Shoplifting":
                    shoplifting++;
                    break;
                case "Commercial Robbery":
                    commercialRobbery++;
                    break;
                case "Dog Poisoning":
                    dogPoisoning++;
                    break;
                case "Stock-theft":
                    stockTheft++;
                    break;
                case "Farm Murder":
                    farmMurder++;
                    break;
                case "Farm attack":
                    farmAttack++;
                    break;
                case "Murder":
                    murder++;
                    break;
                case "Attempted Murder":
                    attemptedMurder++;
                    break;
                case "Rape":
                    rape++;
                    break;
                case "Kidnapping":
                    kidnapping++;
                    break;
                case "Assault":
                    assault++;
                    break;
                case "Sexual Assault":
                    sexualAssault++;
                    break;
                case "other…":
                    other++;
                    break;
            }

        }

        List<PieEntry> entries = new ArrayList<>();
        if (houseBurglary > 0)
        entries.add(new PieEntry(houseBurglary, "House Burglary"));

        if (houseRobbery > 0)
        entries.add(new PieEntry(houseRobbery, "House Robbery"));

        if (theftOfaMotorVehicle > 0)
        entries.add(new PieEntry(theftOfaMotorVehicle, "Theft of a Motor Vehicle"));

        if (theftFromaMotorVehicle > 0)
        entries.add(new PieEntry(theftFromaMotorVehicle, "Theft from a Motor Vehicle"));

        if ( hijacking > 0)
        entries.add(new PieEntry(hijacking, "Hi-jacking"));

        if (damageToProperty > 0)
        entries.add(new PieEntry(damageToProperty, "Damage to property / Arson"));

        if (pickPocketing > 0)
        entries.add(new PieEntry(pickPocketing, "Pick-pocketing or bag-snatching"));

        if (armedRobbery > 0)
        entries.add(new PieEntry(armedRobbery, "Armed Robbery"));

        if ( robberyWithAggravating > 0)
        entries.add(new PieEntry(robberyWithAggravating, "Robbery with aggravating circumstances"));

        if (commercialBurglary > 0)
        entries.add(new PieEntry(commercialBurglary, "Commercial Burglary"));

        if (shoplifting > 0)
        entries.add(new PieEntry(shoplifting, "Shoplifting"));

        if (commercialRobbery > 0)
        entries.add(new PieEntry(commercialRobbery, "Commercial Robbery"));

        if (dogPoisoning > 0)
        entries.add(new PieEntry(dogPoisoning, "Dog Poisoning"));

        if (stockTheft > 0)
        entries.add(new PieEntry(stockTheft, "Stock-theft"));

        if (farmMurder > 0)
        entries.add(new PieEntry(farmMurder, "Farm Murder"));

        if (farmAttack > 0)
        entries.add(new PieEntry(farmAttack, "Farm attack"));

        if (murder > 0)
        entries.add(new PieEntry(murder, "Murder"));

        if (attemptedMurder > 0)
        entries.add(new PieEntry(attemptedMurder, "Attempted Murder"));

        if (rape > 0)
        entries.add(new PieEntry(rape, "Rape"));

        if (kidnapping > 0)
        entries.add(new PieEntry(kidnapping, "Kidnapping"));

        if (assault > 0)
        entries.add(new PieEntry(assault, "Assault"));

        if (sexualAssault > 0)
        entries.add(new PieEntry(sexualAssault, "Sexual Assault"));

        if (other > 0)
        entries.add(new PieEntry(other, "other…"));

        int[] color = new int[]{getResources().getColor(R.color.Red)
        ,getResources().getColor(R.color.Green)
        ,getResources().getColor(R.color.Yellow)
        ,getResources().getColor(R.color.Blue)
        ,getResources().getColor(R.color.Orange)
        ,getResources().getColor(R.color.Purple)
        ,getResources().getColor(R.color.Cyan)
        ,getResources().getColor(R.color.Magenta)
        ,getResources().getColor(R.color.Lime)
//        ,getResources().getColor(R.color.Pink)
//        ,getResources().getColor(R.color.Teal)
//        ,getResources().getColor(R.color.Lavender)
//        ,getResources().getColor(R.color.Brown)
//        ,getResources().getColor(R.color.Beige)
//        ,getResources().getColor(R.color.Maroon)
//        ,getResources().getColor(R.color.Mint)
//        ,getResources().getColor(R.color.Olive)
//        ,getResources().getColor(R.color.Coral)
//        ,getResources().getColor(R.color.Navy)
//        ,getResources().getColor(R.color.Grey)
//        ,getResources().getColor(R.color.Black)
//        ,getResources().getColor(R.color.White)
//        ,getResources().getColor(R.color.greenPrimLight)
//        ,getResources().getColor(R.color.color_e)
//        ,getResources().getColor(R.color.color2)
            };

        PieDataSet set = new PieDataSet(entries, "Incidents in my Area");
        set.setColors(color);
        set.setValueTextColor(getResources().getColor(R.color.Black));
        set.setValueTextSize(14f);
        PieData data = new PieData(set);
        mPieChart.setData(data);
        mPieChart.setCenterText("Incidents in my Area");
    }

}
