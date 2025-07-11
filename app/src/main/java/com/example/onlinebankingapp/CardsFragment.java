package com.example.onlinebankingapp;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.fragment.app.Fragment;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;

import java.util.Map;

public class CardsFragment extends Fragment {
    TextView cardNumberText, usernameText;
    UserManager userManager;

    public CardsFragment() {
        userManager = UserManager.getInstance();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.cards_fragment, container, false);

        cardNumberText = rootView.findViewById(R.id.cardNumber);
        usernameText = rootView.findViewById(R.id.username);
        loadUserData();
        return rootView;
    }

    private void loadUserData() {
        userManager.getUserData(new UserManager.UserDataCallback() {
            @Override
            public void onDataLoaded(Map<String, Object> userData) {
                if (userData != null && userData.containsKey("fullName") && userData.containsKey("cards")) {
                    String fullName = (String) userData.get("fullName");
                    usernameText.setText(fullName);
                    Map<String, Boolean> cards = (Map<String, Boolean>) userData.get("cards");
                    for (Map.Entry<String, Boolean> entry : cards.entrySet()) {
                        if (entry.getValue()) {
                            String cardId = entry.getKey();
                            userManager.getCardData(cardId, new UserManager.CardDataCallback() {
                                @Override
                                public void onCardLoaded(DocumentSnapshot cardData) {
                                    if (cardData.exists()) {
                                        String cardNumber = cardData.getString("cardNumber");
                                        cardNumberText.setText(formatCardNumber(cardNumber));
                                    } else {
                                        cardNumberText.setText("Card not found");
                                    }
                                }

                                @Override
                                public void onFailure(Exception e) {
                                    cardNumberText.setText("Error");
                                }
                            });
                        }
                    }
                } else {
                    usernameText.setText("User data not found");
                }
            }

            @Override
            public void onFailure(Exception e) {
                usernameText.setText("Error");
            }
        });
    }
    private String formatCardNumber(String cardNumber){
        if (cardNumber == null){
            return "";
        }
        return cardNumber.replaceAll("(.{4})", "$1 ").trim();
    }
}
