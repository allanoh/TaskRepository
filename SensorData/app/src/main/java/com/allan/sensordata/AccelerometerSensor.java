package com.allan.sensordata;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;

import utils.settings;

/**
 * Created by allan gogo on 9/20/2014.
 */
public class AccelerometerSensor implements SensorEventListener{
    Context context;
    public AccelerometerSensor(Context appcontext){
        context = appcontext;
    }

    private static enum ACCELEROMETER_EVENTS {walking, stationary, loitering};

    private String TAG = "ACCELEROMETER SENSOR";


    private SensorManager sensorManager;
    private Sensor accSensor;
    private int sampling_frequency;

    private boolean shouldContinue = true;
    private volatile int accDataCounter = 0;
    private int accDataSampleSize = 400;
    private float[] accData = new float[accDataSampleSize];

    public static final int WALKING_PEAK_COUNT = 10;
    public static final int LOITERING_PEAK_COUNT = 5;

    private static final int meanMagnitudesWindowSize = 20;

    private String locomotiveState;
    public static String prevLocomotiveState = "";
    public static String currLocomotiveState = "";

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            Float x = event.values[0];
            Float y = event.values[1];
            Float z = event.values[2];
            try {
//                sendToFile(x+"","Accelerometer X");
//                sendToFile(y+"","Accelerometer Y");
//                sendToFile(z+"","Accelerometer Z");
                setXData(x);
                setYData(y);
                setZData(z);
                hasAccData = true;
            } catch (Exception e) {
                e.printStackTrace();
            }
            if (accDataCounter < accDataSampleSize) {
                accData[accDataCounter] = computeMagnitude(x.floatValue(),
                        y.floatValue(), z.floatValue());
                accDataCounter++;
            }

        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
    }

    public void startSensor() {
        shouldContinue = true;
        new Thread(recorder).start();
    }

    public void stopSensor() {
        try{
            shouldContinue = false;
            try{
                unregisterListener();
                Thread.interrupted();
            }catch(Exception e){
            }
        }catch(Exception e){
        }
    }

    private void registerListener(){
        if (accSensor != null)
            sensorManager.registerListener(this, accSensor, SensorManager.SENSOR_DELAY_FASTEST);
    }

    private void unregisterListener(){
        if(accSensor != null)sensorManager.unregisterListener(this, accSensor);
    }

    private float computeMagnitude(float x, float y, float z) {
        float[] accelerometer_values = new float[3];
        accelerometer_values[0] = x;
        accelerometer_values[1] = y;
        accelerometer_values[2] = z;

        float kFilteringFactor = 0.8f;
        float[] gravity = new float[3];
        gravity[0] = 0;
        gravity[1] = 0;
        gravity[2] = 0;

        float[] linear_acceleration = new float[3];

        gravity[0] = kFilteringFactor * gravity[0] + (1 - kFilteringFactor)
                * accelerometer_values[0];
        gravity[1] = kFilteringFactor * gravity[1] + (1 - kFilteringFactor)
                * accelerometer_values[1];
        gravity[2] = kFilteringFactor * gravity[2] + (1 - kFilteringFactor)
                * accelerometer_values[2];

        linear_acceleration[0] = (accelerometer_values[0] - gravity[0]);
        linear_acceleration[1] = (accelerometer_values[1] - gravity[1]);
        linear_acceleration[2] = (accelerometer_values[2] - gravity[2]);
//		Log.d(TAG, "x="+linear_acceleration[0]+",y="+linear_acceleration[1]+",z="+linear_acceleration[2]);

        float magnitude = 0.0f;
        magnitude = (float) Math.sqrt(linear_acceleration[0]
                * linear_acceleration[0] + linear_acceleration[1]
                * linear_acceleration[1] + linear_acceleration[2]
                * linear_acceleration[2]);
        magnitude = Math.abs(magnitude);

        return magnitude;
    }

    Runnable recorder = new Runnable() {

        @Override
        public void run() {
            while(shouldContinue){
                registerListener();

                try{
                    workingThread(sampling_frequency);
                }catch(Exception iex){
                    Log.e(TAG, "Occured when interrupting thread: " + iex.getMessage());
                    unregisterListener();
                    continue;
                }

                unregisterListener();

                locomotiveState = "Unknown";
                if (accDataCounter >= accDataSampleSize) {
                    accDataCounter = 0;
                    computeLocomotiveState();
                    Log.v(TAG, locomotiveState);



//					String accJSON = "{\"Acc\":{"+"\"timestamp\":"+"\""	+ System.currentTimeMillis() + "\",\"state\":"+"\""+locomotiveState+"\""+"}}";
                   // buildEvent(locomotiveState);
                    Log.d("ACCELEROMETER:", locomotiveState);

                    //	LogHandler.logSensedData(locomotiveState, "Acc");

                    // Comment out this line for the locomotive state code to work
//					DispatcherThread.bufferSensedData(accJSON);

                    for (int i = 0; i < accDataSampleSize; i++)
                        accData[i] = 0;
                }

                try {
                    workingThread(sampling_frequency);
                } catch (Exception e) {
                    Log.e(TAG, "Acc Thread Interrupted during off-period");
                }
            }
        }
    };

    private void workingThread(int frequency){
        try{
            Thread.sleep(frequency);
        }catch(Exception e){
            Log.e(TAG, "Occured when sleeping thread. "+e.getMessage());
        }
    }

    private static float getWindowMeanMagnitude(float[] sampleWindow) {
        float sum = 0;
        for(int i = 0; i < sampleWindow.length; i++)
            sum += sampleWindow[i];

        float magnitude = sum / sampleWindow.length;
        magnitude = Math.abs(magnitude);
        return magnitude;
    }

    private void computeLocomotiveState() {
        float meanMagnitudes[] = new float[meanMagnitudesWindowSize];
        float[] dataSamples = new float[meanMagnitudesWindowSize];
        int k = 0;
        for (int i = 0; i < accDataSampleSize;) {
            for (int j = 0; j < meanMagnitudesWindowSize; i++, j++) {
                dataSamples[j] = accData[i];
            }
            meanMagnitudes[k] = getWindowMeanMagnitude(dataSamples);
            k++;
        }

        // Count the peaks in given windowframe
        int numPeaks = 0;
        for (int i = 1; i < meanMagnitudesWindowSize; i++) {
            float slope = meanMagnitudes[i] - meanMagnitudes[i - 1];
            if (slope > 0.8 || slope < -0.8) { // look for sign changes
                numPeaks++;
            }
        }
        prevLocomotiveState = currLocomotiveState;
        if (numPeaks >= WALKING_PEAK_COUNT)
            locomotiveState = ACCELEROMETER_EVENTS.walking.toString();
        else if (numPeaks >= LOITERING_PEAK_COUNT)
            locomotiveState = ACCELEROMETER_EVENTS.loitering.toString();
        else
            locomotiveState = ACCELEROMETER_EVENTS.stationary.toString();

        currLocomotiveState = locomotiveState;
    }
    public boolean initSensor(Context context, int samplingFrequency) {
        sensorManager = (SensorManager)context.getSystemService(Context.SENSOR_SERVICE);
        accSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        if(accSensor != null) {
            return true;
        }else {
            return false;
        }
    }
    public  void sendToFile(String value,
                            String type) throws IOException {
        Boolean writeMeta = false;
        File metadata = new File("/storage/emulated/0/File.csv");
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
        osw.write(type);
        osw.write(System.getProperty("line.separator"));
        osw.flush();
        osw.close();
        // Toast.makeText(getActivity(), "Loaded to osw",
        // Toast.LENGTH_LONG).show();
			/*
			 * ensure that everything is really written out and close
			 */

    }
    static double currentX;
    static double currentY;
    static double currentZ;
    public void setXData(double x){
        currentX=x;
    }
    public static double getXData(){
        return currentX;
    }
    public void setYData(double y){
        currentY=y;
    }
    public static double getYData(){
        return currentY;
    }
    public void setZData(double z){
        currentY=z;
    }
    public static double getZData(){
        return currentZ;
    }
    static boolean hasAccData=false;
    public static boolean hasAccData(){
        return hasAccData;
    }
}
