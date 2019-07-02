package com.example.opendataresource;

import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ActivityInfo;
import android.support.annotation.NonNull;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
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

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class LoginActivity extends AppCompatActivity implements View.OnClickListener, InternetStatusListener.OnlineOrOffline {


    @BindView(R.id.tvOfflineMode)
    TextView offlineTextView;
    @BindView(R.id.etUsername)
    EditText username;
    @BindView(R.id.etPassword)
    EditText password;
    @BindView(R.id.btLoginOrSignUp)
    Button signUpSignInButton;
    @BindView(R.id.tvLoginOrSignUp)
    TextView signUpSignInText;



    private FirebaseAuth firebaseAuth;
    LoginOrSignUp currentText = LoginOrSignUp.SIGN_IN;
    private IntentFilter intentFilter;
    private InternetStatusListener internetStatusListener;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        final ActionBar actionBar = getSupportActionBar();

        internetStatusListener = new InternetStatusListener();
        intentFilter = new IntentFilter("android.net.conn.CONNECTIVITY_CHANGE");

        registerReceiver(internetStatusListener, intentFilter);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);


        actionBar.setDisplayShowTitleEnabled(false);


        firebaseAuth = FirebaseAuth.getInstance();

        ButterKnife.bind(this);

        signUpSignInText.setOnClickListener(this);

    }


    @Override
    protected void onStart() {
        super.onStart();
        FirebaseUser currentUser = firebaseAuth.getCurrentUser();
        if (currentUser != null) {
            updateUI(currentUser);
        }
    }

    @OnClick(R.id.btLoginOrSignUp)
    public void loginOrSignUpClicked(final View view) {
        String usernameText = username.getText().toString();
        String passwordText = password.getText().toString();

        switch (currentText) {
            case SIGN_UP:

                if (usernameText.isEmpty()) {
                    username.setError(getString(R.string.emptyFieldError));
                } else if (passwordText.isEmpty()) {
                    password.setError(getString(R.string.emptyFieldError));
                } else if (isValidEmailId(usernameText.trim())) {
                    view.setClickable(false);
                    firebaseAuth.createUserWithEmailAndPassword(usernameText, passwordText)
                            .addOnCompleteListener(this, task -> {
                                if (task.isSuccessful()) {

                                    FirebaseUser user = firebaseAuth.getCurrentUser();
                                    updateUI(user);

                                } else {
                                    view.setClickable(true);

                                    Toast.makeText(LoginActivity.this, "Authentication failed.",
                                            Toast.LENGTH_SHORT).show();
                                }
                            });
                }
                break;

            case SIGN_IN:
                if (usernameText.isEmpty()) {
                    username.setError(getString(R.string.emptyFieldError));
                } else if (passwordText.isEmpty()) {
                    password.setError(getString(R.string.emptyFieldError));
                } else if (isValidEmailId(usernameText.trim())) {
                    view.setClickable(false);
                    firebaseAuth.signInWithEmailAndPassword(usernameText, passwordText).addOnSuccessListener(new OnSuccessListener<AuthResult>() {


                        @Override
                        public void onSuccess(AuthResult authResult) {
                            FirebaseUser user = firebaseAuth.getCurrentUser();
                            updateUI(user);
                        }


                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            view.setClickable(true);
                            Toast.makeText(LoginActivity.this, R.string.sthWrongText, Toast.LENGTH_SHORT).show();

                        }
                    });

                }
                break;
        }


    }

    private void updateUI(FirebaseUser user) {

        Intent intent = new Intent(LoginActivity.this, MainActivity.class);
        intent.putExtra("User", user);

        unregisterReceiver(internetStatusListener);


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
        if (v.getId() == R.id.tvLoginOrSignUp) {
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