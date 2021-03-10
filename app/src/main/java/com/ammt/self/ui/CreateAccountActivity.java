package com.ammt.self.ui;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.ammt.self.R;

import com.ammt.self.controller.JournalApi;
import com.ammt.self.databinding.ActivityCreateAccountBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;

import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;


public class CreateAccountActivity extends AppCompatActivity {

    private ActivityCreateAccountBinding binding;
    private FirebaseAuth firebaseAuth;
    private FirebaseUser currentUser;

    //Firebase connection
    FirebaseFirestore db = FirebaseFirestore.getInstance();

    CollectionReference collectionReference = db.collection("Users");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityCreateAccountBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        Objects.requireNonNull(getSupportActionBar()).setElevation(0);

        firebaseAuth = FirebaseAuth.getInstance();

        binding.accCreateAccount.setOnClickListener(view -> {

            String username = binding.usernameCreateAccount.getText().toString().trim();
            String email = binding.emailCreateAcc.getText().toString().trim();
            String password = binding.passwordCreateAcc.getText().toString().trim();

            createAccountAndSignIn(username, email, password);

        });

    }

    private void createAccountAndSignIn(final String username, String email, String password) {

        if (!TextUtils.isEmpty(username) && !TextUtils.isEmpty(email) && !TextUtils.isEmpty(password)) {
            binding.progressBarCreateAccount.setVisibility(View.VISIBLE);

            firebaseAuth.createUserWithEmailAndPassword(email, password)
                    .addOnCompleteListener(this, task -> {
                        if (task.isSuccessful()) {
                            //Login, save user and go to postActivity
                            currentUser = firebaseAuth.getCurrentUser();
                            assert currentUser != null;
                            String currentUserId = currentUser.getUid();

                            Map<String, String> userObj = new HashMap<>();
                            userObj.put("username", username);
                            userObj.put("userId", currentUserId);

                            collectionReference.add(userObj).addOnSuccessListener(documentReference -> documentReference.get().addOnCompleteListener(task1 -> {
                                if (Objects.requireNonNull(task1.getResult()).exists()) {

                                    binding.progressBarCreateAccount.setVisibility(View.INVISIBLE);
                                    String name = task1.getResult().getString("username");
                                    JournalApi journalApi = JournalApi.getInstance();
                                    journalApi.setUsername(name);
                                    journalApi.setUserId(currentUserId);
                                    startActivity(new Intent(CreateAccountActivity.this, PostJournalActivity.class));
                                    finish();

                                } else {

                                    binding.progressBarCreateAccount.setVisibility(View.INVISIBLE);
                                    Toast.makeText(CreateAccountActivity.this, "Something went wrong", Toast.LENGTH_SHORT).show();

                                }
                            })).addOnFailureListener(e -> {

                                binding.progressBarCreateAccount.setVisibility(View.INVISIBLE);
                                Toast.makeText(CreateAccountActivity.this, "Something went wrong", Toast.LENGTH_SHORT).show();

                            });
                        } else {

                            binding.progressBarCreateAccount.setVisibility(View.INVISIBLE);
                            Toast.makeText(CreateAccountActivity.this, "Something went wrong", Toast.LENGTH_SHORT).show();
                        }
                    }).addOnFailureListener(e -> {

                    });

        } else {

            Toast.makeText(CreateAccountActivity.this, "Empty fields aren't allowed", Toast.LENGTH_SHORT).show();

        }
    }
}
