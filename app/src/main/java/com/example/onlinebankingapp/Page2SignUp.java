package com.example.onlinebankingapp;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

public class Page2SignUp extends Fragment {

    private Spinner countryCodeSpinner;
    private EditText phoneNumberEditText;
    private TextInputEditText passwordEditText;
    private Button continueButton;
    private String firstName, lastName, email;
    private boolean isPhoneNumberValid = false;
    private boolean isPasswordValid = false;
    private FirebaseFirestore db = FirebaseFirestore.getInstance();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.signup_page2, container, false);

        if (getArguments() != null) {
            firstName = getArguments().getString("firstName");
            lastName = getArguments().getString("lastName");
            email = getArguments().getString("email");
        }

        continueButton = rootView.findViewById(R.id.continueButton);
        countryCodeSpinner = rootView.findViewById(R.id.countryCodeSpinner);
        phoneNumberEditText = rootView.findViewById(R.id.createPhoneNumber);
        passwordEditText = rootView.findViewById(R.id.createPassword);

        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(getActivity(),
                R.array.country_codes, R.layout.country_item);
        adapter.setDropDownViewResource(R.layout.country_item);
        countryCodeSpinner.setAdapter(adapter);

        continueButton.setEnabled(false);

        phoneNumberEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                validatePhoneNumber(s.toString().trim());
            }

            @Override
            public void afterTextChanged(Editable s) {
                updateContinueButtonState();
            }
        });

        passwordEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                validatePassword(s.toString().trim());
            }

            @Override
            public void afterTextChanged(Editable s) {
                updateContinueButtonState();
            }
        });

        continueButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String countryCode = countryCodeSpinner.getSelectedItem().toString();
                String phoneNumber = phoneNumberEditText.getText().toString().trim();
                String password = passwordEditText.getText().toString().trim();

                if (isPhoneNumberValid && isPasswordValid) {
                    checkIfUserExists(countryCode, phoneNumber, firstName, lastName, email, password);
                }
            }
        });

        return rootView;
    }

    private void validatePhoneNumber(String phoneNumber) {
        isPhoneNumberValid = phoneNumber.matches("[0-9]+");

        if (!isPhoneNumberValid) {
            phoneNumberEditText.setError("Phone number should contain numbers only");
        } else {
            phoneNumberEditText.setError(null);
        }
    }

    private void validatePassword(String password) {
        isPasswordValid = password.length() >= 8;
        if (!isPasswordValid) {
            passwordEditText.setError("Password should be at least 8 characters long");
        } else {
            passwordEditText.setError(null);
        }
    }

    private void updateContinueButtonState() {
        continueButton.setBackgroundTintList(ContextCompat.getColorStateList(getActivity(), R.color.darkpink));
        continueButton.setEnabled(isPhoneNumberValid && isPasswordValid);
    }

    private void checkIfUserExists(String countryCode, String phoneNumber, String firstName, String lastName, String email, String password) {
        db.collection("users")
                .whereEqualTo("countryCode", countryCode)
                .whereEqualTo("phoneNumber", phoneNumber)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        QuerySnapshot querySnapshot = task.getResult();
                        if (!querySnapshot.isEmpty()) {
                            Utility.showToast(getActivity(), "Phone number already exists!");
                        } else {
                            Bundle bundle = new Bundle();
                            bundle.putString("firstName", firstName);
                            bundle.putString("lastName", lastName);
                            bundle.putString("email", email);
                            bundle.putString("countryCode", countryCode);
                            bundle.putString("phoneNumber", phoneNumber);
                            bundle.putString("password", password);

                            Page3SignUp fragment = new Page3SignUp();
                            fragment.setArguments(bundle);

                            FragmentTransaction transaction = getParentFragmentManager().beginTransaction();
                            transaction.replace(R.id.fragment_container, fragment);
                            transaction.addToBackStack(null);
                            transaction.commit();
                        }
                    } else {
                        Utility.showToast(getActivity(), "Failed to check user existence: " + task.getException().getMessage());
                    }
                });
    }
}
