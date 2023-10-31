package mobi.audax.tupi.passageiro.bin.util;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.TaskStackBuilder;
import android.content.Context;
import android.content.Intent;

import androidx.core.app.NotificationCompat;

import mobi.audax.tupi.passageiro.R;


public class Notification {

    public void showNotification(Context context, String title, String body, Intent intent) {
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        int notificationId = (int) (Math.random() * 1000);

        String VALOR_CONSTANTE = "android.support.sortKey";
        int SUMMARY_ID = 0;
        String channelId = "channel-01";
        String channelName = "Channel Name";
        int importance = NotificationManager.IMPORTANCE_HIGH;
        String GROUP_KEY_WORK_EMAIL = "com.mobi.audax.tupi.WORK_NOTIFICATION";

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O){
            NotificationChannel mChannel = new NotificationChannel(
                    channelId, channelName, importance);
            notificationManager.createNotificationChannel(mChannel);
        }

        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(context, channelId)
//                .setTicker(context.getString(R.string.nutcin_restaurante))
                .setColor(context.getResources().getColor(R.color.primary))
                .setSmallIcon(R.drawable.ic_stat)
//                .setWhen(System.currentTimeMillis())
                .setContentTitle(title)
                .setContentText(body)
                .setSortKey(VALOR_CONSTANTE)
                .setGroup(GROUP_KEY_WORK_EMAIL)
                .setAutoCancel(true);

//        NotificationCompat.InboxStyle style = new NotificationCompat.InboxStyle();
//        String[] descs = new String[]{"Descrição 1", "Descrição 2", "Descrição 3", "Descrição 4"};
//        for (int i = 0; i < descs.length; i++) {
//            style.addLine(descs[i]);
//        }
//        mBuilder.setStyle(style);

        android.app.Notification summaryNotification =
                new NotificationCompat.Builder(context, channelId)
                        .setContentTitle(title)
                        //set content text to support devices running API level < 24
                        .setContentText(body)
                        .setColor(context.getResources().getColor(R.color.primary))
                       .setSmallIcon(R.drawable.ic_stat)
                        //build summary info into InboxStyle template
//                        .setStyle(new NotificationCompat.InboxStyle()
//                                .addLine("Alex Faarborg  Check this out")
//                                .addLine("Jeff Chang    Launch Party")
//                                .setBigContentTitle("2 new messages")
//                                .setSummaryText("janedoe@example.com"))
                        //specify which group this notification belongs to
                        .setGroup(GROUP_KEY_WORK_EMAIL)
                        //set this notification as the summary for the group
                        .setGroupSummary(true)
                        .build();



        TaskStackBuilder stackBuilder = TaskStackBuilder.create(context);
        stackBuilder.addNextIntent(intent);
        PendingIntent resultPendingIntent = stackBuilder.getPendingIntent(
                0,
                PendingIntent.FLAG_IMMUTABLE
        );
        mBuilder.setContentIntent(resultPendingIntent);


        notificationManager.notify(notificationId, mBuilder.build());
        notificationManager.notify(SUMMARY_ID, summaryNotification);
    }
}
