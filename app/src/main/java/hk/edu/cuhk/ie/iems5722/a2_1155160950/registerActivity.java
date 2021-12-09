package hk.edu.cuhk.ie.iems5722.a2_1155160950;


import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;



import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

/**
 * Created by leoymr on 2/4/17.
 *
 * 注册页面activity
 */

public class registerActivity extends AppCompatActivity implements View.OnClickListener {

    private static final String POST = "post request";

    private Button register;    //注册按钮
    private EditText editText_confirmPWD;   //确认密码
    private EditText editText_user_email;  //用户邮箱
    private EditText editText_user_name;  //用户名
    private EditText editText_user_pw;  //用户密码

    private String user_email;
    private String user_name;
    private String user_pw;

    //正则表达式验证邮箱、用户名和密码
    private static Pattern pattern;
    private static Matcher matcher;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        setTitle("注册");

       // AndroidBug5497Workaround.assistActivity(this);

        register = (Button) findViewById(R.id.registerBtn);
        editText_confirmPWD = (EditText) findViewById(R.id.confirmPWD);
        editText_user_email = (EditText) findViewById(R.id.registerId);
        editText_user_name = (EditText) findViewById(R.id.registerName);
        editText_user_pw = (EditText) findViewById(R.id.registerPWD);

        register.setOnClickListener(this);

    }

    @Override
    public void onClick(View v) {
        user_email = editText_user_email.getText().toString();
        user_name = editText_user_name.getText().toString();
        user_pw = editText_user_pw.getText().toString();
        String conf_pwd = editText_confirmPWD.getText().toString();

        switch (v.getId()) {
            case R.id.registerBtn:
                if (user_email.equals("")) {
                    //邮箱不为空
                    Toast.makeText(registerActivity.this, "账号不能为空", Toast.LENGTH_SHORT).show();
                } else if (!isValidEmail(user_email)) {
                    //验证邮箱
                    Toast.makeText(registerActivity.this, "请输入正确的邮箱", Toast.LENGTH_SHORT).show();
                } else if (user_name.equals("")) {
                    //用户名不为空
                    Toast.makeText(registerActivity.this, "用户名不能为空", Toast.LENGTH_SHORT).show();
                } else if (!isValidName(user_name)) {
                    //验证用户名
                    Toast.makeText(registerActivity.this, "请输入不超过20位由数字、字母、下划线和连字符组成的用户名", Toast.LENGTH_SHORT).show();
                } else if (user_pw.equals("")) {
                    //密码不为空
                    Toast.makeText(registerActivity.this, "密码不能为空", Toast.LENGTH_SHORT).show();
                } else if (!isValidPwd(user_pw)) {
                    //验证密码
                    Toast.makeText(registerActivity.this, "请输入6～20位，包含字母与数字的密码", Toast.LENGTH_SHORT).show();
                } else if (conf_pwd.equals("")) {
                    //确认密码不为空
                    Toast.makeText(registerActivity.this, "确认密码不能为空", Toast.LENGTH_SHORT).show();
                } else if (!conf_pwd.equals(user_pw)) {
                    //判断确认密码是否输错
                    Toast.makeText(registerActivity.this, "两次密码输入不一致", Toast.LENGTH_SHORT).show();
                } else {
                    Log.d("user_id", user_email);
                    Log.d("user_pw", user_pw);
                    Log.d("user_name", user_name);

                    new RegisterAsyncTask().execute();

                }
                break;
        }
    }

    class RegisterAsyncTask extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... params) {
            return postRequestWithHttp();
        }

        @Override
        protected void onPostExecute(String status) {
            super.onPostExecute(status);
            if (status.equals("OK")) {
                Intent intent = new Intent(registerActivity.this, loginActivity.class);
                intent.putExtra("user_id", user_email);
                intent.putExtra("user_pwd", user_pw);
                Toast.makeText(registerActivity.this, "注册成功", Toast.LENGTH_SHORT).show();
                startActivity(intent);
            } else if (status.equals("ERROR") ) {
                Toast.makeText(registerActivity.this, "输入有误，请重新注册", Toast.LENGTH_SHORT).show();
            }
        }


        private String postRequestWithHttp() {
            JSONObject json = null;
            String status = "";
            String requestUrl = "http://34.125.154.205/api/project/register?user_name=%s&user_pw=%s&user_email=%s";
            URL url = null;
            requestUrl = String.format(requestUrl, user_name, user_pw,user_email);
            try {
                HttpURLConnection connection = null;
                url = new URL(requestUrl);
                connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("POST");// 设置可向服务器输出connection.setDoOutput(true);// 打开连接
                connection.connect();// 打开连接后，向服务端写要提交的参数// 参数格式：“name=asdasdas&age=123123”
                InputStream is = null;
                is = connection.getInputStream();
                String results = "";
                BufferedReader br = new BufferedReader(new InputStreamReader(is));
                results = br.readLine();
                json = new JSONObject(results);
                status = json.getString("status");
            } catch (Exception e) {
                e.printStackTrace();
                Log.d(POST, "POST request error");
            }
            return status;
        }
    }

    /**
     * 验证邮箱是否正确
     *
     * @param email
     * @return
     */
    private boolean isValidEmail(String email) {
        boolean flag = false;
        String REGEX_Email = "^[_A-Za-z0-9-]+(\\.[_A-Za-z0-9-]+)*@[A-Za-z0-9]+(\\.[A-Za-z0-9]+)*(\\.[A-Za-z]{2,})$";

        pattern = Pattern.compile(REGEX_Email);
        matcher = pattern.matcher(email);
        if (matcher.matches())
            flag = true;
        return flag;
    }

    /**
     * 验证用户名是否正确
     *
     * @param name
     * @return
     */
    private boolean isValidName(String name) {
        boolean flag = false;
        //匹配不超过20位由数字、字母、下划线和连字符组成的用户名
        String REGEX_pwd = "^[a-zA-Z0-9_-]{3,15}$";

        pattern = Pattern.compile(REGEX_pwd);
        matcher = pattern.matcher(name);
        if (matcher.matches())
            flag = true;
        return flag;
    }

    /**
     * 验证密码是否正确
     *
     * @param pwd
     * @return
     */
    private boolean isValidPwd(String pwd) {
        boolean flag = false;
        //匹配6～20位包含数字和字母的密码
        String REGEX_pwd = "^(?![0-9]+$)(?![a-zA-Z]+$)[0-9A-Za-z]{6,20}$";

        pattern = Pattern.compile(REGEX_pwd);
        matcher = pattern.matcher(pwd);
        if (matcher.matches())
            flag = true;
        return flag;
    }
}
