package com.tangojlabs.locationdata;

/**
 * Created by Sean on 10/4/2015.
 */
import java.util.ArrayList;

public class DataPoint {

    public String dateTime;
    public Float azimuth;
    public Float lat;
    public Float lng;

    public DataPoint(String dateTime, Float azimuth, Float lat, Float lng) {
        this.dateTime = dateTime;
        this.azimuth = azimuth;
        this.lat = lat;
        this.lng = lng;
    }

    public static ArrayList<DataPoint> getDataPoints() {
        ArrayList<DataPoint> dataPoints = new ArrayList<DataPoint>();
        dataPoints.add(new DataPoint("date time", 0.0f, 0.0f, 0.0f));
        return dataPoints;
    }
}
