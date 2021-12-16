package hk.edu.cuhk.ie.iems5722.a2_1155160950;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.Toast;

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

public class NewFriendsActivity extends AppCompatActivity {
    private List<Friend> FriendList = new ArrayList<>();
    getFriendsListTask mTask;
    private String user_name;
    private EditText friendNameInput;
    private String Msg;
    private String status;
    sendAddFriendRequest sendTask;
    ConfirmFriendRequest confirmTask;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_friends);
        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setTitle("New friend request");
        Intent intent = getIntent();
        user_name = intent.getStringExtra("user_name");
        friendNameInput = findViewById(R.id.send_add_friend_text);

        mTask = new getFriendsListTask();
        mTask.execute();

        ((ImageButton) findViewById(R.id.send_add_friend_request)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String text = friendNameInput.getText().toString().trim();
                if (!TextUtils.isEmpty(text)) {
                    sendTask = new sendAddFriendRequest();
                    sendTask.execute(text);


                } else {
                    Toast.makeText(NewFriendsActivity.this, "you need to type in something!", Toast.LENGTH_SHORT).show();
                }
            }

        });

        final ListView listView = (ListView) findViewById(R.id.newfriend_listview);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
                Friend friend = FriendList.get(position);
                AlertDialog alertDialog2 = new AlertDialog.Builder(NewFriendsActivity.this)
                        .setTitle("Confirmation")
                        .setMessage(String.format("%s wants to add you!", friend.name))
                        .setIcon(R.mipmap.ic_launcher)
                        .setPositiveButton("Accept", new DialogInterface.OnClickListener() {//添加"Yes"按钮
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                confirmTask = new ConfirmFriendRequest();
                                confirmTask.execute("0",friend.name,user_name);
                                Intent Refreshintent = new Intent(NewFriendsActivity.this, NewFriendsActivity.class);
                                Refreshintent.putExtra("user_name",user_name);
                                startActivity(Refreshintent);

                                //传进来一个状态码，由状态码决定要怎么样发送api
                            }
                        })

                        .setNegativeButton("Reject", new DialogInterface.OnClickListener() {//添加取消
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                confirmTask = new ConfirmFriendRequest();
                                confirmTask.execute("1",friend.name,user_name);
                                Intent Refreshintent = new Intent(NewFriendsActivity.this, NewFriendsActivity.class);
                                Refreshintent.putExtra("user_name",user_name);
                                startActivity(Refreshintent);


                            }
                        })
                        .setNeutralButton("More details", new DialogInterface.OnClickListener() {//添加普通按钮
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                Toast.makeText(NewFriendsActivity.this, "More details", Toast.LENGTH_SHORT).show();
                            }
                        })
                        .create();
                alertDialog2.show();
//                Intent intent = new Intent(NewFriendsActivity.this,
//                        ChatActivity.class);
//                intent.putExtra("roomid",friend.name);
//                startActivity(intent);
            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                Intent parentActivityIntent = new Intent(this, ContactActivity.class);
                parentActivityIntent.putExtra("user_name",user_name);
                parentActivityIntent.addFlags(
                        Intent.FLAG_ACTIVITY_CLEAR_TOP |
                                Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(parentActivityIntent);

        }
        return super.onOptionsItemSelected(item);
    }

    private class sendAddFriendRequest extends AsyncTask<String, Void, String> {

        @Override
        protected void onPreExecute() {

            AlertDialog alertDialog1 = new AlertDialog.Builder(NewFriendsActivity.this)
                    .setTitle("Notice")//标题
                    .setMessage(Msg)//内容
                    .setIcon(R.mipmap.cuhk)//图标
                    .create();
            alertDialog1.show();

            // 执行前显示提示
        }

        @Override
        protected String doInBackground(String... strings) {

            String target_name = strings[0];

            String url = "http://34.125.154.205/api/project/send_add_friend_request?user_name=%s&target_name=%s";
            URL requesturl = null;
            try {
                url = String.format(url, user_name,target_name);
                System.out.println("url"+url);
                requesturl = new URL(url);
                HttpURLConnection conn = (HttpURLConnection) requesturl.openConnection();
                conn.setRequestMethod("POST"); // Use the GET method
// Start the query
                conn.connect();
                int response = conn.getResponseCode(); // This will be 200 if successful
                InputStream is = null;
                is = conn.getInputStream();
// Convert the InputStream into a string
                String results = "";
                BufferedReader br = new BufferedReader(new InputStreamReader(is));
                results = br.readLine();
                JSONObject json = null;
                json = new JSONObject(results);
                status = json.getString("status");
                Msg = json.getString("Msg");
            } catch (ProtocolException | JSONException e) {
                e.printStackTrace();
            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return Msg;
        }

        protected void onPostExecute (String... strings) {



        }
    }


    private class getFriendsListTask extends AsyncTask<Void, Void, List<String>> {

        @Override
        protected void onPreExecute() {
            // 执行前显示提示
        }

        @Override
        protected List<String> doInBackground(Void... params) {
            List<String> Pre_friendlist = new ArrayList<>();
            String url = "http://34.125.154.205/api/project/get_request_friends_list?user_name=%s";
            URL requesturl = null;
            try {
                url = String.format(url, user_name);
                System.out.println("url"+url);
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
//                String status = json.getString("status");
                JSONArray array = json.getJSONArray("data");
                for (int i = 0; i < array.length(); i++) {
                    String user1 = array.getJSONObject(i).getString("user1");
                    Pre_friendlist.add(user1);
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
            ListView listView = (ListView) findViewById(R.id.newfriend_listview);
            final FriendAdapter friendAdapter = new FriendAdapter(NewFriendsActivity.this, FriendList);
            listView.setAdapter(friendAdapter);
            for (int i = 0; i < list.size(); i = i + 1) {
                String name = (String) list.get(i);
                Friend friend = new Friend(name);
                FriendList.add(friend);
                friendAdapter.notifyDataSetChanged();


            }


        }



    }

    private class ConfirmFriendRequest extends AsyncTask<String, Void, String> {

        @Override
        protected void onPreExecute() {
            // 执行前显示提示
        }

        @Override
        protected String doInBackground(String... strings) {
            String url = "";
            String status_code = strings[0];
            String requester = strings[1];
            String receiver = strings[2];
            URL requesturl = null;

            if (status_code.equals("0")){
                url = "http://34.125.154.205/api/project/confirm_add_friend_request?status_code=%s&requester=%s&receiver=%s";
            }
            else{
                url = "http://34.125.154.205/api/project/confirm_add_friend_request?status_code=%s&requester=%s&receiver=%s";
            }

            try {
                url = String.format(url, status_code,requester,receiver);
                System.out.println("url"+url);
                requesturl = new URL(url);
                HttpURLConnection conn = (HttpURLConnection) requesturl.openConnection();
                conn.setReadTimeout(10000); // 10,000 milliseconds
                conn.setConnectTimeout(15000); // 15,000 milliseconds
                conn.setRequestMethod("GET"); // Use the GET method
                conn.setDoInput(true);
// Start the query
                conn.connect();
                int response = conn.getResponseCode(); // This will be 200 if successful


            } catch (ProtocolException e) {
                e.printStackTrace();
            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }

        protected void onPostExecute() {







            }


        }



    }




