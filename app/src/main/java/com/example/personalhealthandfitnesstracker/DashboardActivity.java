package com.example.personalhealthandfitnesstracker;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.CollectionReference;

public class DashboardActivity extends AppCompatActivity implements SensorEventListener {

    private TextView txtUser, txtStepValue, txtMealValue, txtHydrationValue;
    private SensorManager sensorManager;
    private Sensor stepCounterSensor;
    private float totalSteps = 0f;
    private float previousTotalSteps = 0f;
    private boolean running = false;
    private FirebaseFirestore db;
    private FirebaseAuth auth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_dashboard);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Initialize Views
        txtUser = findViewById(R.id.txtUser);
        txtStepValue = findViewById(R.id.txtStepValue);
        txtMealValue = findViewById(R.id.txtMealValue);
        txtHydrationValue = findViewById(R.id.txtHydrationValue);

        // Initialize Firestore and Firebase Authentication
        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();

        // Load data from FireStore
        loadMealData();
        loadHydrationData();

        // Initialize Buttons
        Button btnMealLogging = findViewById(R.id.btnViewMoreMeal);
        Button btnHydration = findViewById(R.id.btnViewMoreHydration);

        // Set up the meal logging button
        btnMealLogging.setOnClickListener(v -> {
            Intent intent = new Intent(DashboardActivity.this, MealLoggerActivity.class);
            startActivity(intent);
        });

        // Set up the hydration button
        btnHydration.setOnClickListener(v -> {
            Intent intent = new Intent(DashboardActivity.this, HydrationActivity.class);
            startActivity(intent);
        });

        // Get the current user
//        FirebaseUser currentUser = auth.getCurrentUser();
//        if (currentUser != null) {
//            txtUser.setText(currentUser.getDisplayName()); // Display user name
//        } else {
//            // Handle the case where the user is not logged in
//            txtUser.setText("Guest");
//        }

        // Initialize the sensor manager and step counter
        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        stepCounterSensor = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER);

        // Data for Steps
        loadData(); // Load previously saved step count
        resetSteps(); // Set up step reset functionality

        // Set up Bottom Navigation
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);
        bottomNavigationView.setOnNavigationItemSelectedListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.nav_home) {
                // Already in DashboardActivity, do nothing or refresh
                return true;
            } else if (itemId == R.id.nav_meal) {
                startActivity(new Intent(DashboardActivity.this, MealLoggerActivity.class));
                return true;
            } else if (itemId == R.id.nav_water) {
                startActivity(new Intent(DashboardActivity.this, HydrationActivity.class));
                return true;
            } else {
                return false;
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        running = true;

        // Get the step counter sensor
        Sensor stepSensor = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER);

        if (stepSensor == null) {
            // Show a toast message if no sensor is detected
            Toast.makeText(this, "No sensor detected on this device", Toast.LENGTH_SHORT).show();
        } else {
            // Register the sensor listener
            sensorManager.registerListener(this, stepSensor, SensorManager.SENSOR_DELAY_UI);
        }
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        // Get the TextView to display steps
        TextView tv_stepsTaken = findViewById(R.id.txtStepValue);

        if (running) {
            totalSteps = event.values[0];

            // Calculate current steps
            int currentSteps = (int) totalSteps - (int) previousTotalSteps;

            // Update the TextView with current steps
            tv_stepsTaken.setText(String.valueOf(currentSteps) + " STEPS");
        }
    }

    private void resetSteps() {
        TextView tv_stepsTaken = findViewById(R.id.txtStepValue);
        tv_stepsTaken.setOnClickListener(v -> {
            // Show a toast message for resetting steps
            Toast.makeText(this, "Long tap to reset steps", Toast.LENGTH_SHORT).show();
        });

        tv_stepsTaken.setOnLongClickListener(v -> {
            // Update previousTotalSteps before resetting
            previousTotalSteps = totalSteps;

            // Reset steps to 0
            tv_stepsTaken.setText("0 STEPS");

            // Save the data
            saveData();

            return true;
        });
    }


    private void saveData() {
        // Save the previous total steps using SharedPreferences
        SharedPreferences sharedPreferences = getSharedPreferences("myPrefs", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putFloat("key1", previousTotalSteps);
        editor.apply(); // Commit the changes
    }

    private void loadData() {
        // Load the previous total steps from SharedPreferences
        SharedPreferences sharedPreferences = getSharedPreferences("myPrefs", Context.MODE_PRIVATE);
        float savedNumber = sharedPreferences.getFloat("key1", 0f);

        // Log the saved number for debugging
        Log.d("MainActivity", String.valueOf(savedNumber));

        previousTotalSteps = savedNumber; // Update previous total steps
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        // No implementation needed for this app
    }

    private void loadMealData() {
        FirebaseUser currentUser = auth.getCurrentUser();
        if (currentUser != null) {
            DocumentReference userDoc = db.collection("users").document(currentUser.getUid());
            CollectionReference mealsCollection = userDoc.collection("meals");

            mealsCollection.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                @Override
                public void onComplete(Task<QuerySnapshot> task) {
                    if (task.isSuccessful()) {
                        float totalCalories = 0f;
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            // Assuming each meal document has a field named "calories"
                            Float calories = document.getDouble("calories").floatValue();
                            totalCalories += calories;
                        }
                        txtMealValue.setText(totalCalories + "\nCalories");
                    } else {
                        Log.w("DashboardActivity", "Error getting documents.", task.getException());
                    }
                }
            });
        }
    }

    private void loadHydrationData() {
        FirebaseUser currentUser = auth.getCurrentUser();
        if (currentUser != null) {
            DocumentReference userDoc = db.collection("users").document(currentUser.getUid());
            CollectionReference hydrationCollection = userDoc.collection("hydrations");

            hydrationCollection.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                @Override
                public void onComplete(Task<QuerySnapshot> task) {
                    if (task.isSuccessful()) {
                        float totalWaterIntake = 0f;
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            // Assuming each hydration document has a field named "waterIntake"
                            Float waterIntake = document.getDouble("waterIntake").floatValue();
                            totalWaterIntake += waterIntake;
                        }
                        txtHydrationValue.setText(totalWaterIntake + "\nLiters");
                    } else {
                        Log.w("DashboardActivity", "Error getting documents.", task.getException());
                    }
                }
            });
        }
    }
}
