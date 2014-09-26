package com.allan.sensordata;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Environment;
import android.provider.MediaStore;
import android.provider.Settings;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import org.apache.http.Header;
import org.apache.http.client.ResponseHandler;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.Calendar;
import java.util.TimeZone;

import utils.settings;





public class Home extends ActionBarActivity {
    final static int CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE = 1;
    Uri imageUri                      = null;
    static TextView imageDetails      = null;
    public  static ImageView showImg  = null;
    ProgressDialog pDialog;
    Home CameraActivity = null;
    Button cameraCapture, cameraUpload, upload;
    ToggleButton location, accelerometer, microphone, start;
    ImageView image;
    private LocationManager locationManager;
    private String provider;
    //private String latituteField;
    private String latitudeField;
    private String longitudeField;
    private String SENSOR_FILE_NAME;
    private static final int CAMERA_REQUEST = 1888;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        cameraCapture = (Button) findViewById(R.id.cameraCaptureButton);
        cameraUpload = (Button) findViewById(R.id.cameraUploadButton);
        upload = (Button) findViewById(R.id.uploadButton);
        location = (ToggleButton) findViewById(R.id.locationToggleButton);
        accelerometer = (ToggleButton) findViewById(R.id.accelerometerToggleButton);
        microphone = (ToggleButton) findViewById(R.id.microphoneToggleButton);
        start = (ToggleButton) findViewById(R.id.startButton);


        upload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(getApplicationContext(),"Sending to server ...",Toast.LENGTH_LONG).show();
                //code to server
                //db handlers
              // new SendData().execute();
                AsyncHttpClient client = new AsyncHttpClient();
                File myFile = new File("/storage/emulated/0/File.csv");
                RequestParams params = new RequestParams();
                try {
                    params.put("csv", myFile);
                } catch(FileNotFoundException e) {
                    e.printStackTrace();
                }

