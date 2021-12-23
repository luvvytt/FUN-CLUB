package hk.edu.cuhk.ie.iems5722.a2_1155160950;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONException;
import org.json.JSONObject;

import java.net.URISyntaxException;

import io.socket.client.IO;
import io.socket.client.Socket;
import io.socket.emitter.Emitter;

public class TictactoeActivity extends AppCompatActivity {
    private String user_name;
    int[][] winPositions = {{0, 1, 2}, {3, 4, 5}, {6, 7, 8},
            {0, 3, 6}, {1, 4, 7}, {2, 5, 8},
            {0, 4, 8}, {2, 4, 6}};
    int[] gameState = {2, 2, 2, 2, 2, 2, 2, 2, 2};
    private Socket socket;
    TextView my_name;
    TextView opponent_name;
    TextView opponent_state;
    TextView my_state;
    boolean MyReadyState = false;
    boolean OpponentReadyState = false;
    boolean gameActive = false;
    boolean isMyTurn = true;
    public int counter = 0;



    public void playerTap(View view) {
        ImageView img = (ImageView) view;
        if (gameActive && isMyTurn){
            int tappedImage = Integer.parseInt(img.getTag().toString());
            if (socket != null) {
                try {
                    JSONObject json = new JSONObject();
                    json.put("user_name", user_name);
                    json.put("room_name","tictactoe_room");
                    json.put("index",tappedImage);
                    socket.emit("tap_event", json);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
            //img.setImageResource(R.drawable.cross);
        }
    }

    public void GameControl(){
        if (MyReadyState && OpponentReadyState){
            gameActive = true;
        }
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tictactoe);
        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setTitle("Tictactoe Game");
        Intent intent = getIntent();
        user_name = intent.getStringExtra("user_name");
        my_name = findViewById(R.id.my_name);
        opponent_name = findViewById(R.id.opponent_name);
        opponent_state = findViewById(R.id.opponent_state);
        my_state = findViewById(R.id.mystate);


        try {
            socket = IO.socket("http://34.125.154.205:5000/");
            socket.on(Socket.EVENT_CONNECT, onConnectSuccess);
            socket.on("update_roommenber",update_roommenber);
            socket.on("quit_roommenber",quit_roommenber);
            socket.on("update_ready_state",update_ready_state);
            socket.on("tap_update_UI",tap_update_UI);
            socket.on("post_winner",post_winner);
            socket.connect();
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }


        ((Button) findViewById(R.id.ReadyBtn)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                try {
                    JSONObject json = new JSONObject();
                    json.put("user_name", user_name);
                    json.put("room_name","tictactoe_room");
                    socket.emit("get_ready", json);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }


        });

    }

