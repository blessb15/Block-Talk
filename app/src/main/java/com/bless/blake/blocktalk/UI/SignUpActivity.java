package com.bless.blake.blocktalk.UI;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Typeface;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import com.bless.blake.blocktalk.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;

import butterknife.Bind;
import butterknife.ButterKnife;

public class SignUpActivity extends AppCompatActivity implements View.OnClickListener {
    @Bind(R.id.password) EditText mPasswordEditText;
    @Bind(R.id.comfirmPassword) EditText mConfirmPasswordEditText;
    @Bind(R.id.usernameSignUp) EditText mUsernameEditText;
    @Bind(R.id.emailSignUp) EditText mEmailEditText;
    @Bind(R.id.submitNewUser) Button mSubmitNewUser;
    @Bind(R.id.loginView) TextView mLoginView;

    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;
    private ProgressDialog mAuthProgressDialog;
    private String mUsername;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);
        ButterKnife.bind(this);
        Typeface font = Typeface.createFromAsset(getAssets(), "fonts/Poppins-Regular.ttf");
        mLoginView.setTypeface(font);

        mAuth = FirebaseAuth.getInstance();
        createAuthStateListener();
        createAuthProgressDialog();
        mSubmitNewUser.setOnClickListener(this);
        mLoginView.setOnClickListener(this);

    }

    @Override
    public void onStart() {
        super.onStart();
        mAuth.addAuthStateListener(mAuthListener);
    }

    @Override
    public void onStop() {
        super.onStop();
        if (mAuthListener != null) {
            mAuth.removeAuthStateListener(mAuthListener);
        }
    }

    @Override
    public void onClick(View view){
        if(view == mLoginView){
            Intent intent = new Intent(SignUpActivity.this, LogInActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        }

        if(view == mSubmitNewUser){
            createNewUser();
        }
    }

    private void createNewUser(){
        mUsername = mUsernameEditText.getText().toString().trim();
        final String email = mEmailEditText.getText().toString().trim();
        String password = mPasswordEditText.getText().toString().trim();
        String comfirmPassword = mConfirmPasswordEditText.getText().toString().trim();

        boolean validEmail = isValidEmail(email);
        boolean validPassword = isValidPassword(password,comfirmPassword);
        boolean validUsername = isValidUsername(mUsername);
        if( !validEmail || !validPassword || !validUsername) return;

        mAuthProgressDialog.show();

        mAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {

            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {

                mAuthProgressDialog.dismiss();

                if (task.isSuccessful()) {
                    createFirebaseUserProfile(task.getResult().getUser());
                } else {
                    Toast.makeText(SignUpActivity.this, "Try again", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void createFirebaseUserProfile(final FirebaseUser user){

        UserProfileChangeRequest addProfileName = new UserProfileChangeRequest.Builder()
                .setDisplayName(mUsername)
                .build();

        user.updateProfile(addProfileName)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if(task.isSuccessful()) {
                        }
                    }
                });
    }

    private void createAuthStateListener(){
        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                final FirebaseUser user = firebaseAuth.getCurrentUser();
                if(user != null) {
                    Intent intent = new Intent(SignUpActivity.this, MainActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                    finish();
                }
            }
        };
    }

    private void createAuthProgressDialog(){
        mAuthProgressDialog = new ProgressDialog(this);
        mAuthProgressDialog.setTitle("loading...");
        mAuthProgressDialog.setMessage("Checking with database");
        mAuthProgressDialog.setCancelable(false);
    }

    private boolean isValidEmail(String email){
        boolean isGoodEmail =
                (email != null && Patterns.EMAIL_ADDRESS.matcher(email).matches());
        if(!isGoodEmail){
            mEmailEditText.setError("Please enter a valid email address");
            return false;
        }
        return isGoodEmail;
    }

    private boolean isValidUsername(String username) {
        if (username.equals("")) {
            mUsernameEditText.setError("Please enter a username");
            return false;
        }
        return true;
    }

    private boolean isValidPassword (String password, String confirmPassword) {
        if (password.length() < 6) {
            mPasswordEditText.setError("Please enter a password containg at least 6 characters");
            return false;
        } else if (!password.equals(confirmPassword)) {
            mPasswordEditText.setError("Passwords do not match");
            return false;
        }
        return true;
    }
}
