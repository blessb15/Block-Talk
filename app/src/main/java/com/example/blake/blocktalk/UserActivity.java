package com.example.blake.blocktalk;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import butterknife.Bind;
import butterknife.ButterKnife;

public class UserActivity extends AppCompatActivity {
    @Bind(R.id.GetUser) TextView mGetUser;
    @Bind(R.id.MessagesView) ListView mMessagesView;
    private String[] messages = {"Bill: Sup erbody!?!", "Jim: nuttin much, homie g. hbu?", "Michaela: i like chicken enchilada's",
    "Blake: HAH. me too!!!!!", "Jim: whoever made this app is probably super rich, and owns like 50 cows...", "Blake: yeah, basically."};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user);
        ButterKnife.bind(this);
        Intent intent = getIntent();
        String username = intent.getStringExtra("username");

        mGetUser.setText("Hey, " + username + "!");

        ArrayAdapter adapter = new ArrayAdapter(this, android.R.layout.simple_list_item_1, messages);
        mMessagesView.setAdapter(adapter);



    }
}
