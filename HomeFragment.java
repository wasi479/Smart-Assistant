package com.example.smartassistant;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;

public class HomeFragment extends Fragment {

    public HomeFragment() {

    }

    @Nullable
    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater,
            @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {

        View view =
                inflater.inflate(
                        R.layout.fragment_home,
                        container,
                        false);

        // CONNECT VIEWS



        MaterialButton openAiButton =
                view.findViewById(R.id.openAiButton);

        // ACCESS BOTTOM NAVIGATION FROM MAIN ACTIVITY

            BottomNavigationView bottomNavigation =
                    requireActivity()
                            .findViewById(R.id.bottomNavigation);

        // TASKS CARD CLICK



        // AI BUTTON CLICK

        openAiButton.setOnClickListener(v -> {

            bottomNavigation.setSelectedItemId(R.id.nav_ai);

        });

        return view;
    }
}
