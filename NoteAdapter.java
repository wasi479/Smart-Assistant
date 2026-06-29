package com.example.smartassistant;

import android.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

public class NoteAdapter
        extends RecyclerView.Adapter<NoteAdapter.NoteViewHolder> {

    ArrayList<NoteModel> noteList;

    FirebaseFirestore db;

    FirebaseAuth auth;

    OnNoteClickListener listener;

    // CLICK INTERFACE

    public interface OnNoteClickListener {

        void onNoteClick(NoteModel note);
    }

    // CONSTRUCTOR

    public NoteAdapter(
            ArrayList<NoteModel> noteList,
            OnNoteClickListener listener) {

        this.noteList = noteList;

        this.listener = listener;

        this.db = FirebaseFirestore.getInstance();

        this.auth = FirebaseAuth.getInstance();
    }

    @NonNull
    @Override
    public NoteViewHolder onCreateViewHolder(
            @NonNull ViewGroup parent,
            int viewType) {

        View view =
                LayoutInflater.from(parent.getContext())
                        .inflate(
                                R.layout.item_note,
                                parent,
                                false);

        return new NoteViewHolder(view);
    }

    @Override
    public void onBindViewHolder(
            @NonNull NoteViewHolder holder,
            int position) {

        NoteModel note =
                noteList.get(position);

        holder.noteTitle.setText(
                note.getTitle() != null
                        ? note.getTitle()
                        : "Untitled");

        String content =
                note.getContent() != null
                        ? note.getContent()
                        : "";

        // PREVIEW TEXT

        String preview =
                content.length() > 120
                        ? content.substring(0, 120) + "..."
                        : content;

        holder.noteContent.setText(
                preview);

        // DATE

        if (note.getCreatedAt() > 0) {

            SimpleDateFormat sdf =
                    new SimpleDateFormat(
                            "MMM dd, yyyy · HH:mm",
                            Locale.getDefault());

            String dateStr =
                    sdf.format(
                            new Date(
                                    note.getCreatedAt()));

            holder.noteDate.setText(
                    dateStr);

        } else {

            holder.noteDate.setText("");
        }

        // NOTE CLICK

        holder.itemView.setOnClickListener(v -> {

            if (listener != null) {

                listener.onNoteClick(note);
            }
        });

        // DELETE BUTTON

        holder.deleteNoteButton.setOnClickListener(v -> {

            int adapterPosition =
                    holder.getAdapterPosition();

            if (adapterPosition == RecyclerView.NO_POSITION)
                return;

            new AlertDialog.Builder(v.getContext())
                    .setTitle("Delete Note")
                    .setMessage(
                            "Are you sure you want to delete this note?")
                    .setPositiveButton(
                            "Delete",
                            (dialog, which) ->
                                    deleteNote(v, adapterPosition))
                    .setNegativeButton(
                            "Cancel",
                            null)
                    .show();
        });
    }

    private void deleteNote(
            View view,
            int position) {

        if (position < 0 || position >= noteList.size())
            return;

        NoteModel note =
                noteList.get(position);

        String docId =
                note.getDocumentId();

        if (docId == null || docId.isEmpty()) {

            Toast.makeText(
                    view.getContext(),
                    "Cannot delete note",
                    Toast.LENGTH_SHORT).show();

            return;
        }

        FirebaseUser user =
                auth.getCurrentUser();

        if (user == null)
            return;

        db.collection("users")
                .document(user.getUid())
                .collection("notes")
                .document(docId)
                .delete()
                .addOnSuccessListener(unused -> {

                    if (position < noteList.size()) {

                        noteList.remove(position);

                        notifyItemRemoved(position);

                        notifyItemRangeChanged(
                                position,
                                noteList.size());

                        Toast.makeText(
                                view.getContext(),
                                "Note deleted",
                                Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> {

                    Toast.makeText(
                            view.getContext(),
                            "Failed to delete note",
                            Toast.LENGTH_SHORT).show();
                });
    }

    @Override
    public int getItemCount() {

        return noteList.size();
    }

    // VIEW HOLDER

    static class NoteViewHolder
            extends RecyclerView.ViewHolder {

        TextView noteTitle;

        TextView noteContent;

        TextView noteDate;

        TextView deleteNoteButton;

        NoteViewHolder(
                @NonNull View itemView) {

            super(itemView);

            noteTitle =
                    itemView.findViewById(
                            R.id.noteTitle);

            noteContent =
                    itemView.findViewById(
                            R.id.noteContent);

            noteDate =
                    itemView.findViewById(
                            R.id.noteDate);

            deleteNoteButton =
                    itemView.findViewById(
                            R.id.deleteNoteButton);
        }
    }
}