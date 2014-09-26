package com.allan.sensordata;

import android.content.Context;
import android.content.Intent;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.widget.Toast;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.List;

import utils.settings;

import static android.content.Context.TELEPHONY_SERVICE;

/**
 * Created by allan gogo on 9/20/2014.
 */
public class LocationSensor implements LocationListener {
     double longitude;
    double latitude;
    Context context;
    public LocationSensor(Context appContext){
        context = appContext;
        LocationManager lm = (LocationManager)context.getSystemService(Context.LOCATION_SERVICE);
        Location location = lm.getLastKnownLocation(LocationManager.GPS_PROVIDER);
        //longitude = location.getLongitude();
        //latitude = location.getLatitude();
    }
    private String TAG = "GPS_LOCATION";
    private LocationManager locationManager;
    float MIN_DISTANCE_CHANGE_FOR_UPDATES = (float)10;//meters

    public boolean initSensor(){
        if (context!= null) {
            locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
            return true;
        }else return false;
    }
    public Boolean isMobileAvailable() {
        TelephonyManager tel = (TelephonyManager) context.getSystemService(TELEPHONY_SERVICE);
        return ((tel.getNetworkOperator() != null && tel.getNetworkOperator().equals("")) ? false : true);
    }

    public void startSensor(){
        /*Criteria criteria = new Criteria();
        String provider = locationManager.getBestProvider(criteria,true);*/
        Criteria criteria = new Criteria();
        String provider = locationManager.getBestProvider(criteria, true);
        locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
        Location location = locationManager.getLastKnownLocation(provider);
        if(location != null ) {
            longitude = location.getLongitude();
            latitude = location.getLatitude();

        }
        Toast.makeText(context,latitude+"",Toast.LENGTH_LONG).show();
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 2000, 10, this);
    }
    public  void sendToFile(String value,
                            String type) throws IOException {
        Boolean writeMeta = false;
        File metadata = new File(context.getFilesDir(), "File.csv");
        Log.w("metadata file => ", metadata.getAbsolutePath());
        // Toast.makeText(getActivity(), metadata.toString(),
        // Toast.LENGTH_LONG).show();
        if (!metadata.exists()) {
            metadata.createNewFile();
            writeMeta = true;
        }
        // Toast.makeText(getActivity(),"Next .....",
        // Toast.LENGTH_LONG).show();
        FileOutputStream fOut = new FileOutputStream(metadata, true);
        // Toast.makeText(getActivity(), "Continue if file exist",
        // Toast.LENGTH_LONG).show();

        OutputStreamWriter osw = new OutputStreamWriter(fOut);

        if (writeMeta) {
            osw.write(settings.getHeaders());
        }
        // Write the string to the file
        osw.write(String.valueOf(value));
        osw.write(",");
        osw.write(String.valueOf(type));
        osw.write(System.getProperty("line.separator"));
        osw.flush();
        osw.close();
        // Toast.makeText(getActivity(), "Loaded to osw",
        // Toast.LENGTH_LONG).show();
			/*
			 * ensure that everything is really written out and close
			 */

    }
    @Override
    public void onLocationChanged(Location location) {
        longitude = location.getLongitude();
        latitude = location.getLatitude();
        String message = String.format(
                "New Location \n Longitude: %1$s \n Latitude: %2$s",
                longitude, latitude);
        Log.d(TAG,message);
        String latt = latitude + "";
        try {
            sendToFile(latt, "Latitude");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void sendToCloudant(double lat,double lon, String message){
        Toast.makeText(context,message,Toast.LENGTH_LONG).show();
    }
    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {
        Log.d(TAG, "Provider status changed");
    }

    @Override
    public void onProviderEnabled(String provider) {
        Log.d(TAG, "Provider enabled by the user. GPS turned on");
    }

    @Override
    public void onProviderDisabled(String provider) {
        Log.d(TAG,"Provider disabled by the user. GPS turned off");
    }

    @SuppressWarnings("deprecation")
    public static void turnGPSOn(Context context)
    {
        Intent intent = new Intent("android.location.GPS_ENABLED_CHANGE");
        intent.putExtra("enabled", true);
        context.sendBroadcast(intent);

        String provider = Settings.Secure.getString(context.getContentResolver(), Settings.Secure.LOCATION_PROVIDERS_ALLOWED);
        if (! provider.contains("gps"))
        { //if gps is disabled
            final Intent poke = new Intent();
            poke.setClassName("com.android.settings", "com.android.settings.widget.SettingsAppWidgetProvider");
            poke.addCategory(Intent.CATEGORY_ALTERNATIVE);
            poke.setData(Uri.parse("3"));
            context.sendBroadcast(poke);
        }
    }

    @SuppressWarnings("deprecation")
    public static void turnGPSOff(Context context)
    {
        String provider = Settings.Secure.getString(context.getContentResolver(), Settings.Secure.LOCATION_PROVIDERS_ALLOWED);
        if (provider.contains("gps"))
        { //if gps is enabled
            final Intent poke = new Intent();
            poke.setClassName("com.android.settings", "com.android.settings.widget.SettingsAppWidgetProvider");
            poke.addCategory(Intent.CATEGORY_ALTERNATIVE);
            poke.setData(Uri.parse("3"));
            context.sendBroadcast(poke);
        }
    }
}
