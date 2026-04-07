package com.example.question_3;

import android.os.Bundle;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity implements SensorEventListener {
    private SensorManager sensorManager;
    private Sensor accelerometer;
    private Sensor LightSensor;
    private Sensor ProximitySensor;
    private TextView tvAccel, tvLight, tvProximity, tvStatus;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        tvAccel = findViewById(R.id.tvAccelerometer);
        tvLight = findViewById(R.id.tvLight);
        tvProximity = findViewById(R.id.tvProximity);
        tvStatus = findViewById(R.id.tvStatus);

        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        LightSensor = sensorManager.getDefaultSensor(Sensor.TYPE_LIGHT);
        ProximitySensor = sensorManager.getDefaultSensor(Sensor.TYPE_PROXIMITY);

        StringBuilder status = new StringBuilder("Sensors Found: \n");
        if (accelerometer != null) {
            status.append("Accelerometer: YES  \n");
        } else {
            status.append("Accelerometer: NO  \n");
            tvAccel.setText("Not available");
        }

        if (LightSensor != null) {
            status.append("Light: YES  \n");
        } else {
            status.append("Light: NO  \n");
            tvLight.setText("Not available");
        }

        if (ProximitySensor != null) {
            status.append("Proximity: YES");
        } else {
            status.append("Proximity: NO");
            tvProximity.setText("Not available");
        }

        tvStatus.setText(status.toString());
    }

    // on resume for when the activity becomes visible
    // so listeners are registered here to only work when app is visible to user
    @Override
    protected void onResume() {
        super.onResume();
        if (accelerometer != null)
            sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_NORMAL);
        if (LightSensor != null)
            sensorManager.registerListener(this, LightSensor, SensorManager.SENSOR_DELAY_NORMAL);
        if (ProximitySensor != null)
            sensorManager.registerListener(this, ProximitySensor, SensorManager.SENSOR_DELAY_NORMAL);
    }

    @Override
    protected void onPause() {
        super.onPause();
        sensorManager.unregisterListener(this);
    }

    // ---SensorEventListener methods---
    @Override
    public void onSensorChanged(SensorEvent event) {
        switch (event.sensor.getType()) {
            case Sensor.TYPE_ACCELEROMETER:
                float x = event.values[0];
                float y = event.values[1];
                float z = event.values[2];
                tvAccel.setText(String.format("X: %.2f m/s²\nY: %.2f m/s²\nZ: %.2f m/s²", x, y, z));
                break;

            case Sensor.TYPE_LIGHT:
                float lux = event.values[0];
                String lightDesc;
                if (lux < 10) lightDesc = "(Very Dark)";
                else if (lux < 100) lightDesc = "(Dim)";
                else if (lux < 1000) lightDesc = "(Normal Room)";
                else lightDesc = "(Bright/Outdoor)";
                tvLight.setText(String.format("%.1f lux  %s", lux, lightDesc));
                break;

            case Sensor.TYPE_PROXIMITY:
                float distance = event.values[0];
                String proxDesc = distance == 0 ? "(Object NEAR)" : "(Object FAR)";
                tvProximity.setText(String.format("%.1f cm  %s", distance, proxDesc));
                break;
        }
    }

    // this method has no need here but must be implemented
    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        // only implemented no use
    }
}
