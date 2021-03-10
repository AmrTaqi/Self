package com.ammt.self.ui;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Instrumentation;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.ammt.self.R;
import com.ammt.self.controller.JournalApi;
import com.ammt.self.databinding.ActivityPostJournalBinding;
import com.ammt.self.models.Journal;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.Date;
import java.util.Objects;

public class PostJournalActivity extends AppCompatActivity implements View.OnClickListener {

    private ActivityPostJournalBinding binding;
    private static final int GALLERY_CODE = 1;
    private Uri imageUri;

    private String currentUsername;
    private String currentUserId;

    //Connection to fireStore
    FirebaseFirestore db = FirebaseFirestore.getInstance();
    CollectionReference journalsCollectionReference = db.collection("Journals");
    StorageReference storageReference;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityPostJournalBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        Objects.requireNonNull(getSupportActionBar()).setElevation(0);


        storageReference = FirebaseStorage.getInstance().getReference();


        binding.postSaveButton.setOnClickListener(this);
        binding.postCameraButton.setOnClickListener(this);

        binding.postProgressBar.setVisibility(View.INVISIBLE);

        if (JournalApi.getInstance() != null) {
            currentUsername = JournalApi.getInstance().getUsername();
            currentUserId = JournalApi.getInstance().getUserId();
            binding.postUsernameTextView.setText(currentUsername);
        }

    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.post_save_button:
                saveJournal();
                    break;
            case R.id.post_cameraButton:
                Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                intent.setType("image/*");
                startActivityForResult(intent, GALLERY_CODE);
                break;
        }
    }

    private void saveJournal() {
        String title = binding.titlePostJournal.getText().toString().trim();
        String thought = binding.thoughtsPostJournal.getText().toString().trim();

        if (!TextUtils.isEmpty(title) && !TextUtils.isEmpty(thought) && imageUri != null) {

            binding.postProgressBar.setVisibility(View.VISIBLE);

            StorageReference filePath = storageReference.child("journal_images")
                    .child("my_image_" + Timestamp.now().getSeconds());

            filePath.putFile(imageUri).addOnSuccessListener(taskSnapshot ->
                    filePath.getDownloadUrl().addOnSuccessListener(uri -> {
                String url = uri.toString();
                Journal journal = new Journal();
                journal.setTitle(title);
                journal.setThought(thought);
                journal.setTimeStamp(new Timestamp(new Date()));
                journal.setImageUri(url);
                journal.setUserId(currentUserId);
                journal.setUsername(currentUsername);

                journalsCollectionReference.add(journal).addOnSuccessListener(documentReference -> {
                    binding.postProgressBar.setVisibility(View.INVISIBLE);
                    startActivity(new Intent(PostJournalActivity.this, JournalsListActivity.class));
                    finish();

                }).addOnFailureListener(e -> {
                        binding.postProgressBar.setVisibility(View.INVISIBLE);
                        Toast.makeText(PostJournalActivity.this, "Something went wrong", Toast.LENGTH_SHORT).show();
                });
            }).addOnFailureListener(e -> {

            })).addOnFailureListener(e -> {
                binding.postProgressBar.setVisibility(View.INVISIBLE);
                Toast.makeText(PostJournalActivity.this, "Something went wrong", Toast.LENGTH_SHORT).show();
            });
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == GALLERY_CODE && resultCode == RESULT_OK) {
            if (data != null) {
                imageUri = data.getData();
                binding.postBackgroundImageView.setImageURI(imageUri);
            }
        }
    }
}
