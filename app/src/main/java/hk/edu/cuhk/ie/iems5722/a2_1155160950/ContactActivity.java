package hk.edu.cuhk.ie.iems5722.a2_1155160950;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class ContactActivity extends AppCompatActivity {
    private List<Friend> FriendList = new ArrayList<>();
    getFriendsListTask mTask;
    private String user_name;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_contact);
        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setTitle("Contact");
        Intent intent = getIntent();
        user_name = intent.getStringExtra("user_name");
        mTask = new getFriendsListTask();
        mTask.execute();

        ((Button) findViewById(R.id.NewFriendsBtn)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent addnewfriendintent = new Intent(ContactActivity.this, NewFriendsActivity.class);
                addnewfriendintent.putExtra("user_name",user_name);
                startActivity(addnewfriendintent);
            }

        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                Intent parentActivityIntent = new Intent(this, NavigationActivity.class);
                parentActivityIntent.putExtra("user_name",user_name);
                parentActivityIntent.addFlags(
                        Intent.FLAG_ACTIVITY_CLEAR_TOP |
                                Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(parentActivityIntent);

        }
        return super.onOptionsItemSelected(item);
    }

    private class getFriendsListTask extends AsyncTask<Void, Void, List<String>> {

        @Override
        protected void onPreExecute() {
            // 执行前显示提示
        }

        @Override
        protected List<String> doInBackground(Void... params) {
            List<String> Pre_friendlist = new ArrayList<>();
            String url = "http://34.125.154.205/api/project/get_friends_list?user_name=%s";

            URL requesturl = null;
            try {
                url = String.format(url, user_name);
                requesturl = new URL(url);
                HttpURLConnection conn = (HttpURLConnection) requesturl.openConnection();
                conn.setReadTimeout(10000); // 10,000 milliseconds
                conn.setConnectTimeout(15000); // 15,000 milliseconds
                conn.setRequestMethod("GET"); // Use the GET method
                conn.setDoInput(true);
// Start the query
                conn.connect();
                int response = conn.getResponseCode(); // This will be 200 if successful
                InputStream is = conn.getInputStream();
// Convert the InputStream into a string
                String results = "";
                BufferedReader br = new BufferedReader(new InputStreamReader(is));
                results = br.readLine();
                JSONObject json = new JSONObject(results);
                String status = json.getString("status");
                JSONArray array = json.getJSONArray("data");
                for (int i = 0; i < array.length(); i++) {
                    String user2 = array.getJSONObject(i).getString("user2");
                    Pre_friendlist.add(user2);
                }
            } catch (ProtocolException | JSONException e) {
                e.printStackTrace();
            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return Pre_friendlist;
        }

        protected void onPostExecute(List list) {
            ListView listView = (ListView) findViewById(R.id.friends_listview);
            final FriendAdapter friendAdapter = new FriendAdapter(ContactActivity.this, FriendList);
            listView.setAdapter(friendAdapter);
            for (int i = 0; i < list.size(); i = i + 1) {
                String name = (String) list.get(i);
                Friend friend = new Friend(name);
                FriendList.add(friend);
                friendAdapter.notifyDataSetChanged();



            }


        }



    }
}