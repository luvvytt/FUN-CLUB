package hk.edu.cuhk.ie.iems5722.a2_1155160950;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.TextUtils;
import android.text.format.DateFormat;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AbsListView;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.NumberPicker;
import android.widget.Toast;

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
import java.net.URL;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class ChatActivity extends AppCompatActivity {
    private EditText textInput;
    //    private ArrayList<InputText> inputTextList = new ArrayList<>();
    chatroom_Task mTask;
    Send_message mSend;
    int roomid;
    private ArrayList<InputText> inputTextList = new ArrayList<>();
    private ArrayList<InputText> inputTextListTemp = new ArrayList<>();
    private ArrayList<InputText> inputTextListTemp2 = new ArrayList<>();
    private String user_name;

    private InputTextAdapter arrayAdapter;
    private ListView listView;
    private static final int INITIATE = 1;
    private static final int REFRESH = 2;
    private static final int PULL_UP_LOAD = 3;
    private int current_page = 1;



    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main_activity_actions, menu);
        return true;
    }


//    private List<Text> TextList=new ArrayList<>();
//    TextAdapter adapter =  new TextAdapter(ChatActivity.this,R.layout.text,TextList);
//    ListView listView=findViewById(R.id.chatroom_listview);


    protected void onCreate(Bundle savedInstanceState) {


        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);
        Intent intent = getIntent();
        roomid = intent.getIntExtra("roomid", 0);
        String roomname = intent.getStringExtra("roomname");
        user_name = intent.getStringExtra("user_name");

        textInput = findViewById(R.id.send_text);
        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setTitle(roomname);


        arrayAdapter = new InputTextAdapter(ChatActivity.this, inputTextList);
        listView = (ListView) findViewById(R.id.chatroom_listview);
        listView.setAdapter(arrayAdapter);
        mTask = new chatroom_Task();
        mTask.execute(roomid, INITIATE, 1);


        listView.setOnScrollListener(new AbsListView.OnScrollListener() {
            private chatroom_Task Load_more;
            int roomid2 = roomid;
            boolean isLoad;

            public void onScrollStateChanged(AbsListView absListView, int i) {
                switch (i) {
                    // 当不滚动时
                    case NumberPicker.OnScrollListener.SCROLL_STATE_IDLE:
                        // 判断滚动到底部
                        if (listView.getLastVisiblePosition() == (listView.getCount() - 1)) {
                        }
                        // 判断滚动到顶部

                        if (listView.getFirstVisiblePosition() == 0) {
                            current_page += 1;
                            Load_more = new chatroom_Task();
                            Load_more.execute(roomid, PULL_UP_LOAD, current_page);
                            listView.postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    listView.requestFocusFromTouch();
                                    listView.setSelection(5);
                                }
                            }, 500);

                        }

                        break;
                }

            }


            @Override
            public void onScroll(AbsListView absListView, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
                isLoad = (firstVisibleItem + visibleItemCount) == totalItemCount;
            }


        });


        ((ImageButton) findViewById(R.id.chatroom_send)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String text = textInput.getText().toString().trim();
                if (!TextUtils.isEmpty(text)) {
                    mSend = new Send_message();
                    String strroomid = String.valueOf(roomid);
                    mSend.execute(strroomid, text,user_name);
                    current_page = 1;

                    listView.setSelection(listView.getBottom()); //回到底部
                    textInput.setText("");

                } else {
                    Toast.makeText(ChatActivity.this, "you need to type in something!", Toast.LENGTH_SHORT).show();
                }

            }
        });


    }

    public String getTime() {
        String currentTime = DateFormat.format(" HH:mm", new Date()).toString();
        return (currentTime);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                current_page = 1;
                //NavUtils.navigateUpFromSameTask(this);//onSaveInstanceState

                // This is called when the Home (Up) button is pressed
                // in the Action Bar.
                Intent parentActivityIntent = new Intent(this, MainActivity.class);
                parentActivityIntent.addFlags(
                        Intent.FLAG_ACTIVITY_CLEAR_TOP |
                                Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(parentActivityIntent);

                //当点击不同的menu item 是执行不同的操作
            case R.id.refresh:
                System.out.println("currentpage" + roomid);
                chatroom_Task mTask2 = new chatroom_Task();
                mTask2.execute(roomid, REFRESH, 1);
                listView.setSelection(listView.getBottom()); //回到底部
                current_page = 1;

                // onBackPressed();

        }
        return super.onOptionsItemSelected(item);
    }

    private class chatroom_Task extends AsyncTask<Integer, Void, List<String>> {
        private int mode;

        @Override
        protected void onPreExecute() {
            // 执行前显示提示
        }

        @Override
        protected List<String> doInBackground(Integer... integers) {
            List<String> Pre_chatroomList = new ArrayList<>();
            String flag = "do";
            int total_pages = 1;
            try {
//               String url2 = "http://18.217.125.61/api/a3/get_messages?chatroom_id=%d&page=%d";
              String url2 = "http://34.125.154.205/api/a3/get_messages?chatroom_id=%d&page=%d";

                int chatroom_id = integers[0];
                mode = integers[1];
                int max_page = integers[2];
                int page = 1;
                String results = "";
                if (mode == 3) {
                    page = max_page;
                }
                url2 = String.format(url2, chatroom_id, page);
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


                JSONObject data = json.getJSONObject("data");
                total_pages = data.getInt("total_pages");
                if (page > total_pages) {
                    flag = "not_do";
                }
                Pre_chatroomList.add(flag);
                JSONArray array = data.getJSONArray("messages");
                for (int i = 0; i < array.length(); i++) {
                    String name = array.getJSONObject(i).getString("name");
                    String message_time = array.getJSONObject(i).getString("message_time");
                    String message = array.getJSONObject(i).getString("message");
                    Pre_chatroomList.add(name);
                    Pre_chatroomList.add(message);
                    Pre_chatroomList.add(message_time);

                }
            } catch (ProtocolException | JSONException e) {
                e.printStackTrace();
            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return Pre_chatroomList;
        }

        @Override
        protected void onPostExecute(List list) {
            if ((String) (list.get(0)) == "do") {
                if (mode == 1) {
                    for (int i = list.size() - 1; i > 1; i = i - 3) {
                        String time = (String) list.get(i);
                        String text = (String) list.get(i - 1);
                        String name = (String) list.get(i - 2);
                        InputText inputText = new InputText(text, time, name);
                        inputTextList.add(inputText);
                        arrayAdapter.notifyDataSetChanged();

                    }
                } else if (mode == 2) {
                    inputTextList.removeAll(inputTextList);
                    for (int i = list.size() - 1; i > 1; i = i - 3) {
                        String time = (String) list.get(i);
                        String text = (String) list.get(i - 1);
                        String name = (String) list.get(i - 2);
                        InputText inputText = new InputText(text, time, name);
                        inputTextList.add(inputText);
                        arrayAdapter.notifyDataSetChanged();
                    }
                } else if (mode == 3) {
                    for (int j = 0; j < inputTextList.size(); j++) {
                        inputTextListTemp.add(inputTextList.get(j));
                    }
                    inputTextList.clear();


                    for (int i = list.size() - 1; i > 1; i = i - 3) {
                        String time = (String) list.get(i);
                        String text = (String) list.get(i - 1);
                        String name = (String) list.get(i - 2);
                        InputText inputText = new InputText(text, time, name);
                        inputTextList.add(inputText);
                    }
                    inputTextList.addAll(inputTextListTemp);
                    inputTextListTemp.clear();
                    arrayAdapter.notifyDataSetInvalidated();




                }
            } else {
                Toast.makeText(ChatActivity.this, "it is the last page!", Toast.LENGTH_SHORT).show();
            }
        }


    }


    private class Send_message extends AsyncTask<String, Void, String> {

        @Override
        protected void onPreExecute() {

            // 执行前显示提示
        }

        @Override
        protected String doInBackground(String... strings) {
//            String requestUrl = "http://18.217.125.61/api/a3/send_message";
           String requestUrl = "http://34.125.154.205/api/a3/send_messages?chatroom_id=%s&user_id=%s&name=%s&message=%s";
            URL url = null;
            String status = null;

            try {

                HttpURLConnection connection = null;
//                StringBuilder sb = new StringBuilder();
                String chatroom_id = strings[0];
                String userid = "1155160950";
                String name = strings[2];
                String message = strings[1];
//
                requestUrl = String.format(requestUrl, chatroom_id, userid,name,message);
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
                System.out.println("statusss" + status);
                if (status.equals("OK")) {
                    mTask = new chatroom_Task();
                    mTask.execute(Integer.parseInt(chatroom_id), INITIATE, 1);


                }

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
}












