package hk.edu.cuhk.ie.iems5722.a2_1155160950;


import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import okhttp3.Response;

/**
 * 登录界面activity
 */
public class loginActivity extends AppCompatActivity implements View.OnClickListener {
    private String user_name;

    private static final String TAG = "login";

    private String user_email;
    private String user_id_intent;
    private String user_pw;

    private Button login;
    private Button register;
    private EditText editText_user_email;
    private EditText editText_user_pw;

    private SharedPreferences preferences;
    private SharedPreferences.Editor editor;
    private CheckBox rememPwd;

    //正则表达式验证邮箱密码
    private static Pattern pattern;
    private static Matcher matcher;

    private boolean isGooglePlayAvailable = false;
    private String token = "";
    public String MyName = "";

    public loginActivity() {
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);


        setTitle("登录");

        Intent intent = getIntent();
        String account = "";
        String pwd = "";


        List<String> permissionList = new ArrayList<>();

        /* 提前询问权限，防止在主页面报错
        检测权限list，若无则添加 */
        if (ContextCompat.checkSelfPermission(loginActivity.this,
                android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            permissionList.add(android.Manifest.permission.ACCESS_FINE_LOCATION);
        }
        if (ContextCompat.checkSelfPermission(loginActivity.this,
                android.Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {
            permissionList.add(android.Manifest.permission.READ_PHONE_STATE);
        }
        if (ContextCompat.checkSelfPermission(loginActivity.this,
                android.Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            permissionList.add(android.Manifest.permission.WRITE_EXTERNAL_STORAGE);
        }
        if (!permissionList.isEmpty()) {
            String[] permissions = permissionList.toArray(new String[permissionList.size()]);
            ActivityCompat.requestPermissions(loginActivity.this, permissions, 1);
        }

        isGooglePlayAvailable = isGooglePlayServicesAvailable(this);
        Log.d("isGoogleService", String.valueOf(isGooglePlayAvailable));

        preferences = PreferenceManager.getDefaultSharedPreferences(this);

        rememPwd = (CheckBox) findViewById(R.id.remember_pwd);
        login = (Button) findViewById(R.id.loginBtn);
        register = (Button) findViewById(R.id.register_login);

        editText_user_email = (EditText) findViewById(R.id.loginId_edtxt);
        editText_user_pw = (EditText) findViewById(R.id.loginPwd_edtxt);

        boolean isRemember = preferences.getBoolean("remember_password", false);
        if (isRemember && (intent.getStringExtra("user_id") == null)) {
            account = preferences.getString("user_email", "");
            pwd = preferences.getString("user_pw", "");
            editText_user_email.setText(account);
            editText_user_pw.setText(pwd);
            rememPwd.setChecked(true);
        } else if (intent.getStringExtra("user_id") != null) {
            account = intent.getStringExtra("user_id");
            pwd = intent.getStringExtra("user_pwd");

            Log.d("register", account);
            editText_user_email.setText(account);
            editText_user_pw.setText(pwd);
        }

        //获取token
        //token = FirebaseMessaging.getInstance().getToken().getResult();
        token = "token";
        System.out.println("token"+token);

        login.setOnClickListener(this);
        register.setOnClickListener(this);

    }

    /**
     * 用于第一次安装apk，加载firebase获取的token id
     * @return
     */
    public String load() {
        FileInputStream in = null;
        BufferedReader reader = null;
        StringBuilder content = new StringBuilder();
        try {
            in = openFileInput("token.txt");
            reader = new BufferedReader(new InputStreamReader(in));
            String line = "";
            while ((line = reader.readLine()) != null) {
                content.append(line);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        Log.d("token reader", content.toString());
        return content.toString();
    }

    public boolean isGooglePlayServicesAvailable(Activity activity) {
        GoogleApiAvailability googleApiAvailability = GoogleApiAvailability.getInstance();
        int status = googleApiAvailability.isGooglePlayServicesAvailable(activity);
        if (status != ConnectionResult.SUCCESS) {
            if (googleApiAvailability.isUserResolvableError(status)) {
                googleApiAvailability.getErrorDialog(activity, status, 2404).show();
            }
            return false;
        }
        return true;
    }

    /**
     * 响应登录，注册按钮点击事件
     * @param v
     */
    @Override
    public void onClick(View v) {
        user_email = editText_user_email.getText().toString();
        user_pw = editText_user_pw.getText().toString();
        switch (v.getId()) {
            case R.id.loginBtn:
                if (user_email.equals("") || user_pw.equals("")) {
                    //验证输入不为空
                    Toast.makeText(loginActivity.this, "账号或密码不能为空", Toast.LENGTH_SHORT).show();
                } else if (!isValidEmail(user_email)) {
                    //验证邮箱
                    Toast.makeText(loginActivity.this, "请输入正确的邮箱", Toast.LENGTH_SHORT).show();
                } else if (!isValidPwd(user_pw)) {
                    //验证密码
                    Toast.makeText(loginActivity.this, "请输入6～20位，包含字母与数字的密码", Toast.LENGTH_SHORT).show();
                } else {
                    //记住密码
                    editor = preferences.edit();
                    if (rememPwd.isChecked()) {
                        editor.putBoolean("remember_password", true);
                        editor.putString("user_email", user_email);
                        editor.putString("user_pw", user_pw);
                    } else {
                        editor.clear();
                    }
                    editor.apply();
                    new LoginAsyncTask().execute();
                }
                break;
            case R.id.register_login:
                Intent intent = new Intent(loginActivity.this, registerActivity.class);
                startActivity(intent);
                break;
        }
    }

    class LoginAsyncTask extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... params) {
            return postRequestWithHttp();
        }

        @Override
        protected void onPostExecute(String status) {
            super.onPostExecute(status);
            if (status.equals("OK")) {

                Intent intent = new Intent(loginActivity.this, NavigationActivity.class);
                intent.putExtra("user_name",user_name);



                startActivity(intent);

            } else if (status.equals("ERROR")) {
                Toast.makeText(loginActivity.this, "用户名或密码有错，请重新输入", Toast.LENGTH_SHORT).show();
            }
        }


        private String postRequestWithHttp() {
            Response response = null;
            String code = null;
            String status = "";
            JSONObject json = null;
            String requestUrl = "http://34.125.154.205/api/project/login?user_token=%s&user_pw=%s&user_email=%s";
            URL url = null;
            requestUrl = String.format(requestUrl, token, user_pw,user_email);
//            if (token != null) {
//                Log.d("Token: ", token);
//            } else {
//                token = load();
//            }
            try {
                HttpURLConnection connection = null;
                url = new URL(requestUrl);
                connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("GET");// 设置可向服务器输出connection.setDoOutput(true);// 打开连接
                connection.connect();// 打开连接后，向服务端写要提交的参数// 参数格式：“name=asdasdas&age=123123”
                InputStream is = null;
                is = connection.getInputStream();
                String results = "";
                BufferedReader br = new BufferedReader(new InputStreamReader(is));
                results = br.readLine();
                json = new JSONObject(results);
                status = json.getString("status");
                user_name = json.getString("user_name");
            } catch (Exception e) {
                e.printStackTrace();
            }
            return status;
        }
    }

    /**
     * 验证邮箱
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
     * 验证密码
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
