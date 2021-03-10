package com.ammt.self.adapters;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.text.TextUtils;
import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.ammt.self.R;
import com.ammt.self.callbacks.JournalsOnClickListener;
import com.ammt.self.databinding.JournalsListRowBinding;
import com.ammt.self.models.Journal;
import com.ammt.self.ui.LoginActivity;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.squareup.picasso.Picasso;

import java.util.List;


public class JournalsRecyclerViewAdapter extends RecyclerView.Adapter<JournalsRecyclerViewAdapter.ViewHolder> {
    private final Context context;
    private final List<Journal> journalList;
    JournalsOnClickListener callback;
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();
    private final CollectionReference collectionReference = db.collection("Journals");

    public JournalsRecyclerViewAdapter(Context context, List<Journal> journalList, JournalsOnClickListener callback) {
        this.context = context;
        this.journalList = journalList;
        this.callback = callback;
    }

    @NonNull
    @Override
    public JournalsRecyclerViewAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(JournalsListRowBinding.inflate(LayoutInflater.from(context), parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Journal journal = journalList.get(position);
        holder.binding.journalsTitleListRow.setText(journal.getTitle());
        holder.binding.journalsThoughtListRow.setText(journal.getThought());
        holder.binding.journalsTimeListRow.setText(DateUtils.getRelativeTimeSpanString(journal.getTimeStamp().getSeconds() * 1000));
        holder.binding.journalsUsernameListRow.setText(journal.getUsername());
        Picasso.get().load(journal.getImageUri()).placeholder(R.drawable.placeholder_image).fit().into(holder.binding.journalsImageListRow);
    }

    @Override
    public int getItemCount() {
        return journalList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder{
        private final JournalsListRowBinding binding;
        public ViewHolder(JournalsListRowBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
            binding.journalsShareButtonListRow.setOnClickListener(view ->
               callback.shareJournal(getAdapterPosition()));

            binding.journalsDeleteButtonListRow.setOnClickListener(view ->
                callback.deleteJournal(getAdapterPosition()));
        }
    }


}