    @Override
    protected void onDestroy() {
        if (socket != null) {
            socket.disconnect();
            socket.off();
        }
        super.onDestroy();
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                if (socket != null) {
                    try {
                        JSONObject json = new JSONObject();
                        json.put("user_name", user_name);
                        json.put("room_name","tictactoe_room");
                        socket.emit("leave", json);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
                Intent parentActivityIntent = new Intent(this, NavigationActivity.class);
                parentActivityIntent.putExtra("user_name",user_name);
                parentActivityIntent.addFlags(
                        Intent.FLAG_ACTIVITY_CLEAR_TOP |
                                Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(parentActivityIntent);

        }
        return super.onOptionsItemSelected(item);
    }

    private Emitter.Listener onConnectSuccess = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    try {
                        JSONObject json = new JSONObject();
                        json.put("user_name",user_name);
                        json.put("room_name","tictactoe_room");
                        socket.emit("join", json);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            });
        }
    };

    private Emitter.Listener update_roommenber = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            try {
                JSONObject data = (JSONObject) args[0];
                String player_name = data.getString("user_name"); //回传姓名，如果是自己就把名字放在左边，否则放在右边
                if (player_name.equals(user_name)){
                    my_name.setText(player_name);
                }
                else {
                    opponent_name.setText(player_name);
                }

            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    };

    private Emitter.Listener quit_roommenber = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            try {
                JSONObject data = (JSONObject) args[0];
                String player_name = data.getString("user_name"); //退出时，广播退出者用户名，不是退出者的设置文字
                System.out.println(player_name);
                if (!player_name.equals(user_name) && !gameActive){
                    opponent_name.setText("No opponent...");
                    opponent_state.setText("Not ready");

                }

                else if (!player_name.equals(user_name) && gameActive){
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            AlertDialog alertDialog2 = new AlertDialog.Builder(TictactoeActivity.this)
                                    .setTitle("Opponent left!")
                                    .setMessage("Your opponent has left! Do you wanna play again?")
                                    .setIcon(R.mipmap.ic_launcher)
                                    .setPositiveButton("Play again", new DialogInterface.OnClickListener() {//添加"Yes"按钮
                                        @Override
                                        public void onClick(DialogInterface dialogInterface, int i) {
                                            counter = 0;
                                            Intent Refreshintent = new Intent(TictactoeActivity.this, TictactoeActivity.class);
                                            Refreshintent.putExtra("user_name",user_name);
                                            startActivity(Refreshintent);

                                            //传进来一个状态码，由状态码决定要怎么样发送api
                                        }
                                    })

                                    .setNegativeButton("Quit", new DialogInterface.OnClickListener() {//添加取消
                                        @Override
                                        public void onClick(DialogInterface dialogInterface, int i) {

                                            if (socket != null) {
                                                try {
                                                    JSONObject json = new JSONObject();
                                                    json.put("user_name", user_name);
                                                    json.put("room_name","tictactoe_room");
                                                    socket.emit("leave", json);
                                                } catch (JSONException e) {
                                                    e.printStackTrace();
                                                }
                                            }
                                            Intent parentActivityIntent = new Intent(TictactoeActivity.this, NavigationActivity.class);
                                            parentActivityIntent.putExtra("user_name",user_name);
                                            parentActivityIntent.addFlags(
                                                    Intent.FLAG_ACTIVITY_CLEAR_TOP |
                                                            Intent.FLAG_ACTIVITY_NEW_TASK);
                                            startActivity(parentActivityIntent);


                                        }
                                    })

                                    .create();
                            alertDialog2.show();
                        }
                    });
                }

            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    };

    private Emitter.Listener update_ready_state = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            try {
                JSONObject data = (JSONObject) args[0];
                String player_name = data.getString("user_name"); //回传姓名，如果是自己就把名字放在左边，否则放在右边
                if (player_name.equals(user_name)){
                    MyReadyState = true;
                    GameControl();
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            my_state.setText("Ready");
                        }
                    });
                }
                else{
                    OpponentReadyState = true;
                    GameControl();
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            opponent_state.setText("Ready");
                            opponent_name.setText(player_name);
                        }
                    });
                }

            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    };

    private Emitter.Listener tap_update_UI = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            try {
                JSONObject data = (JSONObject) args[0];
                String player_name = data.getString("user_name");
                counter ++;
                System.out.println("count:"+counter);
                int index = data.getInt("index");
                if (player_name.equals(user_name)){
                    if (isMyTurn) {
                        isMyTurn = false; //如果是自己更新了，要等对方更新了才能下下一步棋
                    }
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            if (gameState[index] == 2) {                                            // 0代表自己 1代表对手 2代表待定
                                int box_id = TictactoeActivity.this.getResources().getIdentifier("imageView" + index, "id", TictactoeActivity.this.getPackageName());
                                ImageView box = findViewById(box_id);
                                box.setImageResource(R.drawable.circle);
                                gameState[index] = 0;
                                for (int[] winPosition : winPositions) {
                                    if (gameState[winPosition[0]] == gameState[winPosition[1]] &&
                                            gameState[winPosition[1]] == gameState[winPosition[2]] &&
                                            gameState[winPosition[0]] != 2 && gameState[winPosition[0]] == 0) {  //判断自己是否取得了胜利
                                        if (socket != null) {
                                            try {
                                                JSONObject json = new JSONObject();
                                                json.put("user_name", user_name);
                                                json.put("room_name","tictactoe_room");
                                                json.put("number",counter);
                                                socket.emit("user_win", json);
                                            } catch (JSONException e) {
                                                e.printStackTrace();
                                            }
                                        }
                                    }
                                    else if (counter==9 && gameState[winPosition[0]] == 0) { //平局时只让一个人上报平局事件，避免重复触发
                                        if (socket != null) {
                                            try {
                                                JSONObject json = new JSONObject();
                                                json.put("user_name", "draw");
                                                json.put("room_name","tictactoe_room");
                                                json.put("number",counter);
                                                socket.emit("user_win", json);
                                            } catch (JSONException e) {
                                                e.printStackTrace();
                                            }
                                        }
                                    }
                                }


                            }
                        }
                    });

                }

                else {
                    if (!isMyTurn) {
                        isMyTurn = true; //对方的棋子更新
                    }
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            if (gameState[index] == 2) {
                                int box_id = TictactoeActivity.this.getResources().getIdentifier("imageView" + index, "id", TictactoeActivity.this.getPackageName());
                                ImageView box = findViewById(box_id);
                                box.setImageResource(R.drawable.cross);
                                gameState[index] = 1;


                            }
                        }
                    });
                }

            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    };

    private Emitter.Listener post_winner = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            try {
                JSONObject data = (JSONObject) args[0];
                String player_name = data.getString("user_name");
                int number = data.getInt("number");
                if (player_name.equals(user_name)){         //自己赢了
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                             AlertDialog alertDialog2 = new AlertDialog.Builder(TictactoeActivity.this)
                                    .setTitle("You win!")
                                    .setMessage(String.format("You win your opponent in only %d steps! Do you wanna play again?", number))
                                    .setIcon(R.mipmap.ic_launcher)
                                    .setPositiveButton("Play again", new DialogInterface.OnClickListener() {//添加"Yes"按钮
                                        @Override
                                        public void onClick(DialogInterface dialogInterface, int i) {
                                            counter = 0;
                                            Intent Refreshintent = new Intent(TictactoeActivity.this, TictactoeActivity.class);
                                            Refreshintent.putExtra("user_name",user_name);
                                            startActivity(Refreshintent);

                                            //传进来一个状态码，由状态码决定要怎么样发送api
                                        }
                                    })

                                    .setNegativeButton("Quit", new DialogInterface.OnClickListener() {//添加取消
                                        @Override
                                        public void onClick(DialogInterface dialogInterface, int i) {

                                            if (socket != null) {
                                                try {
                                                    JSONObject json = new JSONObject();
                                                    json.put("user_name", user_name);
                                                    json.put("room_name","tictactoe_room");
                                                    socket.emit("leave", json);
                                                } catch (JSONException e) {
                                                    e.printStackTrace();
                                                }
                                            }
                                            Intent parentActivityIntent = new Intent(TictactoeActivity.this, NavigationActivity.class);
                                            parentActivityIntent.putExtra("user_name",user_name);
                                            parentActivityIntent.addFlags(
                                                    Intent.FLAG_ACTIVITY_CLEAR_TOP |
                                                            Intent.FLAG_ACTIVITY_NEW_TASK);
                                            startActivity(parentActivityIntent);


                                        }
                                    })

                                    .create();
                            alertDialog2.show();
                        }
                    });
                }
                else if (player_name.equals("draw")){
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            AlertDialog alertDialog2 = new AlertDialog.Builder(TictactoeActivity.this)
                                    .setTitle("Draw game!")
                                    .setMessage(String.format("You have a draw game! Do you wanna play again?", number))
                                    .setIcon(R.mipmap.ic_launcher)
                                    .setPositiveButton("Play again", new DialogInterface.OnClickListener() {//添加"Yes"按钮
                                        @Override
                                        public void onClick(DialogInterface dialogInterface, int i) {

                                            Intent Refreshintent = new Intent(TictactoeActivity.this, TictactoeActivity.class);
                                            Refreshintent.putExtra("user_name",user_name);
                                            startActivity(Refreshintent);

                                            //传进来一个状态码，由状态码决定要怎么样发送api
                                        }
                                    })

                                    .setNegativeButton("Quit", new DialogInterface.OnClickListener() {//添加取消
                                        @Override
                                        public void onClick(DialogInterface dialogInterface, int i) {

                                            if (socket != null) {
                                                try {
                                                    JSONObject json = new JSONObject();
                                                    json.put("user_name", user_name);
                                                    json.put("room_name","tictactoe_room");
                                                    socket.emit("leave", json);
                                                } catch (JSONException e) {
                                                    e.printStackTrace();
                                                }
                                            }
                                            Intent parentActivityIntent = new Intent(TictactoeActivity.this, NavigationActivity.class);
                                            parentActivityIntent.putExtra("user_name",user_name);
                                            parentActivityIntent.addFlags(
                                                    Intent.FLAG_ACTIVITY_CLEAR_TOP |
                                                            Intent.FLAG_ACTIVITY_NEW_TASK);
                                            startActivity(parentActivityIntent);


                                        }
                                    })

                                    .create();
                            alertDialog2.show();
                        }
                    });
                }
                else{
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            AlertDialog alertDialog2 = new AlertDialog.Builder(TictactoeActivity.this)
                                    .setTitle("You Lose!")
                                    .setMessage(String.format("You lose in %d steps! Do you wanna play again?", number))
                                    .setIcon(R.mipmap.ic_launcher)
                                    .setPositiveButton("Play again", new DialogInterface.OnClickListener() {//添加"Yes"按钮
                                        @Override
                                        public void onClick(DialogInterface dialogInterface, int i) {

                                            Intent Refreshintent = new Intent(TictactoeActivity.this, TictactoeActivity.class);
                                            Refreshintent.putExtra("user_name",user_name);
                                            startActivity(Refreshintent);

                                            //传进来一个状态码，由状态码决定要怎么样发送api
                                        }
                                    })

                                    .setNegativeButton("Quit", new DialogInterface.OnClickListener() {//添加取消
                                        @Override
                                        public void onClick(DialogInterface dialogInterface, int i) {

                                            if (socket != null) {
                                                try {
                                                    JSONObject json = new JSONObject();
                                                    json.put("user_name", user_name);
                                                    json.put("room_name","tictactoe_room");
                                                    socket.emit("leave", json);
                                                } catch (JSONException e) {
                                                    e.printStackTrace();
                                                }
                                            }
                                            Intent parentActivityIntent = new Intent(TictactoeActivity.this, NavigationActivity.class);
                                            parentActivityIntent.putExtra("user_name",user_name);
                                            parentActivityIntent.addFlags(
                                                    Intent.FLAG_ACTIVITY_CLEAR_TOP |
                                                            Intent.FLAG_ACTIVITY_NEW_TASK);
                                            startActivity(parentActivityIntent);


                                        }
                                    })

                                    .create();
                            alertDialog2.show();
                        }
                    });
                }

            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    };
}