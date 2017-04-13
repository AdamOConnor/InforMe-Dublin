package com.example.adamoconnor.test02maps.LoginAndRegister;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.example.adamoconnor.test02maps.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

/**
 * Created by Adam O'Connor on 13/04/2017.
 */

public class RegisterAccount extends Progress {

    private static final String TAG = "RegisterAccount";

    private EditText mNameField;
    private EditText mEmailField;
    private EditText mPasswordField;

    private Button mRegisterButton;

    private ProgressDialog mProgress;

    private FirebaseAuth mAuth;
    private DatabaseReference mDatabase;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.register_account);

        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance().getReference().child("users");

        mNameField = (EditText) findViewById(R.id.nameField);
        mEmailField = (EditText) findViewById(R.id.emailField);
        mPasswordField = (EditText) findViewById(R.id.passwordField);
        mRegisterButton = (Button) findViewById(R.id.registerButton);

        mRegisterButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startRegister();
            }
        });

    }

    private void startRegister() {

        final String name = mNameField.getText().toString().trim();
        mProgress = new ProgressDialog(RegisterAccount.this);
        mProgress.setMessage("Registering user "+name+" ...");
        final String email = mEmailField.getText().toString().trim();
        final String password = mPasswordField.getText().toString().trim();

            // [START create_user_with_email]
            mAuth.createUserWithEmailAndPassword(email, password)
                    .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            Log.d(TAG, "createUserWithEmail:onComplete:" + task.isSuccessful());
                            Log.d(TAG,"!!!!!!!!!!!!!!!!!!!!!!1110"+name+"   "+email+"   "+password);

                            if(task.isSuccessful()) {

                                String user_id = mAuth.getCurrentUser().getUid();

                                DatabaseReference currentUserDb = mDatabase.child("user_id");
                                currentUserDb.child("name").setValue(name);
                                currentUserDb.child("image").setValue("default");

                                mProgress.dismiss();

                                Toast.makeText(RegisterAccount.this, "Authentication successful",
                                        Toast.LENGTH_SHORT).show();
                                // [START_EXCLUDE]
                                Intent mainIntent = new Intent(RegisterAccount.this, EmailPasswordAuthentication.class);
                                mainIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                startActivity(mainIntent);

                            }
                            // If sign in fails, display a message to the user. If sign in succeeds
                            // the auth state listener will be notified and logic to handle the
                            // signed in user can be handled in the listener.
                            if (!task.isSuccessful()) {
                                Toast.makeText(RegisterAccount.this,  R.string.auth_failed+"Please check internet connection",
                                        Toast.LENGTH_SHORT).show();
                            }

                            // [END_EXCLUDE]
                        }
                    });
            // [END create_user_with_email]

    }
}
