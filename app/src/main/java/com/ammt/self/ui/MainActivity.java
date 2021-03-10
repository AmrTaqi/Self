package com.ammt.self.ui;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.ammt.self.R;
import com.ammt.self.controller.JournalApi;
import com.ammt.self.databinding.MainScreenLayoutBinding;
import com.ammt.self.models.Journal;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.Objects;


public class MainActivity extends AppCompatActivity {
    private FirebaseAuth firebaseAuth;
    private FirebaseAuth.AuthStateListener authStateListener;
    private FirebaseUser currentUser;

    private final FirebaseFirestore db = FirebaseFirestore.getInstance();
    private final CollectionReference collectionReference = db.collection("Users");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        com.ammt.self.databinding.MainScreenLayoutBinding binding = MainScreenLayoutBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        Objects.requireNonNull(getSupportActionBar()).setElevation(0);

        firebaseAuth = FirebaseAuth.getInstance();
        authStateListener = firebaseAuth -> {
            if (currentUser != null) {
                String currentUserId = currentUser.getUid();
                collectionReference.whereEqualTo("userId", currentUserId).addSnapshotListener((value, error) -> {

                    if (error != null) return;
                    assert value != null;
                    if (!value.isEmpty()) {
                        for (QueryDocumentSnapshot val: value) {
                            JournalApi journalApi = JournalApi.getInstance();
                            journalApi.setUsername(val.getString("username"));
                            journalApi.setUserId(val.getString("userId"));

                            startActivity(new Intent(MainActivity.this, JournalsListActivity.class));
                            finish();
                        }
                    }
                });
            }
        };



        binding.startMainButton.setOnClickListener(view -> {
            startActivity(new Intent(MainActivity.this, LoginActivity.class));
            finish();
        });

    }

    @Override
    protected void onStart() {
        super.onStart();
        currentUser = firebaseAuth.getCurrentUser();
        firebaseAuth.addAuthStateListener(authStateListener);
    }

    @Override
    protected void onPause() {
        super.onPause();
        firebaseAuth.removeAuthStateListener(authStateListener);
    }
}
