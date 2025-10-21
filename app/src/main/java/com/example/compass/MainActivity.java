package com.example.compass;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.view.animation.Animation;
import android.view.animation.RotateAnimation;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import java.util.Locale;

public class MainActivity extends AppCompatActivity implements SensorEventListener {

    private Float azimuth_angle;
    private SensorManager compassSensorManager;
    private Sensor accelerometer;
    private Sensor magnetometer;
    private TextView tv_degrees;
    private TextView tv_direction;
    private ImageView iv_compass;
    private float current_degree = 0f;

    private float[] accel_read;
    private float[] magnetic_read;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Link XML components
        tv_degrees = findViewById(R.id.degrees);
        tv_direction = findViewById(R.id.direction);
        iv_compass = findViewById(R.id.compass);

        // Initialize SensorManager, which is mao ni ang mag bali mag activate sa sensors sa device/phone
        compassSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);

        // Get the sensors
        //measures the gravity og device tilt
        accelerometer = compassSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        //magnetic field and find magnetic north
        magnetometer = compassSensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
    }

    @Override
    protected void onResume() {
        super.onResume();
        compassSensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_UI);
        compassSensorManager.registerListener(this, magnetometer, SensorManager.SENSOR_DELAY_UI);
    }

    @Override
    protected void onPause() {
        super.onPause();
        compassSensorManager.unregisterListener(this);
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER)
            accel_read = event.values.clone();

        if (event.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD)
            magnetic_read = event.values.clone();

        if (accel_read != null && magnetic_read != null) {
            float[] R = new float[9]; //R Rotation Matrix
            float[] I = new float[9]; //I Inclination
            boolean successful_read = SensorManager.getRotationMatrix(R, I, accel_read, magnetic_read);

            if (successful_read) {
                float[] orientation = new float[3];
                SensorManager.getOrientation(R, orientation);

                azimuth_angle = orientation[0];
                float degrees = (float) Math.toDegrees(azimuth_angle);
                degrees = (degrees + 360) % 360; // Normalize 0–360°

                // Direction logic with 0° for N, E, S, W
                String direction;
                float displayDegrees;
                float tolerance = 1.0f;

                if (Math.abs(degrees - 0) <= tolerance || Math.abs(degrees - 360) <= tolerance) {
                    direction = "N";
                    displayDegrees = 0f;
                } else if (Math.abs(degrees - 90) <= tolerance) {
                    direction = "E";
                    displayDegrees = 0f;
                } else if (Math.abs(degrees - 180) <= tolerance) {
                    direction = "S";
                    displayDegrees = 0f;
                } else if (Math.abs(degrees - 270) <= tolerance) {
                    direction = "W";
                    displayDegrees = 0f;
                } else if (degrees > tolerance && degrees < 90 - tolerance) {
                    direction = "NE";
                    displayDegrees = degrees;
                } else if (degrees > 90 + tolerance && degrees < 180 - tolerance) {
                    direction = "SE";
                    displayDegrees = 180 - degrees;
                } else if (degrees > 180 + tolerance && degrees < 270 - tolerance) {
                    direction = "SW";
                    displayDegrees = degrees - 180;
                } else {
                    direction = "NW";
                    displayDegrees = 360 - degrees;
                }

                // Clamp display degrees between 1° and 89° for diagonals
                if (!direction.equals("N") && !direction.equals("E") &&
                        !direction.equals("S") && !direction.equals("W")) {
                    displayDegrees = Math.max(1, Math.min(89, displayDegrees));
                }

                // Update text
                tv_direction.setText(direction);
                tv_degrees.setText(String.format(Locale.getDefault(), "%.0f°", displayDegrees));

                // Rotate compass image
                RotateAnimation rotate = new RotateAnimation(
                        current_degree,
                        -degrees,
                        Animation.RELATIVE_TO_SELF, 0.5f,
                        Animation.RELATIVE_TO_SELF, 0.5f
                );

                rotate.setDuration(250);
                rotate.setFillAfter(true);
                iv_compass.startAnimation(rotate);
                current_degree = -degrees;
            }
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        // Not used
    }
}