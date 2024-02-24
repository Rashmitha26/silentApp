package com.example.myapplication;

import android.app.AlarmManager;
import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.util.Log;
import android.widget.Toast;
import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingEvent;
import java.util.List;

public class GeofenceTransitionsJobIntentService extends IntentService {
    private AudioManager audioManager;
    public static  final String TAG="GeofenceService";
    public GeofenceTransitionsJobIntentService(){
        super(TAG);
    }
    @Override
    protected void onHandleIntent(Intent intent){
        audioManager=(AudioManager)getSystemService(Context.AUDIO_SERVICE);
        int prev=audioManager.getRingerMode();
        //List<Geofence> obtained=intent.getArrayListExtra("geofences");
        GeofencingEvent geofencingEvent=GeofencingEvent.fromIntent(intent);
        Log.d("EVENT",geofencingEvent.getGeofenceTransition()+"");
        if(geofencingEvent.hasError()){

        }
        else{
            int transition=geofencingEvent.getGeofenceTransition();
            List<Geofence> geofences=geofencingEvent.getTriggeringGeofences();
            Log.d("LIST",geofences.toString());
            String x="";
            for(int i=0;i<geofences.size();i++) x=x+" "+geofences.get(i);
            Toast.makeText(this,x,Toast.LENGTH_SHORT).show();
            Geofence geofence=geofences.get(0);
            String reqid=geofence.getRequestId();
            if(transition==Geofence.GEOFENCE_TRANSITION_ENTER){
                audioManager.setRingerMode(1);
                Toast.makeText(this,"silent mode",Toast.LENGTH_SHORT).show();
                Log.d(TAG,"ENTER GEOFENCE "+reqid);
            }
            else if(transition==Geofence.GEOFENCE_TRANSITION_EXIT){
                audioManager.setRingerMode(AudioManager.RINGER_MODE_NORMAL);
                Log.d(TAG,"exit geofence "+reqid);
            }
        }

    }
}