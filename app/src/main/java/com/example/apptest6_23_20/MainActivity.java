package com.example.apptest6_23_20;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.widget.TextView;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import com.google.gson.Gson;
import java.sql.Timestamp;
import java.util.Date;
import java.util.HashMap;
import java.util.UUID;

public class MainActivity extends AppCompatActivity implements SensorEventListener, LocationListener, Runnable {

    SensorManager sensorManager;
    Sensor accelerometer;

    LocationManager locationManager;
    LocationListener locationListener;

    private static final String TAG = "MainActivity";

    protected Context context;
    TextView txtLat;
    String lat;
    String provider;
    double latitude, longitude;
    protected boolean gps_enabled, network_enabled;

    double xAccel;
    double yAccel;
    double zAccel;

    Button mButton;
    EditText mEdit;
    TextView mText;

    double minLat;
    double maxLat;
    double minLongi;
    double maxLongi;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Date date = new Date();
        long timeMilli = date.getTime();
        System.out.println("Time" + timeMilli);
        //create sensors and register them
        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);

        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        sensorManager.registerListener((SensorEventListener) MainActivity.this, accelerometer, SensorManager.SENSOR_DELAY_NORMAL);

        Log.d(TAG, "onCreate: Registered accelerometer listener");

        setContentView(R.layout.activity_main);

        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, this);

        Log.d(TAG, "onCreate: Initializing Sensor Services");

        mButton = (Button)findViewById(R.id.Start);

        ConnectivityManager connManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo mWifi = connManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
        //on click it will record these data points
        mButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                Log.d(TAG, "CLICK!!!!!!!!!!!!!!!!!!!");
                EditText n1 = (EditText)findViewById(R.id.minLat);
                minLat = Double.parseDouble(n1.getText().toString());
                EditText n2 = (EditText)findViewById(R.id.maxLat);
                maxLat = Double.parseDouble(n2.getText().toString());
                EditText n3 = (EditText)findViewById(R.id.minLongi);
                minLongi = Double.parseDouble(n3.getText().toString());
                EditText n4 = (EditText)findViewById(R.id.maxLongi);
                maxLongi = Double.parseDouble(n4.getText().toString());
                try {
                    collect();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            });
    }

    public void collect() throws InterruptedException {
        Log.d(TAG, "COLLECT!!!!!!!!!!!!!!!!!!!");
        int count = 0;
        boolean cont = true;
        UUID packet_id = UUID.randomUUID();
        HashMap<Timestamp, HashMap<String, Double>> dict = new HashMap<Timestamp, HashMap<String, Double>>();

        //while (latitude < minLat || latitude > maxLat || longitude > maxLongi || longitude < minLongi) {
        //do nothing
        //}
        //while (minLat < latitude && latitude < maxLat && minLongi < longitude && longitude < maxLongi) {
        while (count < 5000) {
            sensorManager.registerListener((SensorEventListener) MainActivity.this, accelerometer, SensorManager.SENSOR_DELAY_NORMAL);

            Log.d(TAG, "values0: " + xAccel + yAccel + zAccel + latitude + longitude);
            Timestamp timestamp = new Timestamp(System.currentTimeMillis());
            dict.put(timestamp, new HashMap<String, Double>() {{
                put("xAccel", xAccel);
                put("yAccel", yAccel);
                put("zAccel", zAccel);
                put("latitude", latitude);
                put("longitude", longitude);
            }});
            Log.d(TAG, "values: " + xAccel + yAccel + zAccel + latitude + longitude);
            count += 1;
            if (dict.size() >= 10000) {
                Thread object1 = new Thread(new MainActivity());
                object1.start();
                cont = false;
            }
            if (!cont) {
                break;
            }
            sensorManager.unregisterListener(this);
        }
        upload(dict, packet_id);
    }


    public void upload(HashMap data, UUID packet_id) throws InterruptedException {
        String GUID = "68c1c9a8-deed-4188-8c96-e66ce5a3de01";
        HashMap<String, Object> map = new HashMap<String, Object>();
        map.put("GUID", GUID);
        map.put("packet_id", packet_id);
        map.put("data", data);

        Gson gson = new Gson();
        String json = gson.toJson(map);
/*
        while (!mWifi.isConnected()) {
            Thread.sleep(600000);
        }

        if (mWifi.isConnected()) {
            String url = "http://mtakac.com/data/jake.php?test=GETMETHOD";
        }
*/
    }

    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {
        xAccel = sensorEvent.values[0];
        yAccel = sensorEvent.values[1];
        zAccel = sensorEvent.values[2];
        Log.d(TAG, "onSensorChanged: X: " + sensorEvent.values[0] + "onSensorChanged: Y:" + sensorEvent.values[1] + "onSensorChanged: Z:" + sensorEvent.values[2]);
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
    }

    @Override
    public void onLocationChanged(Location location) {
        latitude = location.getLatitude();
        longitude = location.getLongitude();
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {
        //Log.d("Latitude","status");
    }

    @Override
    public void onProviderEnabled(String provider) {
        //Log.d("Latitude","enable");
    }

    @Override
    public void onProviderDisabled(String provider) {
        //Log.d("Latitude","disable");
    }

    @Override
    public void run() {

    }
}