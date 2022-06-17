package im.zego.easyexample.android.cloudmessage;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import androidx.annotation.NonNull;
import com.blankj.utilcode.util.ActivityUtils;
import com.blankj.utilcode.util.AppUtils;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import im.zego.example.ringtone.RingtoneManager;
import java.util.Map;


public class MyFirebaseMessagingService extends FirebaseMessagingService {

    private static final String TAG = "MyFirebaseMessaging";

    private Handler mainHandler = new Handler(Looper.getMainLooper());

    /**
     * be sure only send data messages for android , or android devices will not receive this callback when android
     * devices is in background or is killed. refer to : https://firebase.google.com/docs/cloud-messaging/android/receive#backgrounded
     */
    @Override
    public void onMessageReceived(@NonNull RemoteMessage remoteMessage) {
        Map<String, String> data = remoteMessage.getData();
        Log.d(TAG, "Message data payload: " + remoteMessage.getData());
        // Check if message contains a notification payload.
        if (remoteMessage.getNotification() != null) {
            Log.d(TAG, "Message Notification Body: " + remoteMessage.getNotification().getBody());
        }

        NotificationHelper.createNotificationChannel(this);

        CloudMessage cloudMessage = CloudMessage.parseFromMap(data);
        Log.d(TAG, "onMessageReceived,AppUtils.isEmpty(): " + ActivityUtils.getActivityList().isEmpty());
        Log.d(TAG, "onMessageReceived,AppUtils.isAppForeground(): " + AppUtils.isAppForeground());
        // when app is not start
        if (ActivityUtils.getActivityList().isEmpty()) {
            RingtoneManager.playRingTone(this);
            NotificationHelper.showNotification(this, cloudMessage);
        }else if(!AppUtils.isAppForeground()){
            // when app is background
            NotificationHelper.showNotification(this, cloudMessage);
        }

        mainHandler.post(new Runnable() {
            @Override
            public void run() {
                CloudMessageManager.getInstance().onMessageReceived(cloudMessage);
            }
        });
    }

    @Override
    public void onNewToken(@NonNull String token) {
        Log.d(TAG, "Refreshed token: " + token);
        // If you want to send messages to this application instance or
        // manage this apps subscriptions on the server side, send the
        // FCM registration token to your app server.
        sendRegistrationToServer(token);
    }

    private void sendRegistrationToServer(String token) {
        // TODO: Implement this method to send token to your app server.
    }


}
