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
    private ImageView iv_compass;
    private float current_degree = 0f;

    private float[] accel_read;
    private float[] magnetic_read;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        tv_degrees = findViewById(R.id.degrees);
        iv_compass = findViewById(R.id.compass);

        compassSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);

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
            accel_read = event.values.clone(); // clone to avoid referencing mutable array

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
                int degreesInt = Math.round(degrees);

                tv_degrees.setText(Integer.toString(degreesInt) + (char) 0x00B0 + " to absolute north.");

                RotateAnimation rotate = new RotateAnimation(
                        current_degree,
                        -degreesInt,
                        Animation.RELATIVE_TO_SELF, 0.5f,
                        Animation.RELATIVE_TO_SELF, 0.5f
                );

                rotate.setDuration(100);
                rotate.setFillAfter(true);
                iv_compass.startAnimation(rotate);
                current_degree = -degreesInt;
            }
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
    }
}
