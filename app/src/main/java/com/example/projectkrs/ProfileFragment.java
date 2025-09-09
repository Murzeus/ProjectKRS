package com.example.projectkrs;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class ProfileFragment extends Fragment {

    private TextView textViewUserEmail;
    private Button buttonChangePassword, buttonLogout;
    private static final int CHANGE_PASSWORD_REQUEST = 1;


    public ProfileFragment() {
        // Required empty public constructor
    }

    @Nullable
    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == CHANGE_PASSWORD_REQUEST) {
            if (resultCode == Activity.RESULT_OK) {
                // Password change successful
            } else {
                // Password change failed or canceled
                Toast.makeText(getActivity(), "Password change failed or canceled", Toast.LENGTH_SHORT).show();
            }
        }
    }


    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile, container, false);

        // Initialize UI components
        textViewUserEmail = view.findViewById(R.id.textViewUserEmail);
        buttonChangePassword = view.findViewById(R.id.buttonChangePassword);
        buttonLogout = view.findViewById(R.id.buttonLogout);

        // Retrieve current user from Firebase Authentication
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            // Get user's email
            String userEmail = currentUser.getEmail();
            if (userEmail != null) {
                // Set user's email to the TextView
                textViewUserEmail.setText(userEmail);
            }
        }

        // Set click listener for Change Password button
        // Set click listener for Change Password button
        buttonChangePassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Start ChangePasswordActivity for result
                Intent intent = new Intent(getActivity(), ChangePasswordActivity.class);
                startActivityForResult(intent, CHANGE_PASSWORD_REQUEST);
            }
        });


        // Set click listener for Logout button
        buttonLogout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Sign out the user from Firebase Authentication
                FirebaseAuth.getInstance().signOut();

                // Redirect the user to the main activity
                Intent intent = new Intent(getActivity(), MainActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);

                // Show toast indicating successful logout
                Toast.makeText(getActivity(), "Logout successful", Toast.LENGTH_SHORT).show();
            }
        });

        return view;
    }
}
