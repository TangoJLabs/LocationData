package com.tangojlabs.locationdata;

import android.app.ListActivity;
import android.content.Context;
import android.graphics.Color;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Handler;
import android.os.Bundle;
import android.util.Log;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.RotateAnimation;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Calendar;

public class MainActivity extends ListActivity implements SensorEventListener, LocationListener {
    private static final String TAG = "MainActivity";

    private Context maContext = this;
    private TextView coordText;
    private TextView azimuthText;
    private ImageView compassArrow;
    private ListView dataList;
    private SensorManager allSensorManager;
    private LocationManager locationManager;

    private Sensor compassAccelerometer;
    private float[] paraLastAccelerometer = new float[3];
    private boolean checkLastAccelerometerSet = false;

    private Sensor compassMagnetometer;
    private float[] paraLastMagnetometer = new float[3];
    private boolean checkLastMagnetometerSet = false;

    private float[] paraR = new float[9];
    private float[] paraOrientation = new float[3];
    private float arrowCurrentDegree = 0f;
    private String provider;
    float lat;
    float lng;

    ArrayList<DataPoint> dataPoints = new ArrayList<DataPoint>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        allSensorManager = (SensorManager)getSystemService(SENSOR_SERVICE);
        compassAccelerometer = allSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        compassMagnetometer = allSensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);

        compassArrow = (ImageView) findViewById(R.id.pointer);
        coordText = (TextView)findViewById(R.id.coord_text);
        azimuthText = (TextView)findViewById(R.id.azimuth_text);
        dataList = (ListView)findViewById(android.R.id.list);
        coordText.setText("Location");
        azimuthText.setText("Direction");

        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        Criteria criteria = new Criteria();
        provider = locationManager.getBestProvider(criteria, false);
        Location location = locationManager.getLastKnownLocation(provider);
        if (location != null) {
            onLocationChanged(location);
        } else {
            coordText.setText("Location not available");
        }

        final Button buttonClearData = (Button)findViewById(R.id.button_clear_data);
        buttonClearData.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dataPoints.clear();
                ListViewAdapter adapter = new ListViewAdapter(maContext, dataPoints);
                ListView listView = (ListView) findViewById(android.R.id.list);
                listView.setAdapter(adapter);
            }
        });
        final Spinner intervalSpinner = (Spinner) findViewById(R.id.spinner_interval);
        ArrayAdapter<CharSequence> spinnerAdapter = ArrayAdapter
                .createFromResource(this, R.array.interval_array,
                        android.R.layout.simple_spinner_item);
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        intervalSpinner.setAdapter(spinnerAdapter);

        final Handler handler = new Handler();
        final Runnable r = new Runnable() {
            public void run() {
                addItems();
                String intervalSelection = intervalSpinner.getSelectedItem().toString();
                String intervalText = intervalSelection.substring(0, 2);
                int intervalInt = Integer.parseInt(intervalText);
                handler.postDelayed(this, intervalInt * 1000);
            }
        };
        final ToggleButton toggle = (ToggleButton) findViewById(R.id.toggle_record_data);
        toggle.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    r.run();
                    toggle.setTextColor(Color.parseColor("#FFFFFF"));
                } else {
                    handler.removeCallbacks(r);
                    toggle.setTextColor(Color.parseColor("#000000"));
                }
            }
        });
    }
    @Override
    protected void onResume() {
        super.onResume();
        allSensorManager.registerListener(this, compassAccelerometer, SensorManager.SENSOR_DELAY_GAME);
        allSensorManager.registerListener(this, compassMagnetometer, SensorManager.SENSOR_DELAY_GAME);
        locationManager.requestLocationUpdates(provider, 400, 1, (android.location.LocationListener) this);
    }

    protected void onPause() {
        super.onPause();
        allSensorManager.unregisterListener(this, compassAccelerometer);
        allSensorManager.unregisterListener(this, compassMagnetometer);
        locationManager.removeUpdates((android.location.LocationListener) this);
    }


    //Compass Methods
    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor == compassAccelerometer) {
            System.arraycopy(event.values, 0, paraLastAccelerometer, 0, event.values.length);
            checkLastAccelerometerSet = true;
        } else if (event.sensor == compassMagnetometer) {
            System.arraycopy(event.values, 0, paraLastMagnetometer, 0, event.values.length);
            checkLastMagnetometerSet = true;
        }
        if (checkLastAccelerometerSet && checkLastMagnetometerSet) {
            SensorManager.getRotationMatrix(paraR, null, paraLastAccelerometer, paraLastMagnetometer);
            SensorManager.getOrientation(paraR, paraOrientation);
            float azimuthInRadians = paraOrientation[0];
            float azimuthInRadiansRounded = round(azimuthInRadians, 2);
            float azimuthInDegreesPreRound = (float) (Math.toDegrees(azimuthInRadiansRounded)+360)%360;
            float azimuthInDegrees = (float)(1*(Math.ceil(azimuthInDegreesPreRound/1)));
            azimuthText.setText(Float.toString(azimuthInDegrees) + (char) 0x00B0);
            RotateAnimation ra = new RotateAnimation(
                    arrowCurrentDegree,
                    -azimuthInDegrees,
                    Animation.RELATIVE_TO_SELF, 0.5f,
                    Animation.RELATIVE_TO_SELF,
                    0.5f);
            ra.setDuration(250);
            ra.setFillAfter(true);
            compassArrow.startAnimation(ra);
            arrowCurrentDegree = -azimuthInDegrees;
        }
    }
    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        // TODO Auto-generated method stub
    }

    //Location Methods
    public void onLocationChanged(Location location) {
        lat = (float) (location.getLatitude());
        lng = (float) (location.getLongitude());
        coordText.setText(String.format("%.5f", lat) + ", " + String.format("%.5f", lng));
    }
    public void onStatusChanged(String provider, int status, Bundle extras) {
        // TODO Auto-generated method stub
    }
    public void onProviderEnabled(String provider) {
        Toast.makeText(this, "Enabled new provider " + provider,
                Toast.LENGTH_SHORT).show();
    }
    public void onProviderDisabled(String provider) {
        Toast.makeText(this, "Disabled provider " + provider,
                Toast.LENGTH_SHORT).show();
    }

    //Custom Methods
    public void addItems() {
        SensorManager.getRotationMatrix(paraR, null, paraLastAccelerometer, paraLastMagnetometer);
        SensorManager.getOrientation(paraR, paraOrientation);
        float azimuthInRadians = paraOrientation[0];
        float azimuthInRadiansRounded = round(azimuthInRadians, 2);
        float azimuthInDegreesPreRound = (float) (Math.toDegrees(azimuthInRadiansRounded)+360)%360;
        float azimuthInDegrees = (float)(1*(Math.ceil(azimuthInDegreesPreRound/1)));
        azimuthText.setText(Float.toString(azimuthInDegrees));

        Calendar c = Calendar.getInstance();
        int month = c.get(Calendar.MONTH);
        int day = c.get(Calendar.DAY_OF_MONTH);
        int hour = c.get(Calendar.HOUR);
        int minute = c.get(Calendar.MINUTE);
        int second = c.get(Calendar.SECOND);
        String dateTime = String.format("%02d", month)
                + "/" + String.format("%02d", day)
                + " " + String.valueOf(hour)
                + ":" + String.format("%02d", minute)
                + ":" + String.format("%02d", second);
        dataPoints.add(new DataPoint(dateTime, azimuthInDegrees, lat, lng));

        ListViewAdapter adapter = new ListViewAdapter(this, dataPoints);
        ListView listView = (ListView) findViewById(android.R.id.list);
        listView.setAdapter(adapter);
        dataList.setSelection(adapter.getCount() - 1);
    }
    public static float round(float d, int decimalPlace) {
        BigDecimal bd = new BigDecimal(Float.toString(d));
        bd = bd.setScale(decimalPlace, BigDecimal.ROUND_HALF_UP);
        return bd.floatValue();
    }

}
