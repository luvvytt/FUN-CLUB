package hk.edu.cuhk.ie.iems5722.a2_1155160950;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

public class NavigationActivity extends AppCompatActivity {

    private String user_name;
    public static String MyName = "";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_navigation);
        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setTitle("Main Page");
        Intent intent = getIntent();
        user_name = intent.getStringExtra("user_name");
        MyName = user_name;


        Toast.makeText(NavigationActivity.this, "Welcome! " + user_name, Toast.LENGTH_SHORT).show();



        ((Button) findViewById(R.id.ChatroomBtn)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent chatroomintent = new Intent(NavigationActivity.this, MainActivity.class);
                chatroomintent.putExtra("user_name",user_name);
                startActivity(chatroomintent);
                }

        });
        ((Button) findViewById(R.id.ContactBtn)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent contactintent = new Intent(NavigationActivity.this, ContactActivity.class);
                contactintent.putExtra("user_name",user_name);
                startActivity(contactintent);
            }


        });
        ((Button) findViewById(R.id.GameBtn)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent contactintent = new Intent(NavigationActivity.this, TictactoeActivity.class);
                contactintent.putExtra("user_name",user_name);
                startActivity(contactintent);
            }


        });



    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                Intent parentActivityIntent = new Intent(this, loginActivity.class);
                parentActivityIntent.addFlags(
                        Intent.FLAG_ACTIVITY_CLEAR_TOP |
                                Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(parentActivityIntent);

        }
        return super.onOptionsItemSelected(item);
    }
}