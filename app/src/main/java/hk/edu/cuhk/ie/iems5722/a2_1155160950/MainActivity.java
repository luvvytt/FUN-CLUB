package hk.edu.cuhk.ie.iems5722.a2_1155160950;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;


public class MainActivity extends AppCompatActivity {
    MyTask mTask;
    private String user_name;
    private List<Chatroom> ChatroomList = new ArrayList<>();


    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        if(!isGooglePlayServicesAvailable(this)){
                Toast.makeText(getApplicationContext(), "Google Play service is not available",
                    Toast.LENGTH_SHORT).show();
        }

        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayOptions(ActionBar.DISPLAY_SHOW_HOME | ActionBar.DISPLAY_SHOW_TITLE);
        actionBar.setIcon(R.drawable.list);
        Intent intent = getIntent();
        user_name = intent.getStringExtra("user_name");
        mTask = new MyTask();
        mTask.execute();
        final ListView listView = (ListView) findViewById(R.id.mainactivity_listview);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
                Chatroom chatroom = ChatroomList.get(position);
                Intent intent = new Intent(MainActivity.this,
                        ChatActivity.class);
                intent.putExtra("roomid",chatroom.id);
                intent.putExtra("roomname",chatroom.text);
                intent.putExtra("user_name",user_name);


                startActivity(intent);
            }
        });

    }

    public void onResume() {
        super.onResume();
        if(!isGooglePlayServicesAvailable(this)){
            Toast.makeText(getApplicationContext(), "Google Play service is not available",
                    Toast.LENGTH_SHORT).show();
        }


    }


    private class MyTask extends AsyncTask<Void, Void, List<String>> {

        @Override
        protected void onPreExecute() {
            // 执行前显示提示
        }

        @Override
        protected List<String> doInBackground(Void... params) {
            List<String> Pre_chatroomList = new ArrayList<>();
            try {
//             URL url = new URL("http://18.217.125.61/api/a3/get_chatrooms");
               URL url = new URL("http://34.125.154.205/api/a3/get_chatrooms");

                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
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
                    String name = array.getJSONObject(i).getString("name");
                    String id = array.getJSONObject(i).getString("id");
                    Pre_chatroomList.add(name);
                    Pre_chatroomList.add(id);
                }
            } catch (ProtocolException | JSONException e) {
                e.printStackTrace();
            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
            System.out.println("assert"+Pre_chatroomList);
            return Pre_chatroomList;
        }

        protected void onPostExecute(List list) {

             ListView listView = (ListView) findViewById(R.id.mainactivity_listview);
            final ChatroomAdapter chatroomAdapter = new ChatroomAdapter(MainActivity.this, ChatroomList);
            listView.setAdapter(chatroomAdapter);
            for (int i = 0; i < list.size(); i = i + 2) {
                String text = (String) list.get(i);
                Integer id = Integer.parseInt((String) list.get(i + 1));
                Chatroom chatroom = new Chatroom(text, id);
                ChatroomList.add(chatroom);
                chatroomAdapter.notifyDataSetChanged();



            }


        }



    }
    public boolean isGooglePlayServicesAvailable(Activity activity) {
        GoogleApiAvailability googleApiAvailability = GoogleApiAvailability.getInstance();
        int status = googleApiAvailability.isGooglePlayServicesAvailable(activity);
        if (status != ConnectionResult.SUCCESS) {
            if (googleApiAvailability.isUserResolvableError(status)) {
                googleApiAvailability.getErrorDialog(activity, status, 9000).show();
            }
            return false;
        }
        return true;
    }
}






