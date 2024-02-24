package com.example.myapplication;

import android.Manifest;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingRequest;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.model.LatLng;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    TextView t1,t2;
    double lat,lon;
    FusedLocationProviderClient fusedLocationProviderClient;
    LocationRequest locationRequest;
    private static final String TAG = MainActivity.class.getSimpleName();
    private ArrayList<Geofence> mGeofenceList;
    private PendingIntent mGeofencePendingIntent;
    private Button mAddGeofencesButton,mRemoveGeofencesButton;
    LatLng latLng;
    GoogleApiClient googleApiClient=null;
     PendingIntent geofencePendingIntent;
    GeofencingRequest geofencingRequest;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        t1=findViewById(R.id.textViewLat);
        t2=findViewById(R.id.textViewLong);
        mAddGeofencesButton=findViewById(R.id.add_geofences_button);
        mRemoveGeofencesButton=findViewById(R.id.remove_geofences_button);
        fusedLocationProviderClient=new FusedLocationProviderClient(this);
        /*fusedLocationProviderClient.getLastLocation().addOnSuccessListener(new OnSuccessListener<Location>() {
            @Override
            public void onSuccess(Location location) {
                lat=location.getLatitude();
                lon=location.getLongitude();
            }
        });*/
        if(!checkPermissions()) requestPermissions();
        createGoogleApi();
        startLocationMonitoring();
        mAddGeofencesButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
               // startGeofenceMonitoring();
                Intent i=new Intent(MainActivity.this,geo.class);
                startActivity(i);
            }
        });
        mRemoveGeofencesButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                stopGeofenceMonitoring();
            }
        });
    }






    private void createGoogleApi() {
        Log.d(TAG, "createGoogleApi()");
        if ( googleApiClient == null ) {
            googleApiClient = new GoogleApiClient.Builder( this )
                   .addApi(LocationServices.API)
                    .addConnectionCallbacks(new GoogleApiClient.ConnectionCallbacks() {
                        @Override
                        public void onConnected(@Nullable Bundle bundle) {
                            Log.d(TAG,"Connected to GoogleApiClient");
                        }

                        @Override
                        public void onConnectionSuspended(int i) {
                            Log.d(TAG,"Connection suspended to GoogleApiClient");

                        }
                    })
                    .addOnConnectionFailedListener(new GoogleApiClient.OnConnectionFailedListener() {
                        @Override
                        public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
                            Log.d(TAG,"Failed to connect to GoogleApiClient");
                        }
                    })
                    .build();
        }
    }
    private void startLocationMonitoring(){
        Log.d(TAG,"startLocation called");
        try{
            LocationRequest locationRequest=LocationRequest.create()
                    .setInterval(1000)
                    .setFastestInterval(500)
                    .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
            LocationServices.FusedLocationApi.requestLocationUpdates(googleApiClient, locationRequest, new LocationListener() {
                @Override
                public void onLocationChanged(Location location) {
                    t1.setText(location.getLatitude() + "");
                    t2.setText(location.getLongitude() + "");
                    Toast.makeText(MainActivity.this,location.getLongitude()+" "+location.getLatitude(),Toast.LENGTH_SHORT).show();
                    Log.d(TAG, "location update " + location.getLatitude() + "," + location.getLongitude());
                }
            });
        }
        catch(Exception e){
            Log.d(TAG,e.getMessage());
        }
    }
    private void startGeofenceMonitoring(){
        Log.d(TAG,"startMonitoring called");
        try{
            final Geofence geofence=new Geofence.Builder()
                    .setCircularRegion(<latitude>,<longitude>,1000)
                    .setRequestId(100+"")
                    .setExpirationDuration(Geofence.NEVER_EXPIRE)
                    .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_ENTER | Geofence.GEOFENCE_TRANSITION_EXIT)
                    .build();
            GeofencingRequest geofencingRequest=new GeofencingRequest.Builder()
                    .setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_ENTER)
                    .addGeofence(geofence).build();
            Intent intent=new Intent(this,GeofenceTransitionsJobIntentService.class);
            PendingIntent pendingIntent=PendingIntent.getService(this,0,intent,PendingIntent.FLAG_UPDATE_CURRENT);
            if(!googleApiClient.isConnected()){
                Log.d(TAG,"googleapiclient isnt connected");
            }
            else{
                LocationServices.GeofencingApi.addGeofences(googleApiClient,geofencingRequest,pendingIntent)
                        .setResultCallback(new ResultCallback<Status>() {
                            @Override
                            public void onResult(@NonNull Status status) {
                                if(status.isSuccess()){
                                    Log.d(TAG,"successfully added geofence"+geofence.toString());
                                }
                                else{
                                    Log.d(TAG,"failed to add geofence");
                                }
                            }
                        });
            }
        }
        catch (Exception e){
            Log.d(TAG,e.getMessage());
        }
    }
    private void stopGeofenceMonitoring(){
        Log.d(TAG,"stopMonitoring called");
        ArrayList<String> geofenceIds=new ArrayList<>();
        geofenceIds.add(100+"");
        LocationServices.GeofencingApi.removeGeofences(googleApiClient,geofenceIds);
        Toast.makeText(this,"Geofence removed Successfully",Toast.LENGTH_SHORT).show();
    }
    @Override
    protected void onResume(){
        super.onResume();
        int response= GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(this);
        if(response!= ConnectionResult.SUCCESS){
            Log.d(TAG,"Google play services not available");
            GoogleApiAvailability.getInstance().getErrorDialog(this,response,1).show();
        }
        else{
            Log.d(TAG,"Google Play services are available");
        }
    }
    @Override
    protected void onStart() {
        super.onStart();
        googleApiClient.connect();
    }

    @Override
    protected void onStop() {
        super.onStop();
        googleApiClient.disconnect();
    }

    public boolean checkPermissions(){
        if(ActivityCompat.checkSelfPermission(MainActivity.this,Manifest.permission.ACCESS_BACKGROUND_LOCATION)==PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(MainActivity.this,Manifest.permission.ACCESS_FINE_LOCATION)==PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(MainActivity.this,Manifest.permission.ACCESS_COARSE_LOCATION)==PackageManager.PERMISSION_GRANTED){
            return true;
        }
        return false;
    }
    public void requestPermissions(){
        //ActivityCompat.requestPermissions(this,new String[]{Manifest.permission.ACCESS_BACKGROUND_LOCATION},100);
        //ActivityCompat.requestPermissions(this,new String[]{Manifest.permission.ACCESS_BACKGROUND_LOCATION,Manifest.permission.ACCESS_FINE_LOCATION,Manifest.permission.ACCESS_COARSE_LOCATION}, 123);
        ActivityCompat.requestPermissions(MainActivity.this, new String[] { Manifest.permission.ACCESS_COARSE_LOCATION,Manifest.permission.ACCESS_BACKGROUND_LOCATION,Manifest.permission.ACCESS_FINE_LOCATION },123);

    }
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults)
    {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        /*if (requestCode == 123) {
            if (grantResults.length > 0 && grantResults[1] == PackageManager.PERMISSION_GRANTED && grantResults[0]==PackageManager.PERMISSION_GRANTED && grantResults[2]==PackageManager.PERMISSION_GRANTED) {
                //getLastLocation();
                Toast.makeText(MainActivity.this, " Permission Granted", Toast.LENGTH_SHORT).show();
            }
            else{
                Toast.makeText(this,"permissions denied",Toast.LENGTH_SHORT).show();
            }
        }*/
        for(int i=0;i<permissions.length;i++){
            if(grantResults[i]==PackageManager.PERMISSION_GRANTED) continue;
           // else Toast.makeText(this,permissions[i]+" not granted",Toast.LENGTH_SHORT).show();
        }

    }

}