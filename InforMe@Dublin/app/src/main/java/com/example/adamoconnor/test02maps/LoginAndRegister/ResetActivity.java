package com.example.adamoconnor.test02maps.LoginAndRegister;

import android.content.DialogInterface;
import android.support.annotation.NonNull;
import android.support.v4.app.NavUtils;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.example.adamoconnor.test02maps.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;

public class ResetActivity extends AppCompatActivity {

    //declared inputs.
    private EditText emailField;

    // declare reset button.
    private Button resetButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reset);

        // action bar with back button.
        setupActionBar();

        //find view id for the design fields.
        emailField = (EditText) findViewById(R.id.mEmailField);
        resetButton = (Button) findViewById(R.id.resetButton);

        // when the reset button has been selected send to the forgotten password method.
        resetButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                // validate the form before processing.
                if(validateForm()) {

                    forgottenPassword();

                }
            }
        });

    }

    /**
     * setting up the navigation back button.
     * @param item
     * the image on whihc users select the back button.
     * @return
     * return the item to the options menu.
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == android.R.id.home) {
            NavUtils.navigateUpFromSameTask(this);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * setting up the top action bar.
     */
    private void setupActionBar() {
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            // Show the Up button in the action bar.
            actionBar.setDisplayHomeAsUpEnabled(true);

        }
    }

    /**
     * validate the form
     * @return
     * return the error if data wasn't entered.
     */
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

    /**
     * method used to send email to user with a
     * forgotten password.
     */
    public void forgottenPassword() {

        FirebaseAuth auth = FirebaseAuth.getInstance();
        final String emailAddress = emailField.getText().toString().trim();

        if(android.util.Patterns.EMAIL_ADDRESS.matcher(emailAddress).matches()) {
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
