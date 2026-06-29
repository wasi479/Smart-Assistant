package com.example.smartassistant;

import android.app.AlertDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

public class NotesFragment extends Fragment {

    RecyclerView recyclerView;

    ArrayList<NoteModel> noteList;

    NoteAdapter adapter;

    LinearLayout emptyStateLayout;

    FirebaseFirestore db;

    FirebaseAuth auth;

    public NotesFragment() {

    }

    @Nullable
    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater,
            @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {

        View view =
                inflater.inflate(
                        R.layout.fragment_notes,
                        container,
                        false);

        recyclerView =
                view.findViewById(
                        R.id.notesRecyclerView);

        emptyStateLayout =
                view.findViewById(
                        R.id.emptyStateLayout);

        noteList =
                new ArrayList<>();

        db =
                FirebaseFirestore.getInstance();

        auth =
                FirebaseAuth.getInstance();

        // ADAPTER

        adapter =
                new NoteAdapter(
                        noteList,
                        note -> {

                            AlertDialog.Builder builder =
                                    new AlertDialog.Builder(
                                            requireContext());

                            builder.setTitle(
                                    note.getTitle());

                            StringBuilder message =
                                    new StringBuilder();

                            message.append(
                                    note.getContent());

                            if (note.getCreatedAt() > 0) {

                                message.append("\n\n");

                                SimpleDateFormat sdf =
                                        new SimpleDateFormat(
                                                "MMM dd, yyyy · HH:mm",
                                                Locale.getDefault());

                                String date =
                                        sdf.format(
                                                new Date(
                                                        note.getCreatedAt()));

                                message.append("Saved: ")
                                        .append(date);
                            }

                            builder.setMessage(
                                    message.toString());

                            builder.setPositiveButton(
                                    "Close",
                                    null);

                            builder.show();
                        });

        // EMPTY STATE OBSERVER

        adapter.registerAdapterDataObserver(
                new RecyclerView.AdapterDataObserver() {

                    @Override
                    public void onChanged() {

                        super.onChanged();

                        updateEmptyState();
                    }

                    @Override
                    public void onItemRangeRemoved(
                            int positionStart,
                            int itemCount) {

                        super.onItemRangeRemoved(
                                positionStart,
                                itemCount);

                        updateEmptyState();
                    }
                });

        recyclerView.setLayoutManager(
                new LinearLayoutManager(
                        getContext()));

        recyclerView.setAdapter(
                adapter);

        updateEmptyState();

        loadNotesFromFirestore();

        return view;
    }

    // LOAD NOTES

    private void loadNotesFromFirestore() {

        FirebaseUser user =
                auth.getCurrentUser();

        if (user == null)
            return;

        db.collection("users")
                .document(user.getUid())
                .collection("notes")
                .orderBy(
                        "createdAt",
                        com.google.firebase.firestore.Query.Direction.DESCENDING)

                .get()

                .addOnCompleteListener(task -> {

                    if (!isAdded() || getContext() == null)
                        return;

                    if (!task.isSuccessful())
                        return;

                    noteList.clear();

                    for (QueryDocumentSnapshot doc
                            : task.getResult()) {

                        NoteModel note =
                                new NoteModel();

                        note.setDocumentId(
                                doc.getId());

                        note.setTitle(
                                doc.getString("title"));

                        note.setContent(
                                doc.getString("content"));

                        Long createdAt =
                                doc.getLong("createdAt");

                        if (createdAt != null) {

                            note.setCreatedAt(
                                    createdAt);
                        }

                        String type =
                                doc.getString("type");

                        if (type != null) {

                            note.setType(type);
                        }

                        noteList.add(note);
                    }

                    adapter.notifyDataSetChanged();

                    updateEmptyState();
                });
    }

    // EMPTY STATE

    private void updateEmptyState() {

        if (!isAdded() || getView() == null)
            return;

        if (noteList.isEmpty()) {

            emptyStateLayout.setVisibility(
                    View.VISIBLE);

            recyclerView.setVisibility(
                    View.GONE);

        } else {

            emptyStateLayout.setVisibility(
                    View.GONE);

            recyclerView.setVisibility(
                    View.VISIBLE);
        }
    }
}