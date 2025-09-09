package com.example.projectkrs;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class ChangePasswordActivity extends AppCompatActivity {

    private EditText editTextCurrentPassword, editTextNewPassword;
    private Button buttonChangePassword;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_change_password);

        // Initialize UI components
        editTextCurrentPassword = findViewById(R.id.editTextCurrentPassword);
        editTextNewPassword = findViewById(R.id.editTextNewPassword);
        buttonChangePassword = findViewById(R.id.buttonChangePassword);

        // Set click listener for Change Password button
        buttonChangePassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                changePassword();
            }
        });
    }

    private void changePassword() {
        // Get current password and new password from EditText fields
        String currentPassword = editTextCurrentPassword.getText().toString().trim();
        String newPassword = editTextNewPassword.getText().toString().trim();

        // Validate input
        if (TextUtils.isEmpty(currentPassword) || TextUtils.isEmpty(newPassword)) {
            Toast.makeText(ChangePasswordActivity.this, "Please enter both current and new passwords", Toast.LENGTH_SHORT).show();
            return;
        }

        // Get the current user
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            // Reauthenticate the user with their current password
            AuthCredential credential = EmailAuthProvider.getCredential(user.getEmail(), currentPassword);
            user.reauthenticate(credential)
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            // Update the user's password
                            user.updatePassword(newPassword)
                                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void aVoid) {
                                            // Password change successful
                                            Toast.makeText(ChangePasswordActivity.this, "Password changed successfully", Toast.LENGTH_SHORT).show();
                                            setResult(RESULT_OK);
                                            finish();
                                        }
                                    })
                                    .addOnFailureListener(new OnFailureListener() {
                                        @Override
                                        public void onFailure(@NonNull Exception e) {
                                            // Password change failed
                                            Toast.makeText(ChangePasswordActivity.this, "Failed to update password: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                        }
                                    });
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            // Reauthentication failed
                            Toast.makeText(ChangePasswordActivity.this, "Failed to reauthenticate: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
        }
    }
}
