package com.example.personalhealthandfitnesstracker;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
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
import com.google.firebase.firestore.FirebaseFirestore;

public class HydrationActivity extends AppCompatActivity {

    private EditText txtWaterIntake, txtDate, txtNote;
    private Button btnSave;
    private FirebaseFirestore db;
    private FirebaseAuth auth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_hydration);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Initialize Firebase
        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();

        // Initialize Views
        txtWaterIntake = findViewById(R.id.txtWaterIntake);
        txtDate = findViewById(R.id.txtDate);
        txtNote = findViewById(R.id.txtNote);
        btnSave = findViewById(R.id.saveButton);

        // Set up the save button click listener
        btnSave.setOnClickListener(v -> saveHydrationData());
    }

    private void saveHydrationData() {
        String waterIntake = txtWaterIntake.getText().toString().trim();
        String date = txtDate.getText().toString().trim();
        String note = txtNote.getText().toString().trim();

        // Validate inputs
        if (waterIntake.isEmpty() || date.isEmpty()) {
            Toast.makeText(this, "Please fill in all required fields", Toast.LENGTH_SHORT).show();
            return;
        }

        // Get the current user
        FirebaseUser currentUser = auth.getCurrentUser();
        if (currentUser == null) {
            Toast.makeText(this, "User not logged in", Toast.LENGTH_SHORT).show();
            return;
        }

        // Create a map to store hydration data
        HydrationModel hydration = new HydrationModel();
        hydration.setWaterIntake(Integer.parseInt(waterIntake));
        hydration.setDate(Integer.parseInt(date));
        hydration.setNote(note);

        // Store the meal data in Firestore
        CollectionReference mealsCollection = db.collection("users")
                .document(currentUser.getUid())
                .collection("hydrations"); // Reference to the meals subcollection

        mealsCollection.add(hydration) // Use add() to create a new document with a unique ID
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(HydrationActivity.this, "Hydration logged successfully", Toast.LENGTH_SHORT).show();
                    clearFields(); // Clear input fields after saving
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(HydrationActivity.this, "Error hydration: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });

        // Set up Bottom Navigation
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);
        bottomNavigationView.setOnNavigationItemSelectedListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.nav_home) {
                startActivity(new Intent(HydrationActivity.this, DashboardActivity.class));
                return true;
            } else if (itemId == R.id.nav_meal) {
                startActivity(new Intent(HydrationActivity.this, MealLoggerActivity.class));
                return true;
            } else if (itemId == R.id.nav_water) {
                // Already in MealLoggerActivity, do nothing or refresh
                return true;
            } else {
                return false;
            }
        });

        // Set the selected item to the Meal tab
        bottomNavigationView.setSelectedItemId(R.id.nav_water);
    }

    private void clearFields() {
        txtWaterIntake.setText("");
        txtDate.setText("");
        txtNote.setText("");
    }
}
