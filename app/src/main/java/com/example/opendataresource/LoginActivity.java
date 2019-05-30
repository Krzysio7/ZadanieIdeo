package com.example.opendataresource;

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.support.annotation.NonNull;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.opendataresource.utils.InternetStatusListener;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.regex.Pattern;

public class LoginActivity extends AppCompatActivity implements View.OnClickListener, InternetStatusListener.OnlineOrOffline {

    private static final String TAG = "LoginActivity";
    private FirebaseAuth mAuth;
    private EditText username;
    private EditText password;
    LoginOrSignUp currentText = LoginOrSignUp.SIGN_IN;
    private Button signUpSignInButton;
    private TextView signUpSignInText;
    private TextView offlineTextView;
    private IntentFilter filter;
    private InternetStatusListener inter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        inter = new InternetStatusListener();
        filter = new IntentFilter("android.net.conn.CONNECTIVITY_CHANGE");
        registerReceiver(inter, filter);
        offlineTextView = findViewById(R.id.offlineView);
        username = findViewById(R.id.username);
        password = findViewById(R.id.password);
        signUpSignInButton = findViewById(R.id.loginButton);
        signUpSignInText = findViewById(R.id.loginOrSignUpText);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        final ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayShowTitleEnabled(false);
        mAuth = FirebaseAuth.getInstance();
        signUpSignInText.setOnClickListener(this);

    }


    @Override
    protected void onStart() {
        super.onStart();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            updateUI(currentUser);
        }
    }

    public void loginOrSignUpClicked(final View view) {
        String usernameText = username.getText().toString();
        String passwordText = password.getText().toString();

        switch (currentText) {
            case SIGN_UP:

                if (usernameText.isEmpty()) {
                    username.setError(getString(R.string.emptyFIeldError));
                } else if (passwordText.isEmpty()) {
                    password.setError(getString(R.string.emptyFIeldError));
                } else if (isValidEmailId(usernameText.trim())) {
                    view.setClickable(false);
                    mAuth.createUserWithEmailAndPassword(usernameText, passwordText)
                            .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                                @Override
                                public void onComplete(@NonNull Task<AuthResult> task) {
                                    if (task.isSuccessful()) {
                                        // Sign in success, update UI with the signed-in user's information
                                        Log.d(TAG, "createUserWithEmail:success");
                                        FirebaseUser user = mAuth.getCurrentUser();
                                        updateUI(user);
                                    } else {
                                        view.setClickable(true);
                                        // If sign in fails, display a message to the user.
                                        Log.w(TAG, "createUserWithEmail:failure", task.getException());
                                        Toast.makeText(LoginActivity.this, "Authentication failed.",
                                                Toast.LENGTH_SHORT).show();
//                                updateUI(null);
                                    }

                                    // ...
                                }
                            });
                }
                break;

            case SIGN_IN:
                view.setClickable(false);
                mAuth.signInWithEmailAndPassword(usernameText, passwordText).addOnSuccessListener(new OnSuccessListener<AuthResult>() {
                    @Override
                    public void onSuccess(AuthResult authResult) {
                        FirebaseUser user = mAuth.getCurrentUser();
                        updateUI(user);
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        view.setClickable(true);
                        Toast.makeText(LoginActivity.this, R.string.sthWrongText, Toast.LENGTH_SHORT).show();

                    }
                });


                break;
        }


    }

    private void updateUI(FirebaseUser user) {

        Intent intent = new Intent(LoginActivity.this, MainActivity.class);
        intent.putExtra("User", user);
        unregisterReceiver(inter);
        startActivity(intent);
        LoginActivity.this.finish();

        Toast.makeText(LoginActivity.this, user.getEmail(),
                Toast.LENGTH_SHORT).show();

    }

    private boolean isValidEmailId(String email) {
        if (Pattern.compile("^(([\\w-]+\\.)+[\\w-]+|([a-zA-Z]{1}|[\\w-]{2,}))@"
                + "((([0-1]?[0-9]{1,2}|25[0-5]|2[0-4][0-9])\\.([0-1]?"
                + "[0-9]{1,2}|25[0-5]|2[0-4][0-9])\\."
                + "([0-1]?[0-9]{1,2}|25[0-5]|2[0-4][0-9])\\.([0-1]?"
                + "[0-9]{1,2}|25[0-5]|2[0-4][0-9])){1}|"
                + "([a-zA-Z]+[\\w-]+\\.)+[a-zA-Z]{2,4})$").matcher(email).matches()) {
            return true;

        } else {
            username.setError(getString(R.string.emailErrorText));
            return false;
        }
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.loginOrSignUpText) {
            if (currentText == LoginOrSignUp.SIGN_IN) {
                signUpSignInText.setText(R.string.signInText);
                signUpSignInButton.setText(R.string.signUpText);
                currentText = LoginOrSignUp.SIGN_UP;
            } else {

                signUpSignInText.setText(R.string.signUpText);
                signUpSignInButton.setText(R.string.signInText);
                currentText = LoginOrSignUp.SIGN_IN;
            }
        }
    }

    @Override
    public void onOnline() {
        offlineTextView.setVisibility(View.GONE);
    }

    @Override
    public void onOffline() {
        offlineTextView.setVisibility(View.VISIBLE);

    }


    public enum LoginOrSignUp {

        SIGN_IN,
        SIGN_UP

    }

}