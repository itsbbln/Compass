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

        // Initialize SensorManager
        compassSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);

        // Get the sensors
        accelerometer = compassSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
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
            float[] R = new float[9];
            float[] I = new float[9];
            boolean successful_read = SensorManager.getRotationMatrix(R, I, accel_read, magnetic_read);

            if (successful_read) {
                float[] orientation = new float[3];
                SensorManager.getOrientation(R, orientation);

                azimuth_angle = orientation[0];
                float degrees = (float) Math.toDegrees(azimuth_angle);
                degrees = (degrees + 360) % 360; // Normalize 0–360°

                // Determine direction
                String direction;
                if (degrees >= 337.5 || degrees < 22.5)
                    direction = "North";
                else if (degrees >= 22.5 && degrees < 67.5)
                    direction = "Northeast";
                else if (degrees >= 67.5 && degrees < 112.5)
                    direction = "East";
                else if (degrees >= 112.5 && degrees < 157.5)
                    direction = "Southeast";
                else if (degrees >= 157.5 && degrees < 202.5)
                    direction = "South";
                else if (degrees >= 202.5 && degrees < 247.5)
                    direction = "Southwest";
                else if (degrees >= 247.5 && degrees < 292.5)
                    direction = "West";
                else
                    direction = "Northwest";

                // Update TextViews separately
                tv_direction.setText(direction);
                tv_degrees.setText(String.format("%d°", Math.round(degrees)));

                // Rotate compass image
                RotateAnimation rotate = new RotateAnimation(
                        current_degree,
                        -Math.round(degrees),
                        Animation.RELATIVE_TO_SELF, 0.5f,
                        Animation.RELATIVE_TO_SELF, 0.5f
                );

                rotate.setDuration(100);
                rotate.setFillAfter(true);
                iv_compass.startAnimation(rotate);
                current_degree = -Math.round(degrees);
            }
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        // Not used
    }
}