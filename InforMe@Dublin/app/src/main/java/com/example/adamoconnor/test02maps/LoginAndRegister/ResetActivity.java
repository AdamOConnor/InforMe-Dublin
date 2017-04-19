package com.example.adamoconnor.test02maps.LoginAndRegister;

import android.content.DialogInterface;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.example.adamoconnor.test02maps.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;

public class ResetActivity extends AppCompatActivity {

    private EditText emailField;
    private Button resetButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reset);

        emailField = (EditText) findViewById(R.id.mEmailField);
        resetButton = (Button) findViewById(R.id.resetButton);

        resetButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if(validateForm()) {
                    forgottenPassword();
                }

            }
        });

    }

    private boolean validateForm() {
        boolean valid = true;

        String email = emailField.getText().toString();
        if (TextUtils.isEmpty(email)) {
            emailField.setError("Required.");
            valid = false;
        } else {
            emailField.setError(null);
        }

        return valid;
    }


    public void forgottenPassword() {

        FirebaseAuth auth = FirebaseAuth.getInstance();
        final String emailAddress = String.valueOf(emailField.getText());
        android.util.Patterns.EMAIL_ADDRESS.matcher(emailAddress).matches();

        if(emailAddress != null && android.util.Patterns.EMAIL_ADDRESS.matcher(emailAddress).matches()) {
            auth.sendPasswordResetEmail(emailAddress)
                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()) {
                                new AlertDialog.Builder(ResetActivity.this)
                                        .setTitle("Password Reset")
                                        .setMessage("An Email has been sent to the following email address "+emailAddress)
                                        .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                                            public void onClick(DialogInterface dialog, int which) {


                                            }
                                        })
                                        .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                                            public void onClick(DialogInterface dialog, int which) {
                                                // do nothing
                                            }
                                        })
                                        .setIcon(R.drawable.informe4)
                                        .show();
                            }
                        }
                    });
        }
        else {
            new AlertDialog.Builder(ResetActivity.this)
                    .setTitle("Email Address")
                    .setMessage("Please enter a valid email address")
                    .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {


                        }
                    })
                    .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            // do nothing
                        }
                    })
                    .setIcon(R.drawable.informe4)
                    .show();
        }
    }

}
