package com.ammt.self.ui;


import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;

import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.ammt.self.R;

import com.ammt.self.controller.JournalApi;
import com.ammt.self.databinding.ActivityLoginBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.Objects;

public class LoginActivity extends AppCompatActivity implements View.OnClickListener {
    private FirebaseUser currentUser;
    private FirebaseAuth firebaseAuth;
    private ActivityLoginBinding binding;


    FirebaseFirestore db = FirebaseFirestore.getInstance();
    CollectionReference collectionReference = db.collection("Users");


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityLoginBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        Objects.requireNonNull(getSupportActionBar()).setElevation(0);

        firebaseAuth = FirebaseAuth.getInstance();

        binding.logLogin.setOnClickListener(this);
        binding.createAccountLogin.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch(v.getId()) {
            case R.id.log_login:
                logIntoJournals();
                break;
            case R.id.createAccount_login:
                startActivity(new Intent(LoginActivity.this, CreateAccountActivity.class));
                break;
        }
    }

    private void logIntoJournals() {
        String getEmail = binding.emailLogin.getText().toString().trim();
        String getPassword = binding.passwordLogin.getText().toString().trim();
        if (!TextUtils.isEmpty(getEmail) && !TextUtils.isEmpty(getPassword)) {
            binding.progressBarLogin.setVisibility(View.VISIBLE);
            firebaseAuth.signInWithEmailAndPassword(getEmail, getPassword)
                    .addOnCompleteListener(this, task -> {

                        if (task.isSuccessful()) {
                            currentUser = firebaseAuth.getCurrentUser();
                            assert currentUser != null;
                            String currentUserId = currentUser.getUid();

                            binding.progressBarLogin.setVisibility(View.INVISIBLE);


                            collectionReference
                                    .whereEqualTo("userId", currentUserId).addSnapshotListener((value, error) -> {

                                if (error != null) return;
                                assert value != null;
                                for (QueryDocumentSnapshot val : value) {
                                    JournalApi journalApi = JournalApi.getInstance();
                                    journalApi.setUsername(val.getString("username"));
                                    journalApi.setUserId(currentUserId);
                                    startActivity(new Intent(LoginActivity.this, JournalsListActivity.class));
                                    finish();
                                }
                            });


                        } else {
                            binding.progressBarLogin.setVisibility(View.INVISIBLE);
                            Toast.makeText(LoginActivity.this, "Try Again", Toast.LENGTH_SHORT).show();
                        }
                    });
        } else if (!getEmail.isEmpty()) {
            binding.progressBarLogin.setVisibility(View.INVISIBLE);
            Toast.makeText(LoginActivity.this, "Password is needed", Toast.LENGTH_SHORT).show();
        } else if (!getPassword.isEmpty()) {
            binding.progressBarLogin.setVisibility(View.INVISIBLE);
            Toast.makeText(LoginActivity.this, "Email is needed", Toast.LENGTH_SHORT).show();
        } else {
            binding.progressBarLogin.setVisibility(View.INVISIBLE);
            Toast.makeText(LoginActivity.this, "Empty fields are not allowed", Toast.LENGTH_SHORT).show();
        }
    }
}
