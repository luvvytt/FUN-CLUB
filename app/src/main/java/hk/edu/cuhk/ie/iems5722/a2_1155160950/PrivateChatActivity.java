package hk.edu.cuhk.ie.iems5722.a2_1155160950;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.format.DateFormat;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

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
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import io.socket.client.IO;
import io.socket.client.Socket;
import io.socket.emitter.Emitter;
public class PrivateChatActivity extends AppCompatActivity {
    private String user_name;
    private String target_name;
    private Socket socket;
    private EditText input;
    private ArrayAdapter arrayAdapter;
    private ListView listView;
    Private_chat_Task sTask;
    Send_message sendTask;
    private ImageButton sendPrivateMsgBtn;
    private ArrayList<InputText> inputTextList = new ArrayList<>();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_private_chat);
        Intent intent = getIntent();
        user_name = intent.getStringExtra("user_name");
        target_name = intent.getStringExtra("target_name");
        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setTitle(target_name);
        sendPrivateMsgBtn = (ImageButton)findViewById(R.id.private_chat_send);
        arrayAdapter = new InputTextAdapter(PrivateChatActivity.this, inputTextList);
        listView = (ListView) findViewById(R.id.privare_chat_listview);
        listView.setAdapter(arrayAdapter);

        //socket io code
        input = (EditText) findViewById(R.id.private_chat_input);
        sTask = new Private_chat_Task();
        sTask.execute(user_name,target_name);


        try {
            socket = IO.socket("http://34.125.154.205:5000/");
            socket.on("update_message",onUpdateMessage);
            socket.connect();
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
        sendPrivateMsgBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String message = input.getText().toString().trim();
                sendTask = new Send_message();
                sendTask.execute(user_name,target_name,message);
                input.setText("");
                if (socket != null) {
                    try {
                        JSONObject json = new JSONObject();
                        json.put("sender", user_name);
                        json.put("receiver", target_name);
                        json.put("message", message);
                        socket.emit("send_message", json);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }


        });

    }

    private class Private_chat_Task extends AsyncTask<String, Void, List<InputText>> {

        @Override
        protected void onPreExecute() {
            // 执行前显示提示
        }

        @Override
        protected List<InputText> doInBackground(String... strings) {
            List<InputText> private_text_list = new ArrayList<>();
            try {
                String url2 = "http://34.125.154.205/api/project/get_private_chat_messages?sender=%s&receiver=%s";
                String sender = strings[0];
                String receiver = strings[1];
                String results = "";
                url2 = String.format(url2, sender, receiver);
                URL url = new URL(url2);
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
                BufferedReader br = new BufferedReader(new InputStreamReader(is));
                results = br.readLine();
                JSONObject json = new JSONObject(results);
                JSONArray data = json.getJSONArray("data");

                for (int i = 0; i < data.length(); i++) {
                    String name =  data.getJSONObject(i).getString("sender");
                    String input_text =  data.getJSONObject(i).getString("message");
                    String time =  data.getJSONObject(i).getString("message_time");
                    InputText inputText = new InputText(input_text, time, name);
                    inputTextList.add(inputText);
                }

            } catch (ProtocolException | JSONException e) {
                e.printStackTrace();
            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return inputTextList;
        }

        @Override
        protected void onPostExecute(List list) {
            arrayAdapter.notifyDataSetInvalidated();

        }


    }

    private class Send_message extends AsyncTask<String, Void, String> {

        @Override
        protected void onPreExecute() {

            // 执行前显示提示
        }

        @Override
        protected String doInBackground(String... strings) {
            String requestUrl = "http://34.125.154.205/api/project/send_private_messages?sender=%s&receiver=%s&message=%s";
            URL url = null;
            String status = null;

            try {
                HttpURLConnection connection = null;
                String sender = strings[0];
                String receiver = strings[1];
                String message = strings[2];

//
                requestUrl = String.format(requestUrl, sender,receiver,message);
                url = new URL(requestUrl);
                connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("POST");// 设置可向服务器输出connection.setDoOutput(true);// 打开连接
                connection.connect();// 打开连接后，向服务端写要提交的参数// 参数格式：“name=asdasdas&age=123123”
                int response = connection.getResponseCode();
                InputStream is = null;
                is = connection.getInputStream();
// Convert the InputStream into a string
                String results = "";
                BufferedReader br = new BufferedReader(new InputStreamReader(is));
                results = br.readLine();
                JSONObject json = null;
                json = new JSONObject(results);
                status = json.getString("status");
            } catch (ProtocolException | JSONException e) {
                e.printStackTrace();
            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return status;
        }
        protected void onPostExecute (String... strings) {
            arrayAdapter.notifyDataSetChanged();

        }
    }

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

    //socket io code
    @Override
    protected void onDestroy() {
        if (socket != null) {
            socket.disconnect();
            socket.off();
        }
        super.onDestroy();
    }


    private Emitter.Listener onUpdateMessage = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            try {
                JSONObject data = (JSONObject) args[0];
                String message = data.getString("message");
                String sender = data.getString("sender");
                String receiver = data.getString("receiver");
                String time = getTime();

                if (receiver.equals(user_name) || receiver.equals((target_name))){ //只有在广播事件中和自己相关的才进行显示
                    InputText inputText = new InputText(message, time, sender);
                    inputTextList.add(inputText);
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            arrayAdapter.notifyDataSetChanged();
                        }
                    });
                }

            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    };

    public String getTime() {
        String currentTime = DateFormat.format("HH:mm", new Date()).toString();
        return (currentTime);
    }

}