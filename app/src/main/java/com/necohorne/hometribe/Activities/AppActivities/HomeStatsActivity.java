package com.necohorne.hometribe.Activities.AppActivities;

import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.location.Address;
import android.location.Geocoder;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.Description;
import com.github.mikephil.charting.components.Legend;
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
import com.necohorne.hometribe.Utilities.MyDialogCloseListener;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static com.google.maps.android.SphericalUtil.computeDistanceBetween;

public class HomeStatsActivity extends AppCompatActivity implements MyDialogCloseListener {

    private static final String TAG = "HomeStatsActivity";

    private SharedPreferences mHomePrefs;
    private boolean prefBool;
    private Home mHome;

    private TextView townName;
    private PieChart mPieChart;
    private String mTimePrefs;
    private SharedPreferences time;
    private SharedPreferences distance;
    private String mDistancePrefs;
    private Date mToday;
    private StatsFilterActivity mFilterActivity;
    private ProgressBar mProgressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate( savedInstanceState );
        setContentView( R.layout.activity_home_stats );

        setupPrefs();

        townName = (TextView) findViewById(R.id.stats_town_textview);
        mPieChart = (PieChart) findViewById(R.id.stats_pie_chart);
        mProgressBar = (ProgressBar) findViewById(R.id.stats_progress_Bar);
        mProgressBar.setVisibility(View.GONE);
        mProgressBar.setIndeterminate(true);

        today();

        if (prefBool){
            getIncidentLocations();
        }

        if(mDistancePrefs.equals(Constants.MY_TOWN)){
            townName.setText(mHome.getTown_city());
        }else {
            townName.setText(mDistancePrefs + " Km from Home");
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate( R.menu.home_stats_menu, menu );
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch (id) {
            case R.id.home_stats_menu_filter:
                mFilterActivity = new StatsFilterActivity();
                mFilterActivity.show( getFragmentManager(), "activity_stats_filter" );
                break;
        }
        return super.onOptionsItemSelected( item );
    }

    public void setupPrefs(){
        mHomePrefs = getSharedPreferences(Constants.PREFS_HOME, 0);
        prefBool = mHomePrefs.contains(Constants.HOME);

        time = getSharedPreferences(Constants.PREFS_TIME, 0 );
        mTimePrefs = time.getString(Constants.TIME, "all");
        distance = getSharedPreferences(Constants.PREFS_DISTANCE, 0 );
        mDistancePrefs = distance.getString(Constants.DISTANCE, "my_town");

        if (prefBool){
            Gson gson = new Gson();
            String json = mHomePrefs.getString(Constants.HOME, "" );
            mHome = gson.fromJson(json, Home.class);
        }
    }

