package utils;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;
import java.util.TimeZone;

/**
 * Created by Allan Gogo on 9/22/2014.
 */
public class settings {

    /**
     * Shared Preference keys
     */

    //Server url
    public static final String UPLOAD_URL = "gogo.brightfellas.co.ke/upload";
    public static final String datePattern = "yyyy-MM-dd_HH-mm-ss-SSS";
    public static String getHeaders() {
        StringBuilder mh = new StringBuilder();
        //Accelerometer Headers
        mh.append("ACCELEROMETER_X,");
        mh.append("ACCELEROMETER_Y,");
        mh.append("ACCELEROMETER_Z,");

        //longitude/latitude headers
        mh.append("Longitude,");
         mh.append("Latitude,");

        //noise header
        mh.append("Noise,");

        //time header
        mh.append("Time,");

        return mh.toString();
    }
    public static String timestampToString(long timestamp, String pattern){
        Calendar cal = Calendar.getInstance();
        cal.setTimeZone(TimeZone.getDefault());
        cal.setTimeInMillis(timestamp);
        DateFormat sdf = new SimpleDateFormat(pattern, Locale.UK);
        return sdf.format(cal.getTime());
    }

}