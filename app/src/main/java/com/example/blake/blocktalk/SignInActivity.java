package com.example.blake.blocktalk;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import butterknife.Bind;

public class SignInActivity extends AppCompatActivity {
    @Bind(R.id.submitSignIn) Button mSubmitSignIn;
    @Bind(R.id.usernameSignIn) EditText mUsernameSignIn;
    @Bind(R.id.passwordSignIn) EditText mPasswordSignIn;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_in);

        mSubmitSignIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String username = mUsernameSignIn.getText().toString();
                String password = mPasswordSignIn.getText().toString();
                Intent intent = new Intent(SignInActivity.this, UserActivity.class);
                intent.putExtra("username", username);
                intent.putExtra("password", password);
                startActivity(intent);
            }
        });
    }
}
