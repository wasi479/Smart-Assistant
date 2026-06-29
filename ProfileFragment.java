package com.example.smartassistant;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class ProfileFragment extends Fragment {

    TextView userName;

    TextView userRole;

    TextView editProfileText;

    TextView memberSinceText;

    TextView tasksCompletedNumber;

    TextView notesCountNumber;

    MaterialButton logoutButton;

    FirebaseAuth auth;

    FirebaseFirestore db;

    public ProfileFragment() {

    }

    @Nullable
    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater,
            @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {

        View view =
                inflater.inflate(
                        R.layout.fragment_profile,
                        container,
                        false);

        auth =
                FirebaseAuth.getInstance();

        db =
                FirebaseFirestore.getInstance();

        // CONNECT VIEWS

        userName =
                view.findViewById(
                        R.id.userName);

        userRole =
                view.findViewById(
                        R.id.userRole);

        editProfileText =
                view.findViewById(
                        R.id.editProfileText);

        memberSinceText =
                view.findViewById(
                        R.id.memberSinceText);

        tasksCompletedNumber =
                view.findViewById(
                        R.id.tasksCompletedNumber);

        notesCountNumber =
                view.findViewById(
                        R.id.notesCountNumber);

        logoutButton =
                view.findViewById(
                        R.id.logoutButton);

        // LOAD PROFILE

        loadProfile();

        // EDIT NAME

        editProfileText.setOnClickListener(v -> {

            showEditNameDialog();

        });

        // LOGOUT

        logoutButton.setOnClickListener(v -> {

            auth.signOut();

            Intent intent =
                    new Intent(
                            getActivity(),
                            LoginActivity.class);

            intent.addFlags(
                    Intent.FLAG_ACTIVITY_CLEAR_TOP
                            | Intent.FLAG_ACTIVITY_NEW_TASK
                            | Intent.FLAG_ACTIVITY_CLEAR_TASK);

            startActivity(intent);

            if (getActivity() != null) {

                getActivity().finish();
            }
        });

        return view;
    }

    // EDIT NAME DIALOG

    private void showEditNameDialog() {

        if (!isAdded() || getContext() == null)
            return;

        AlertDialog.Builder builder =
                new AlertDialog.Builder(
                        requireContext());

        builder.setTitle(
                "Edit Display Name");

        final EditText input =
                new EditText(
                        requireContext());

        input.setHint(
                "Enter your name");

        if (userName != null
                && !userName.getText()
                .toString()
                .equals("Loading...")) {

            input.setText(
                    userName.getText()
                            .toString());
        }

        input.setSingleLine(true);

        int padding =
                (int) (16
                        * getResources()
                        .getDisplayMetrics()
                        .density);

        input.setPadding(
                padding,
                padding,
                padding,
                padding);

        builder.setView(input);

        builder.setPositiveButton(
                "Save",
                (dialog, which) -> {

                    String newName =
                            input.getText()
                                    .toString()
                                    .trim();

                    if (newName.isEmpty()) {

                        Toast.makeText(
                                getContext(),
                                "Name cannot be empty",
                                Toast.LENGTH_SHORT).show();

                        return;
                    }

                    updateDisplayName(
                            newName);
                });

        builder.setNegativeButton(
                "Cancel",
                null);

        builder.show();
    }

    // UPDATE NAME

    private void updateDisplayName(
            String newName) {

        FirebaseUser user =
                auth.getCurrentUser();

        if (user == null)
            return;

        Map<String, Object> updates =
                new HashMap<>();

        updates.put(
                "fullName",
                newName);

        db.collection("users")
                .document(user.getUid())
                .set(updates)

                .addOnSuccessListener(unused -> {

                    if (!isAdded()
                            || getContext() == null)
                        return;

                    if (userName != null) {

                        userName.setText(
                                newName);
                    }

                    Toast.makeText(
                            getContext(),
                            "Name updated",
                            Toast.LENGTH_SHORT).show();
                })

                .addOnFailureListener(e -> {

                    if (!isAdded()
                            || getContext() == null)
                        return;

                    Toast.makeText(
                            getContext(),
                            "Update failed",
                            Toast.LENGTH_SHORT).show();
                });
    }

    // LOAD PROFILE

    private void loadProfile() {

        FirebaseUser user =
                auth.getCurrentUser();

        if (user == null)
            return;

        // MEMBER SINCE

        long creationTimestamp =
                user.getMetadata() != null
                        ? user.getMetadata()
                        .getCreationTimestamp()
                        : 0;

        if (creationTimestamp > 0
                && memberSinceText != null) {

            SimpleDateFormat sdf =
                    new SimpleDateFormat(
                            "MMM yyyy",
                            Locale.getDefault());

            String since =
                    "Member since "
                            + sdf.format(
                            new Date(
                                    creationTimestamp));

            memberSinceText.setText(
                    since);
        }

        // LOAD USER PROFILE

        db.collection("users")
                .document(user.getUid())
                .get()

                .addOnSuccessListener(doc -> {

                    if (!isAdded()
                            || getContext() == null)
                        return;

                    String name =
                            doc.getString(
                                    "fullName");

                    String email =
                            doc.getString(
                                    "email");

                    // DISPLAY NAME

                    if (userName != null) {

                        if (name != null
                                && !name.isEmpty()) {

                            userName.setText(name);

                        } else {

                            userName.setText("User");
                        }
                    }

                    // DISPLAY EMAIL

                    if (userRole != null) {

                        if (email != null
                                && !email.isEmpty()) {

                            userRole.setText(email);

                        } else if (user.getEmail() != null) {

                            userRole.setText(
                                    user.getEmail());
                        }
                    }
                })

                .addOnFailureListener(e -> {

                    if (!isAdded()
                            || getContext() == null)
                        return;

                    Toast.makeText(
                            getContext(),
                            "Could not load profile",
                            Toast.LENGTH_SHORT).show();
                });

        // TASKS COMPLETED COUNT

        db.collection("users")
                .document(user.getUid())
                .collection("tasks")
                .whereEqualTo("completed", true)
                .get()

                .addOnSuccessListener(query -> {

                    if (!isAdded()
                            || getContext() == null)
                        return;

                    if (tasksCompletedNumber != null) {

                        tasksCompletedNumber.setText(
                                String.valueOf(
                                        query.size()));
                    }
                });

        // NOTES COUNT

        db.collection("users")
                .document(user.getUid())
                .collection("notes")
                .get()

                .addOnSuccessListener(query -> {

                    if (!isAdded()
                            || getContext() == null)
                        return;

                    if (notesCountNumber != null) {

                        notesCountNumber.setText(
                                String.valueOf(
                                        query.size()));
                    }
                });
    }
}