package hk.edu.cuhk.ie.iems5722.a2_1155160950;

import android.annotation.SuppressLint;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.os.Build;
import android.util.Log;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.sql.SQLOutput;

public class MyFirebaseMessagingService extends FirebaseMessagingService {
    //private static final String TAG = "MyFirebaseMessagingService";
 // Other overridden methods
    @Override
    public void onNewToken(String token) {
        Log.d("TAG", "Refreshed token " + token);
        sendRegistrationToServer(token);
    }

    private void sendRegistrationToServer(String token) {
        int user_id = 1155160950;
        String requestUrl = "http://34.125.154.205/api/a4/submit_push_token?user_id=%d&token=%s" ;
        requestUrl = String.format(requestUrl, user_id,token);

        System.out.println(requestUrl);
        try {
            URL url = new URL(requestUrl);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("POST");
            connection.connect();
            int response = connection.getResponseCode();
            System.out.println("responses"+response);
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void onMessageReceived(RemoteMessage remoteMessage) {
        String notificationId_no_payload = "notificationId_no_payload";
        String notificationId_with_payload = "notificationId_with_payload";
// Check if message contains a data payload.
        if (remoteMessage.getData().size() > 0) {
            //Log.d(TAG, "Message data payload: " + remoteMessage.getData());
            NotificationCompat.Builder builder = new NotificationCompat.Builder(this,notificationId_with_payload)
                    .setSmallIcon(R.drawable.cuhk)
                    .setContentTitle("chatroom"+remoteMessage.getNotification().getTitle())
                    .setContentText(remoteMessage.getNotification().getBody())
                    .setPriority(NotificationCompat.PRIORITY_DEFAULT);

            NotificationManager notificationManager =
                    (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

            // Since android Oreo notification channel is needed.
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                NotificationChannel channel = new NotificationChannel(notificationId_with_payload,
                        "Channel title",
                        NotificationManager.IMPORTANCE_DEFAULT);
                notificationManager.createNotificationChannel(channel);
            }

            notificationManager.notify(0 , builder.build());
        }
// Check if message contains a notification payload.
        if (remoteMessage.getNotification() != null) {
            //Log.d(TAG, "Message notification payload: " +
                    //remoteMessage.getNotification().getBody());
            NotificationCompat.Builder builder = new NotificationCompat.Builder(this,notificationId_no_payload)
                    .setSmallIcon(R.drawable.cuhk)
                    .setContentTitle("chatroom"+remoteMessage.getNotification().getTitle())
                    .setContentText(remoteMessage.getNotification().getBody())
                    .setPriority(NotificationCompat.PRIORITY_DEFAULT);

            NotificationManager notificationManager =
                    (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

            // Since android Oreo notification channel is needed.
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                NotificationChannel channel = new NotificationChannel(notificationId_no_payload,
                        "Channel title",
                        NotificationManager.IMPORTANCE_DEFAULT);
                notificationManager.createNotificationChannel(channel);
            }

            notificationManager.notify(1 , builder.build());
        }

    }
}