                client.post("http://gogo.brightfellas.co.ke/upload",params, new AsyncHttpResponseHandler() {
                    @Override
                    public void onSuccess(int i, Header[] headers, byte[] bytes) {
                        for (Header header : headers) {
                            Log.i("Success", header.toString());
                        }
                    }

                    @Override
                    public void onFailure(int i, Header[] headers, byte[] bytes, Throwable throwable) {

                            Log.i("Failed", throwable.getMessage());
                    }
                });
            }
        });


        imageDetails = (TextView) findViewById(R.id.imageDetails);

        showImg = (ImageView) findViewById(R.id.imageView);
        getLocation();
        location.setChecked(true);
        location.setTextOn("ON");

        accelerometer.setChecked(true);
        accelerometer.setTextOn("ON");

        microphone.setChecked(true);
        microphone.setTextOn("ON");

        location.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(location.isChecked()){

                    //LocationSensor.turnGPSOn(getApplicationContext());
                    Toast.makeText(getApplicationContext(),"Location on",Toast.LENGTH_LONG).show();
                    locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
                    Criteria criteria = new Criteria();
                    String provider = locationManager.getBestProvider(criteria, true);
                    LocationListener locationListener = new LocationListener() {
                        @Override
                        public void onLocationChanged(Location location) {
                            Toast.makeText(getApplicationContext(),"Location :"+location.getLatitude(),Toast.LENGTH_LONG).show();
                        }

                        @Override
                        public void onStatusChanged(String provider, int status, Bundle extras) {
                            Toast.makeText(getApplicationContext(), provider, Toast.LENGTH_LONG).show();
                        }

                        @Override
                        public void onProviderEnabled(String provider) {
                            Toast.makeText(getApplicationContext(), "enabled :" , Toast.LENGTH_LONG).show();
                        }

                        @Override
                        public void onProviderDisabled(String provider) {
                            Toast.makeText(getApplicationContext(), "disabled :" , Toast.LENGTH_LONG).show();
                        }
                    };
                    Location loc= locationManager.getLastKnownLocation(provider);
                    if(loc!=null) {
                        Toast.makeText(getApplicationContext(), "Location :" + loc.getLatitude(), Toast.LENGTH_LONG).show();
                    }else{
                        locationManager.requestLocationUpdates(provider, 0, 0, locationListener);
                    }
                }else if (!location.isChecked()){
                    //LocationSensor.turnGPSOff(getApplicationContext());
                    //Toast.makeText(getApplicationContext(),"Location off",Toast.LENGTH_LONG).show();
                }
            }
        });

        accelerometer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(accelerometer.isChecked()){
                    Toast.makeText(getApplicationContext(),"Accelerometer on",Toast.LENGTH_LONG).show();
                    AccelerometerSensor accelerometerSensor = new AccelerometerSensor(getApplicationContext());
                    accelerometerSensor.initSensor(getApplicationContext(),15);
                    accelerometerSensor.startSensor();
                }

                if (!accelerometer.isChecked()){
                    Toast.makeText(getApplicationContext(),"Accelerometer off",Toast.LENGTH_LONG).show();
                    AccelerometerSensor accelerometerSensor = new AccelerometerSensor(getApplicationContext());
                    //accelerometerSensor.initSensor(getApplicationContext(),15);
                    accelerometerSensor.stopSensor();
                }
            }
        });

        microphone.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(microphone.isChecked()){
                    Toast.makeText(getApplicationContext(),"microphone on",Toast.LENGTH_LONG).show();
                    MicrophoneSensor microphoneSensor = new MicrophoneSensor(getApplicationContext());
                    microphoneSensor.initSensor(getApplicationContext(),15);
                    microphoneSensor.startSensor();
                }else if(!microphone.isChecked()){
                    Toast.makeText(getApplicationContext(),"microphone off",Toast.LENGTH_LONG).show();
                    MicrophoneSensor microphoneSensor = new MicrophoneSensor(getApplicationContext());
                    //microphoneSensor.initSensor(getApplicationContext(),15);
                    microphoneSensor.stopSensor();
                }
            }
        });



        cameraUpload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {


            }
        });
        cameraCapture.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                Intent cameraIntent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
                startActivityForResult(cameraIntent, CAMERA_REQUEST);
            }
        });
        start.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(start.isChecked()){
                    if(AccelerometerSensor.hasAccData()&&MicrophoneSensor.hasData()){
                        String data;
                        String time = settings.timestampToString(
                                Calendar.getInstance(TimeZone.getDefault())
                                        .getTimeInMillis(), settings.datePattern);
                        data = AccelerometerSensor.getXData()+",";
                        data+= AccelerometerSensor.getYData()+",";
                        data+= AccelerometerSensor.getZData()+",";
                        data+= 0.0+",";
                        data+= 0.0+",";
                        data+= MicrophoneSensor.getNoiseLevel()+",";
//                        data+= AccelerometerSensor.getZData()+",";
                        data+= time+",";
                        try {
                            sendToFile(data);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }

                    }
                }
            }
        });
    }
    public  void sendToFile(String value) throws IOException {
        Boolean writeMeta = false;
        File metadata = new File("/storage/emulated/0/File.csv");
        Log.w("metadata file => ", metadata.getAbsolutePath());
        String []data = value.split(";");
        if (!metadata.exists()) {
            metadata.createNewFile();
            writeMeta = true;
        }

        FileOutputStream fOut = new FileOutputStream(metadata, true);


        OutputStreamWriter osw = new OutputStreamWriter(fOut);

        if (writeMeta) {
            osw.write(settings.getHeaders());
            osw.write(System.getProperty("line.separator"));
        }
        // Write the string to the file
        for(int i=0;i<data.length;i++){
            osw.write(data[i]);
            osw.write(";");
        }
        osw.write(System.getProperty("line.separator"));
        osw.flush();
        osw.close();
        /*
			 * ensure that everything is really written out and close
			 */

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == CAMERA_REQUEST && resultCode == RESULT_OK) {
            Bitmap photo = (Bitmap) data.getExtras().get("data");
            File file = saveImage(photo);
            Toast.makeText(getApplicationContext(),file.getAbsolutePath(),Toast.LENGTH_LONG).show();
        }
    }
    private File saveImage(Bitmap bmp) {
        String extStorageDirectory = Environment.getExternalStorageDirectory().toString();
        OutputStream outStream = null;
        // String temp = null;
        File file = new File(extStorageDirectory, "temp.png");
        if (file.exists()) {
            file.delete();
            file = new File(extStorageDirectory, "temp.png");

        }

        try {
            outStream = new FileOutputStream(file);
            bmp.compress(Bitmap.CompressFormat.PNG, 100, outStream);
            outStream.flush();
            outStream.close();

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
        return file;
    }
    /************ Convert Image Uri path to physical path **************/
    public static String convertImageUriToFile ( Uri imageUri, Activity activity )  {
        Cursor cursor = null;
        int imageID = 0;

        try {

            /*********** Which columns values want to get *******/
            String [] proj={
                    MediaStore.Images.Media.DATA,
                    MediaStore.Images.Media._ID,
                    MediaStore.Images.Thumbnails._ID,
                    MediaStore.Images.ImageColumns.ORIENTATION
            };

            cursor = activity.managedQuery(

                    imageUri,         //  Get data for specific image URI
                    proj,             //  Which columns to return
                    null,             //  WHERE clause; which rows to return (all rows)
                    null,             //  WHERE clause selection arguments (none)
                    null              //  Order-by clause (ascending by name)

            );

            //  Get Query Data
            int columnIndex = cursor.getColumnIndexOrThrow(MediaStore.Images.Media._ID);
            int columnIndexThumb = cursor.getColumnIndexOrThrow(MediaStore.Images.Thumbnails._ID);
            int file_ColumnIndex = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);

            //int orientation_ColumnIndex = cursor.
            //    getColumnIndexOrThrow(MediaStore.Images.ImageColumns.ORIENTATION);

            int size = cursor.getCount();

            /*******  If size is 0, there are no images on the SD Card. *****/

            if (size == 0) {


                imageDetails.setText("No Image");
            }
            else
            {

                int thumbID = 0;
                if (cursor.moveToFirst()) {

                    /**************** Captured image details ************/

                    /*****  Used to show image on view in LoadImagesFromSDCard class ******/
                    imageID     = cursor.getInt(columnIndex);

                    thumbID     = cursor.getInt(columnIndexThumb);

                    String Path = cursor.getString(file_ColumnIndex);

                    //String orientation =  cursor.getString(orientation_ColumnIndex);

                    String CapturedImageDetails = " CapturedImageDetails : \n\n"
                            +" ImageID :"+imageID+"\n"
                            +" ThumbID :"+thumbID+"\n"
                            +" Path :"+Path+"\n";

                    // Show Captured Image detail on activity
                    imageDetails.setText( CapturedImageDetails );

                }
            }
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }

        // Return Captured Image ImageID ( By this ImageID Image will load from sdcard )

        return ""+imageID;
    }


    /**
     * Async task for loading the images from the SD card.
     *
     * @author Android Example
     *
     */

    // Class with extends AsyncTask class

    public class LoadImagesFromSDCard  extends AsyncTask<String, Void, Void> {
        private ProgressDialog Dialog = new ProgressDialog(Home.this);
        Bitmap mBitmap;
        protected void onPreExecute() {
            /****** NOTE: You can call UI Element here. *****/
            // Progress Dialog
            Dialog.setMessage(" Loading image from Sdcard..");
            Dialog.show();
        }
        // Call after onPreExecute method
        protected Void doInBackground(String... urls) {
            Bitmap bitmap = null;
            Bitmap newBitmap = null;
            Uri uri = null;
            try {
                /**  Uri.withAppendedPath Method Description
                 * Parameters
                 *    baseUri  Uri to append path segment to
                 *    pathSegment  encoded path segment to append
                 * Returns
                 *    a new Uri based on baseUri with the given segment appended to the path
                 */

                uri = Uri.withAppendedPath(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, "" + urls[0]);

                /**************  Decode an input stream into a bitmap. *********/
                bitmap = BitmapFactory.decodeStream(getContentResolver().openInputStream(uri));

                if (bitmap != null) {

                    /********* Creates a new bitmap, scaled from an existing bitmap. ***********/

                    newBitmap = Bitmap.createScaledBitmap(bitmap, 170, 170, true);

                    bitmap.recycle();

                    if (newBitmap != null) {

                        mBitmap = newBitmap;
                    }
                }
            } catch (IOException e) {
                // Error fetching image, try to recover

                /********* Cancel execution of this task. **********/
                cancel(true);
            }

            return null;
        }


        protected void onPostExecute(Void unused) {

            // NOTE: You can call UI Element here.

            // Close progress dialog
            Dialog.dismiss();

            if(mBitmap != null)
            {
                // Set Image to ImageView
                showImg.setImageBitmap(mBitmap);
            }
        }
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.home, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }



    public void writeToFile(byte[] data, String filename) {
        FileOutputStream fos = null;
        try {
            fos = openFileOutput(filename, Context.MODE_APPEND);
            fos.write(data);
        } catch (Exception e) {
            Log.e("ERROR", e.getMessage());
        } finally {
            if (fos != null) {
                try {
                    fos.flush();
                    fos.close();
                } catch (Exception e) {
                    Log.e("ERROR", e.getMessage());
                }

            }
        }
    }

    public void getLocation(){
        LocationManager service = (LocationManager) getSystemService(LOCATION_SERVICE);
        boolean enabled = service
                .isProviderEnabled(LocationManager.GPS_PROVIDER);
        if (!enabled) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setMessage("GPS settings is OFF. Click SETTINGS to go to settings or CANCEL to continue.");
            builder.setCancelable(true);
            builder.setPositiveButton("SETTINGS", new okMessage());
            builder.setNegativeButton("CANCEL", new cancelMessage());
            AlertDialog dialog = builder.create();
            dialog.show();
        }
        /*
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        locationManager.requestLocationUpdates( LocationManager.GPS_PROVIDER,
                3000,   // 3 sec
                10, (LocationListener) this);*/
    }

    private final class okMessage implements
            DialogInterface.OnClickListener {
        public void onClick(DialogInterface dialog, int which) {
            Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
            startActivity(intent);
        }
    }

    private final class cancelMessage implements
            DialogInterface.OnClickListener {
        public void onClick(DialogInterface dialog, int which) {
            finish();
        }
    }

    public class SendData extends AsyncTask<Void,Void,Void>{
        @Override
        protected Void doInBackground(Void... params) {
            return null;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            pDialog.setMessage("Sending Data to server");
            pDialog.setCancelable(true);
            pDialog.show();
        }
    }
}
