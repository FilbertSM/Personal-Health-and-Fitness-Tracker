package com.example.personalhealthandfitnesstracker;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

public class MealLoggerActivity extends AppCompatActivity {

    private EditText txtMealName, txtCalorie, txtWeight;
    private Button btnSave;
    private FirebaseFirestore db;
    private FirebaseAuth auth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_meal_logger);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Initialize Firebase
        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();

        // Initialize Views
        txtMealName = findViewById(R.id.txtMealName);
        txtCalorie = findViewById(R.id.txtCalorie);
        txtWeight = findViewById(R.id.txtWeight);
        btnSave = findViewById(R.id.saveButton);

        // Set up the save button click listener
        btnSave.setOnClickListener(v -> saveMealData());
    }

    private void saveMealData() {
        String mealName = txtMealName.getText().toString().trim();
        String calorieInput = txtCalorie.getText().toString().trim();
        String weightInput = txtWeight.getText().toString().trim();

        // Validate inputs
        if (mealName.isEmpty() || calorieInput.isEmpty() || weightInput.isEmpty()) {
            Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        // Get the current user
        FirebaseUser currentUser = auth.getCurrentUser();
        if (currentUser == null) {
            Toast.makeText(this, "User not logged in", Toast.LENGTH_SHORT).show();
            return;
        }

        MealModel meal = new MealModel();
        meal.setMealName(mealName);
        meal.setCalories(Integer.parseInt(calorieInput));
        meal.setWeight(Integer.parseInt(weightInput));

        // Store the meal data in Firestore
        CollectionReference mealsCollection = db.collection("users")
                .document(currentUser.getUid())
                .collection("meals"); // Reference to the meals subcollection

        mealsCollection.add(meal) // Use add() to create a new document with a unique ID
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(MealLoggerActivity.this, "Meal logged successfully", Toast.LENGTH_SHORT).show();
                    clearFields(); // Clear input fields after saving
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(MealLoggerActivity.this, "Error logging meal: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });

        // Set up Bottom Navigation
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);
        bottomNavigationView.setOnNavigationItemSelectedListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.nav_home) {
                startActivity(new Intent(MealLoggerActivity.this, DashboardActivity.class));
                return true;
            } else if (itemId == R.id.nav_meal) {
                // Already in MealLoggerActivity, do nothing or refresh
                return true;
            } else if (itemId == R.id.nav_water) {
                startActivity(new Intent(MealLoggerActivity.this, HydrationActivity.class));
                return true;
            } else {
                return false;
            }
        });

        // Set the selected item to the Meal tab
        bottomNavigationView.setSelectedItemId(R.id.nav_meal);
    }

    private void clearFields() {
        txtMealName.setText("");
        txtCalorie.setText("");
        txtWeight.setText("");
    }
}
