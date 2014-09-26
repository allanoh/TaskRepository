package com.allan.sensordata;

import android.content.Context;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.util.Log;
import android.widget.Toast;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;

import utils.settings;

/**
 * Created by allan gogo on 9/20/2014.
 */
public class MicrophoneSensor {
    Context context;
    private String TAG = "MicrophoneLifeCycleManager";
    int sampling_frequency;

    public MicrophoneSensor(Context appcontext){context = appcontext;}

    private AudioRecord audioRecordInstance = null;

    private static int[] mSampleRates = new int[] { 8000, 11025, 22050, 44100 };

    protected static final int MAXOVER_MSG = 2;
    private int BUFFSIZE = 320;
    private static final double P0 = 0.000002;
    public volatile boolean isRunning = false;

    private static final int CALIB_DEFAULT = -120;
    private int caliberationValue = CALIB_DEFAULT;

    private double maxValue = 0.0;
    public boolean showMaxValue = false;
    public boolean shouldContinue = true;

    public boolean initSensor(Context context, int samplingFrequency) {
        if(findAudioRecord() != null) return true;
        else return false;
    }

    public void startSensor() {
        shouldContinue = true;
        new Thread(workerThread).start();
    }

    public void stopSensor() {
        shouldContinue = false;
        try{
            if(audioRecordInstance != null){
                audioRecordInstance.stop();
                Thread.interrupted();
            }
        }catch(Exception e){
        }
    }

    private AudioRecord findAudioRecord() {
        for (int rate : mSampleRates) {
            for (short audioFormat : new short[] {
                    AudioFormat.ENCODING_PCM_8BIT,
                    AudioFormat.ENCODING_PCM_16BIT }) {
                for (short channelConfig : new short[] {
                        AudioFormat.CHANNEL_IN_MONO,
                        AudioFormat.CHANNEL_IN_STEREO }) {
                    try {
                        int bufferSize = AudioRecord.getMinBufferSize(rate,
                                channelConfig, audioFormat);

                        if (bufferSize != AudioRecord.ERROR_BAD_VALUE) {
                            // check if we can instantiate and have a success
                            AudioRecord recorder = new AudioRecord(
                                    MediaRecorder.AudioSource.MIC, rate,
                                    channelConfig, audioFormat, bufferSize);

                            if (recorder.getState() == AudioRecord.STATE_INITIALIZED)
                                return recorder;
                        }
                    } catch (Exception e) {
                        Log.e(TAG, rate + "Exception, keep trying.", e);
                    }
                }
            }
        }
        return null;
    }

    private String getDecibelLevel(double value){
        if(value <= 30){
            return "Low";
        }else if(value <= 65){
            return "Medium";
        }else{
            return "High";
        }
    }

    /*private void addEvent(String noiseLevel){
        JSONObject body = new JSONObject();

        body.put(MICROPHONE_EVENT_KEYS.micNoiseLevel.toString(), noiseLevel);
        body.put(MICROPHONE_EVENT_KEYS.timestamp.toString(), Utils.getCurrentTimestamp());

        if(eventHandler != null) eventHandler.onEventRecorded(MICROPHONE_EVENT_KEYS.MICROPHONE.toString(), body);
    }*/

    private Runnable workerThread = new Runnable() {
        @Override
        public void run() {
            try {

                android.os.Process
                        .setThreadPriority(android.os.Process.THREAD_PRIORITY_URGENT_AUDIO);
                System.out.println(System.currentTimeMillis());


                audioRecordInstance = findAudioRecord();// new
                // AudioRecord(MediaRecorder.AudioSource.MIC,FREQUENCY,
                // CHANNEL, ENCODING,
                // 8000);

                short[] tempBuffer = new short[BUFFSIZE];

                while (shouldContinue) {
                    //Thread.sleep(getSampling_frequency());
                    audioRecordInstance.startRecording();

                    if (audioRecordInstance.getRecordingState() == AudioRecord.RECORDSTATE_RECORDING) {

                        double splValue = 0.0;
                        double rmsValue = 0.0;

                        for (int i = 0; i < BUFFSIZE - 1; i++) {
                            tempBuffer[i] = 0;
                        }

                        audioRecordInstance.read(tempBuffer, 0, BUFFSIZE);

                        for (int i = 0; i < BUFFSIZE - 1; i++) {
                            rmsValue += tempBuffer[i] * tempBuffer[i];

                        }
                        rmsValue = rmsValue / BUFFSIZE;
                        rmsValue = Math.sqrt(rmsValue);

                        splValue = 20 * Math.log10(rmsValue / P0);
                        splValue = splValue + caliberationValue;
                        if (maxValue < splValue) {
                            maxValue = splValue;
                        }

                        String noiselvl = getDecibelLevel(splValue);
                        //sendToFile(splValue+"","Noise Level");
                        setNoiseLevel(splValue);
                        hasNoiseData = true;
                        Log.d("MICROPHONE: ", Double.toString(splValue));

                    }
                    audioRecordInstance.stop();

                    Thread.sleep(sampling_frequency);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

        }
    };
    static double currentLevel;
    public void setNoiseLevel(double lvl){
        currentLevel=lvl;
    }
    public static double getNoiseLevel(){
        return currentLevel;
    }
    static boolean hasNoiseData=false;
    public static boolean hasData(){
        return hasNoiseData;
    }
    public  void sendToFile(String value,
                            String type) throws IOException {
        Boolean writeMeta = false;
        File metadata = new File("/storage/emulated/0/File.csv");
        Log.w("metadata file => ", metadata.getAbsolutePath());

        if (!metadata.exists()) {
            metadata.createNewFile();
            writeMeta = true;
        }

        FileOutputStream fOut = new FileOutputStream(metadata, true);


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
        /*
			 * ensure that everything is really written out and close
			 */

    }
}
