package com.example.smartassistant;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
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
import com.google.android.material.card.MaterialCardView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class AIFragment extends Fragment {

    EditText promptInput;
    MaterialButton generateButton;
    MaterialCardView summarizeCard;
    MaterialCardView explainCard;
    MaterialCardView quizCard;
    MaterialCardView studyPlanCard;
    MaterialCardView responseLayout;
    TextView responseText;
    MaterialButton copyButton;
    MaterialButton shareButton;
    MaterialButton saveNoteButton;
    MaterialButton regenerateButton;

    String lastPrompt = "";

    FirebaseFirestore db;
    FirebaseAuth auth;

    public AIFragment() {}

    @Nullable
    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater,
            @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_a_i, container, false);

        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();

        promptInput = view.findViewById(R.id.promptInput);
        generateButton = view.findViewById(R.id.generateButton);
        summarizeCard = view.findViewById(R.id.summarizeCard);
        explainCard = view.findViewById(R.id.explainCard);
        quizCard = view.findViewById(R.id.quizCard);
        studyPlanCard = view.findViewById(R.id.studyPlanCard);
        responseLayout = view.findViewById(R.id.responseLayout);
        responseText = view.findViewById(R.id.responseText);
        copyButton = view.findViewById(R.id.copyButton);
        shareButton = view.findViewById(R.id.shareButton);
        saveNoteButton = view.findViewById(R.id.saveNoteButton);
        regenerateButton = view.findViewById(R.id.regenerateButton);

        responseLayout.setVisibility(View.GONE);

        summarizeCard.setOnClickListener(v -> {
            promptInput.setText("Summarize this topic: ");
            promptInput.requestFocus();
        });

        explainCard.setOnClickListener(v -> {
            promptInput.setText("Explain this concept simply: ");
            promptInput.requestFocus();
        });

        quizCard.setOnClickListener(v -> {
            promptInput.setText("Generate MCQs for: ");
            promptInput.requestFocus();
        });

        studyPlanCard.setOnClickListener(v -> {
            promptInput.setText("Create study plan for: ");
            promptInput.requestFocus();
        });

        generateButton.setOnClickListener(v -> {

            String prompt = promptInput.getText().toString().trim();

            if (prompt.isEmpty()) {
                Toast.makeText(getContext(), "Enter a prompt first", Toast.LENGTH_SHORT).show();
                return;
            }

            lastPrompt = prompt;
            generateButton.setEnabled(false);
            generateButton.setText("Generating...");

            GeminiApiService.generateResponse(
                    prompt,
                    new GeminiApiService.GeminiCallback() {

                        @Override
                        public void onSuccess(String response) {
                            if (getActivity() == null) return;

                            getActivity().runOnUiThread(() -> {
                                if (!isAdded() || getContext() == null) return;

                                responseLayout.setVisibility(View.VISIBLE);
                                responseText.setText(response);
                                generateButton.setEnabled(true);
                                generateButton.setText("Generate AI Response");
                            });
                        }

                        @Override
                        public void onFailure(String error) {
                            if (getActivity() == null) return;

                            getActivity().runOnUiThread(() -> {
                                if (!isAdded() || getContext() == null) return;

                                Toast.makeText(getContext(), error, Toast.LENGTH_LONG).show();
                                generateButton.setEnabled(true);
                                generateButton.setText("Generate AI Response");
                            });
                        }
                    });
        });

        copyButton.setOnClickListener(v -> {
            ClipboardManager clipboard = (ClipboardManager)
                    requireContext().getSystemService(Context.CLIPBOARD_SERVICE);
            ClipData clip = ClipData.newPlainText("AI Response", responseText.getText().toString());
            clipboard.setPrimaryClip(clip);
            Toast.makeText(getContext(), "Copied", Toast.LENGTH_SHORT).show();
        });

        shareButton.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_SEND);
            intent.setType("text/plain");
            intent.putExtra(Intent.EXTRA_TEXT, responseText.getText().toString());
            startActivity(Intent.createChooser(intent, "Share AI Response"));
        });

        saveNoteButton.setOnClickListener(v -> {
            if (!isAdded() || getContext() == null) return;

            FirebaseUser user = auth.getCurrentUser();
            if (user == null) {
                Toast.makeText(getContext(), "Not logged in", Toast.LENGTH_SHORT).show();
                return;
            }

            String content = responseText.getText().toString().trim();
            if (content.isEmpty()) {
                Toast.makeText(getContext(), "No content to save", Toast.LENGTH_SHORT).show();
                return;
            }

            String title = lastPrompt.length() > 50
                    ? lastPrompt.substring(0, 50) + "..."
                    : lastPrompt;

            Map<String, Object> note = new HashMap<>();
            note.put("userId", user.getUid());
            note.put("title", title);
            note.put("content", content);
            note.put("createdAt", System.currentTimeMillis());

            db.collection("users")
                    .document(user.getUid())
                    .collection("notes")
                    .add(note)
                    .addOnSuccessListener(docRef -> {
                        if (!isAdded() || getContext() == null) return;
                        Toast.makeText(getContext(), "Note saved!", Toast.LENGTH_SHORT).show();
                    })
                    .addOnFailureListener(e -> {
                        if (!isAdded() || getContext() == null) return;
                        Toast.makeText(getContext(), "Save failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    });
        });

        regenerateButton.setOnClickListener(v -> {
            promptInput.setText(lastPrompt);
            generateButton.performClick();
        });

        return view;
    }
}
