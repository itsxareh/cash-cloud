package com.example.onlinebankingapp;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

public class Page1SignUp extends Fragment {
    private EditText firstName, lastName, emailAddress;
    private Button continueButton;
    private TextView loginButton;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.signup_page1, container, false);

        continueButton = rootView.findViewById(R.id.continueButton);
        loginButton = rootView.findViewById(R.id.loginButton);
        firstName = rootView.findViewById(R.id.createFirstName);
        lastName = rootView.findViewById(R.id.createLastName);
        emailAddress = rootView.findViewById(R.id.createEmail);

        continueButton.setEnabled(false);

        firstName.addTextChangedListener(new SimpleTextWatcher() {
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                updateContinueButtonState();
            }
        });

        lastName.addTextChangedListener(new SimpleTextWatcher() {
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                updateContinueButtonState();
            }
        });

        emailAddress.addTextChangedListener(new SimpleTextWatcher() {
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                updateContinueButtonState();
            }
        });

        loginButton.setOnClickListener(v -> startActivity(new Intent(getActivity(), SignInActivity.class)));

        continueButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String fName = firstName.getText().toString().trim();
                String lName = lastName.getText().toString().trim();
                String email = emailAddress.getText().toString().trim();

                if (fName.isEmpty() || lName.isEmpty() || email.isEmpty()) {
                    Utility.showToast(getActivity(), "Please fill in all fields");
                    return;
                }

                // Pass data to next fragment
                Bundle bundle = new Bundle();
                bundle.putString("firstName", fName);
                bundle.putString("lastName", lName);
                bundle.putString("email", email);

                Page2SignUp fragment = new Page2SignUp();
                fragment.setArguments(bundle);

                FragmentTransaction transaction = getParentFragmentManager().beginTransaction();
                transaction.replace(R.id.fragment_container, fragment);
                transaction.addToBackStack(null);
                transaction.commit();
            }
        });

        return rootView;
    }

    private void updateContinueButtonState() {
        String firstNameInput = firstName.getText().toString().trim();
        String lastNameInput = lastName.getText().toString().trim();
        String emailInput = emailAddress.getText().toString().trim();

        boolean isFirstNameValid = isValidName(firstNameInput);
        boolean isLastNameValid = isValidName(lastNameInput);
        boolean isEmailValid = isValidEmail(emailInput);

        if (!isFirstNameValid) {
            firstName.setError("Invalid first name (should not contain numbers)");
        } else {
            firstName.setError(null);
        }

        if (!isLastNameValid) {
            lastName.setError("Invalid last name (should not contain numbers)");
        } else {
            lastName.setError(null);
        }

        if (!isEmailValid) {
            emailAddress.setError("Invalid email address");
        } else {
            emailAddress.setError(null);
        }

        boolean enableButton = isFirstNameValid && isLastNameValid && isEmailValid;
        continueButton.setBackgroundTintList(ContextCompat.getColorStateList(getActivity(), R.color.darkpink));
        continueButton.setEnabled(enableButton);
    }

    private boolean isValidName(String name) {
        return !name.isEmpty() && !name.matches(".*\\d.*");
    }

    private boolean isValidEmail(String email) {
        // Simple email validation using regex
        return android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches();
    }

    private abstract class SimpleTextWatcher implements android.text.TextWatcher {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            // Do nothing
        }

        @Override
        public void afterTextChanged(android.text.Editable s) {
            // Do nothing
        }
    }
}
