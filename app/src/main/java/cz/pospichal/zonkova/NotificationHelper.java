package cz.pospichal.zonkova;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.support.v4.app.NotificationCompat;

class NotificationHelper {
    private static final NotificationHelper ourInstance = new NotificationHelper();

    static NotificationHelper getInstance() {
        return ourInstance;
    }

    private NotificationHelper() {
    }

    public void DrawNotification(Context context) {
        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(context.getApplicationContext(), "notify_001");
        Intent ii = new Intent(context.getApplicationContext(), MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, ii, 0);

        NotificationCompat.BigTextStyle bigText = new NotificationCompat.BigTextStyle();
        bigText.bigText("ZONKY");
        bigText.setBigContentTitle("Zkontrolujte nové půjčky na Zonky.cz");
        bigText.setSummaryText("Na Zonky.cz jsou nové půjčky které by stály za hřích.");

        mBuilder.setContentIntent(pendingIntent);
        mBuilder.setSmallIcon(R.drawable.ic_menu_white_24dp);
        mBuilder.setContentTitle("Zonky.cz");
        mBuilder.setContentText("Zonky.cz má nové půjčky na které by ses měl mrknout ;)");
        mBuilder.setPriority(Notification.PRIORITY_DEFAULT);
        mBuilder.setStyle(bigText);

        NotificationManager mNotificationManager =
                (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);


        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel("notify_001",
                    "Channel human readable title",
                    NotificationManager.IMPORTANCE_DEFAULT);
            mNotificationManager.createNotificationChannel(channel);
        }

        mNotificationManager.notify(0, mBuilder.build());
    }
}