    public void getIncidentLocations(){

        showProgress();

        final ArrayList<IncidentCrime> crimeArrayList = new ArrayList<>();
        if (crimeArrayList.size() > 0){
            crimeArrayList.clear();
        }
       DatabaseReference mDatabaseReference = FirebaseDatabase.getInstance().getReference();
        Query query = mDatabaseReference.child(getString(R.string.dbnode_incidents));

        if (prefBool){
            query.addListenerForSingleValueEvent( new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    for (DataSnapshot singleSnapshot: dataSnapshot.getChildren()){
                        IncidentCrime incident = new IncidentCrime();

                        Map<String, Object> objectMap = (HashMap<String, Object>) singleSnapshot.getValue();

                        switch (mDistancePrefs){
                            case Constants.FIVE_KILOMETERS:
                                calculateByDistancePrefs(objectMap, crimeArrayList, incident, mTimePrefs);
                                break;
                            case Constants.TEN_KILOMETERS:
                                calculateByDistancePrefs(objectMap, crimeArrayList, incident, mTimePrefs);
                                break;
                            case Constants.MY_TOWN:
                                calculateStatisticsMyTown(objectMap, crimeArrayList, incident, mTimePrefs);
                                break;
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

    private void calculateStatisticsMyTown(Map<String, Object> objectMap, ArrayList<IncidentCrime> crimeArrayList, IncidentCrime incident, String timePrefs) {

        String sLocation = (objectMap.get(getString(R.string.field_incident_location)).toString());
        String date = objectMap.get(getString( R.string.field_incident_date)).toString();
        incident.setIncident_date(date);
        long days = getDateDiff(convertDate(incident).getTime(), mToday, TimeUnit.DAYS);


        if (checkTown(getLocation(sLocation))){
            if (timePrefs.equals(Constants.ALL_TIME)){
                incident.setIncident_type(objectMap.get(getString(R.string.field_incident_type)).toString());
                crimeArrayList.add(incident);
            }else if (days <= Integer.parseInt(mTimePrefs )){
                incident.setIncident_type(objectMap.get(getString(R.string.field_incident_type)).toString());
                crimeArrayList.add(incident);
            }
        }
    }

    private void calculateByDistancePrefs(Map<String, Object> objectMap, ArrayList<IncidentCrime> crimeArrayList, IncidentCrime incident, String timePrefs) {
        String sLocation = (objectMap.get(getString(R.string.field_incident_location)).toString());
        String date = objectMap.get(getString( R.string.field_incident_date)).toString();
        incident.setIncident_date(date);
        long days = getDateDiff(convertDate(incident).getTime(), mToday, TimeUnit.DAYS);


        if (checkDistance(getLocation(sLocation)) <= Integer.parseInt(mDistancePrefs)){
            if (timePrefs.equals(Constants.ALL_TIME)){
                incident.setIncident_type(objectMap.get(getString(R.string.field_incident_type)).toString());
                crimeArrayList.add(incident);
            }else if (days <= Integer.parseInt(mTimePrefs )){
                incident.setIncident_type(objectMap.get(getString(R.string.field_incident_type)).toString());
                crimeArrayList.add(incident);
            }
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

    private double checkDistance(LatLng latLng) {
        double distance = 0;
        if (prefBool){
            if (latLng != null & mHome.getLocation() != null){
                distance = computeDistanceBetween(latLng, mHome.getLocation());
            }
        }
        return distance / 1000;
    }

    private Calendar convertDate(IncidentCrime incident) {
        Calendar cal = Calendar.getInstance();
        SimpleDateFormat sdf = new SimpleDateFormat("EEE MMM dd HH:mm:ss Z yyyy", Locale.ENGLISH);
        try {
            cal.setTime(sdf.parse(incident.getIncident_date()));
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return cal;
    }

    public static long getDateDiff(Date date1, Date date2, TimeUnit timeUnit) {
        long diffInMillies = date2.getTime() - date1.getTime();
        return timeUnit.convert(diffInMillies,TimeUnit.MILLISECONDS);
    }

    public void today(){
        Calendar calendar = Calendar.getInstance();
        mToday = calendar.getTime();
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
        float theftFromMotorVehicle = 0.0f;
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
                    theftFromMotorVehicle++;
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

        if (theftFromMotorVehicle > 0)
        entries.add(new PieEntry(theftFromMotorVehicle, "Theft from a Motor Vehicle"));

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
        ,getResources().getColor(R.color.Lime)};

        if (crimeArrayList.size() % color.length != 0){
            color = new int[]{getResources().getColor( R.color.Red )
                    , getResources().getColor( R.color.Green )
                    , getResources().getColor( R.color.Yellow )
                    , getResources().getColor( R.color.Blue )
                    , getResources().getColor( R.color.Orange )
                    , getResources().getColor( R.color.Purple )
                    , getResources().getColor( R.color.Cyan )
                    , getResources().getColor( R.color.Magenta )
                    , getResources().getColor( R.color.Lime )
                    ,getResources().getColor( R.color.Pink)};
        }

        if (crimeArrayList.size() > 0){
            PieDataSet set = new PieDataSet(entries, "Incidents in my Area");
            set.setColors(color);
            set.setValueTextColor(getResources().getColor(R.color.Black));
            set.setValueTextSize(14f);
            PieData data = new PieData(set);
            mPieChart.setData(data);
            Legend legend = mPieChart.getLegend();
            legend.setEnabled(false);
            Description description = new Description();
            description.setText( "Crime in my Area" );
            mPieChart.setDescription(description);
            mPieChart.invalidate();
            hideProgress();
        }else {
            hideProgress();
        }
    }

    @Override
    public void handleDialogClose(DialogInterface dialog) {
        mTimePrefs = time.getString(Constants.TIME, "all");
        mDistancePrefs = distance.getString(Constants.DISTANCE, "my_town");
        getIncidentLocations();
        if(mDistancePrefs.equals(Constants.MY_TOWN)){
            townName.setText(mHome.getTown_city());
        }else {
            townName.setText(mDistancePrefs + " Km from Home");
        }

    }

    @Override
    public void onPointerCaptureChanged(boolean hasCapture) {

    }

    private void showProgress(){
        mProgressBar.setVisibility(View.VISIBLE);
        mPieChart.setVisibility(View.INVISIBLE);
    }

    private void hideProgress(){
        mProgressBar.setVisibility(View.GONE);
        mPieChart.setVisibility(View.VISIBLE);
    }
}
