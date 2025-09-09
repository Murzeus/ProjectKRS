package com.example.projectkrs;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class RegisterActivity extends AppCompatActivity {

    private EditText editTextEmail, editTextPassword;
    private Button buttonRegister;
    private FirebaseAuth firebaseAuth;
    private Spinner categorySpinner; // Add Spinner for category selection
    private ArrayAdapter<String> categoryAdapter; // Adapter for category Spinner
    private Map<String, String> categoryTypesMap; // HashMap to store category options
    private TextView textViewSignIn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        // Initialize Firebase Auth
        firebaseAuth = FirebaseAuth.getInstance();

        editTextEmail = findViewById(R.id.editTextEmail);
        editTextPassword = findViewById(R.id.editTextPassword);
        buttonRegister = findViewById(R.id.buttonRegister);
        textViewSignIn = findViewById(R.id.textViewSignIn);
        categorySpinner = findViewById(R.id.categorySpinner); // Initialize Spinner

        // Initialize category options HashMap
        categoryTypesMap = new HashMap<>();
        categoryTypesMap.put("Kavinės", "cafe");
        categoryTypesMap.put("Restoranai", "restaurant");
        categoryTypesMap.put("Muziejai", "museum");
        categoryTypesMap.put("Autobusų stotys", "bus_station");
        categoryTypesMap.put("Traukinių stotys", "train_station");
        categoryTypesMap.put("Parkai", "park");
        categoryTypesMap.put("Prekybos centrai", "shopping_mall");
        categoryTypesMap.put("Bendros lankytinos vietos", "tourist_attraction");

        // Initialize ArrayAdapter with category options
        categoryAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, new ArrayList<>(categoryTypesMap.keySet()));
        categoryAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        // Set ArrayAdapter to Spinner
        categorySpinner.setAdapter(categoryAdapter);

        buttonRegister.setOnClickListener(v -> registerUser());
        textViewSignIn.setOnClickListener(v -> {
            // Open registration activity
            Intent intent = new Intent(RegisterActivity.this, LoginActivity.class);
            startActivity(intent);
        });
    }

    private void registerUser() {
        String email = editTextEmail.getText().toString();
        String password = editTextPassword.getText().toString();
        final String selectedCategory; // Placeholder for the selected category type

        // Retrieve the selected category from the spinner
        if (categorySpinner.getSelectedItem() != null) {
            selectedCategory = categoryTypesMap.get(categorySpinner.getSelectedItem().toString());
        } else {
            // Handle case where no category is selected
            // You may display an error message or set a default category
            return;
        }

        if (TextUtils.isEmpty(email)) {
            editTextEmail.setError("Email is required");
            return;
        }

        if (TextUtils.isEmpty(password)) {
            editTextPassword.setError("Password is required");
            return;
        }

        // Register user with Firebase
        firebaseAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Registration success
                            Toast.makeText(RegisterActivity.this, "Registration successful", Toast.LENGTH_SHORT).show();

                            // Save the selected category type to Firestore
                            saveCategoryTypeToFirestore(selectedCategory);

                            // Redirect to HomeActivity
                            Intent intent = new Intent(RegisterActivity.this, MainActivity.class);
                            startActivity(intent);
                            finish(); // Close RegisterActivity to prevent user from navigating back
                        } else {
                            // Registration failed
                            Toast.makeText(RegisterActivity.this, "Registration failed: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    private void saveCategoryTypeToFirestore(String categoryType) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        if (user != null) {
            Map<String, Object> userData = new HashMap<>();
            userData.put("categoryType", categoryType);

            db.collection("users").document(user.getUid()).set(userData)
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            Log.d("TAG", "Category type saved successfully");
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Log.w("TAG", "Error saving category type", e);
                        }
                    });
        }
    }
}
