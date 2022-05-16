package im.zego.easyexample.android.cloudmessage;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.util.Log;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationCompat.Builder;
import androidx.core.app.NotificationManagerCompat;
import com.blankj.utilcode.util.ActivityUtils;

public class NotificationHelper {

    private static final String TAG = "NotificationHelper";
    private static String CHANNEL_ID = "fcm_invite_msg";
    private static String CHANNEL_NAME = "invite_msg";
    private static String CHANNEL_DESC = "invite_msg";
    private static int notificationId = 9864;

    public static void showNotification(Context context, CloudMessage cloudMessage) {
        Log.d(TAG, "showNotification() called with: cloudMessage = [" + cloudMessage + "]");
        Class<?> cls = null;
        try {
            cls = Class.forName(ActivityUtils.getLauncherActivity());
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        if (cls == null) {
            return;
        }
        Intent appIntent = new Intent(context, cls);
        appIntent.setAction(Intent.ACTION_MAIN);
        appIntent.addCategory(Intent.CATEGORY_LAUNCHER);
//        appIntent.addFlags(Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT);

        PendingIntent pendingIntent;
        if (Build.VERSION.SDK_INT >= 23) {
            pendingIntent = PendingIntent.getActivity(context, 0, appIntent,
                PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_UPDATE_CURRENT);
        } else {
            pendingIntent = PendingIntent.getActivity(context, 0, appIntent,
                PendingIntent.FLAG_UPDATE_CURRENT);
        }

        String notificationTitle = "You received a " + cloudMessage.callType + " call";
        String notificationText = cloudMessage.callerUserName + " is calling you";
        Notification customNotification = new Builder(context, CHANNEL_ID)
            .setSmallIcon(im.zego.example.R.drawable.notification)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentTitle(notificationTitle)
            .setContentText(notificationText)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build();

        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
        notificationManager.notify(notificationId, customNotification);
    }

    public static void cancelNotification(Context context) {
        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
        notificationManager.cancel(notificationId);
    }

    public static void createNotificationChannel(Context context) {
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = CHANNEL_NAME;
            String description = CHANNEL_DESC;
            int importance = NotificationManager.IMPORTANCE_HIGH;
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
            channel.setDescription(description);
            // Register the channel with the system; you can't change the importance
            // or other notification behaviors after this
            NotificationManager notificationManager = context.getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }
}
