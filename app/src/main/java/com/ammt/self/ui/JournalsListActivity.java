package com.ammt.self.ui;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.ammt.self.R;
import com.ammt.self.adapters.JournalsRecyclerViewAdapter;
import com.ammt.self.callbacks.JournalsOnClickListener;
import com.ammt.self.controller.JournalApi;
import com.ammt.self.databinding.ActivityJournalsListBinding;
import com.ammt.self.databinding.DeleteConfirmationBinding;
import com.ammt.self.models.Journal;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class JournalsListActivity extends AppCompatActivity {

    private ActivityJournalsListBinding binding;
    private DeleteConfirmationBinding popUpDialogBinding;
    private AlertDialog confirmationDialog;
    private JournalsOnClickListener callback;
    private FirebaseAuth firebaseAuth;
    private FirebaseUser currentUser;
    private JournalsRecyclerViewAdapter journalsRecyclerViewAdapter;
    private List<Journal> journalsList;
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();
    private final CollectionReference collectionReference = db.collection("Journals");


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityJournalsListBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        Objects.requireNonNull(getSupportActionBar()).setElevation(0);

        popUpDialogBinding = DeleteConfirmationBinding.inflate(LayoutInflater.from(this));
        confirmationDialog = new AlertDialog.Builder(JournalsListActivity.this).setView(popUpDialogBinding.getRoot()).create();

        firebaseAuth = FirebaseAuth.getInstance();
        currentUser = firebaseAuth.getCurrentUser();
        journalsList = new ArrayList<>();
        binding.recyclerviewJournalsList.setHasFixedSize(true);
        binding.recyclerviewJournalsList.setLayoutManager(new LinearLayoutManager(this));

        callback = new JournalsOnClickListener() {
            @Override
            public void deleteJournal(int position) {
                confirmationDialog.show();
                popUpDialogBinding.yesRemoveConfirm.setOnClickListener(view -> {
                    confirmationDialog.dismiss();
                    binding.progressBarJournalList.setVisibility(View.VISIBLE);
                    Journal journal = journalsList.get(position);
                    collectionReference
                            .whereEqualTo("timeStamp", journal.getTimeStamp()).get().addOnSuccessListener(queryDocumentSnapshots -> {
                        if (!queryDocumentSnapshots.isEmpty()) {
                            for (QueryDocumentSnapshot val : queryDocumentSnapshots) {
                                val.getReference()
                                        .delete().addOnSuccessListener(aVoid -> {
                                    binding.progressBarJournalList.setVisibility(View.INVISIBLE);
                                    startActivity(new Intent(JournalsListActivity.this, JournalsListActivity.class));
                                    finish();
                                    Toast.makeText(JournalsListActivity.this, "Journal successfully deleted!", Toast.LENGTH_LONG).show();
                                }).addOnFailureListener(e -> {
                                        binding.progressBarJournalList.setVisibility(View.INVISIBLE);
                                Toast.makeText(JournalsListActivity.this, "Error deleting journal", Toast.LENGTH_LONG).show();
                                });
                                break;
                            }
                        }}).addOnFailureListener(e -> binding.progressBarJournalList.setVisibility(View.INVISIBLE));});

                popUpDialogBinding.noRemoveConfirm.setOnClickListener(view -> confirmationDialog.dismiss());

            }

            @Override
            public void shareJournal(int position) {
                Intent intent = new Intent(Intent.ACTION_SEND);
                intent.setType("text/plain");
                intent.putExtra(Intent.EXTRA_TEXT, journalsList.get(position).getTitle());
                intent.putExtra(Intent.EXTRA_SUBJECT, journalsList.get(position).getThought());
                startActivity(intent);
            }
        };

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_add:
                //Add journal post
                if (currentUser != null && firebaseAuth != null)
                startActivity(new Intent(JournalsListActivity.this, PostJournalActivity.class));
                break;
            case R.id.action_signOut:
                //sign out from account
                if (currentUser != null && firebaseAuth != null) {
                    firebaseAuth.signOut();
                    binding.progressBarJournalList.setVisibility(View.VISIBLE);
                    new Handler().postDelayed(() -> {
                        binding.progressBarJournalList.setVisibility(View.INVISIBLE);
                        startActivity(new Intent(JournalsListActivity.this, LoginActivity.class));
                        finish();
                    }, 2000);
                }

                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onStart() {
        super.onStart();
        collectionReference.whereEqualTo("userId", JournalApi.getInstance().getUserId())
                .addSnapshotListener((value, error) -> {
                    if (error != null) return;
                    assert value != null;
                    if (!value.isEmpty()) {
                        for (QueryDocumentSnapshot js : value) {
                            Journal journal = js.toObject(Journal.class);
                            journalsList.add(journal);
                        }


                        journalsRecyclerViewAdapter = new JournalsRecyclerViewAdapter(JournalsListActivity.this,
                                journalsList, callback);
                        binding.recyclerviewJournalsList.setAdapter(journalsRecyclerViewAdapter);
                        journalsRecyclerViewAdapter.notifyDataSetChanged();

                    } else {
                        binding.noThoughtEntryList.setVisibility(View.VISIBLE);
                    }

                });
    }
}