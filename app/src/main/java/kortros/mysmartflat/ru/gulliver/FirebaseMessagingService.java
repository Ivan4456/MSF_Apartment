package kortros.mysmartflat.ru.gulliver;

/**
 * Created by promobot on 23.01.2018.
 */
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.res.Resources;
import android.graphics.BitmapFactory;
import android.webkit.WebView;

import com.google.firebase.messaging.RemoteMessage;

import java.util.List;

import me.leolin.shortcutbadger.ShortcutBadger;

public class FirebaseMessagingService extends com.google.firebase.messaging.FirebaseMessagingService {

    final String LOG_TAG = "Logs_FireBase_Notification";

    public NotificationManager notificationManager;
    public Context ctx;

    SharedPreferences sPref;

    public static String getLauncherClassName(Context context) {
        PackageManager pm = context.getPackageManager();
        Intent intent = new Intent((Intent.ACTION_MAIN));
        intent.addCategory(Intent.CATEGORY_LAUNCHER);
        List<ResolveInfo> resolveInfos = pm.queryIntentActivities(intent,0);
        for (ResolveInfo resolveInfo : resolveInfos) {
            String pkgName = resolveInfo.activityInfo.applicationInfo.packageName;
            if (pkgName.equalsIgnoreCase(context.getPackageName())) {
                String className = resolveInfo.activityInfo.name;
                return  className;
            }
        }
        return null;
    }

    public  static void setBadge(Context context, int count) {

        String launcherClassName = getLauncherClassName(context);
        if (launcherClassName == null) {
            return;
        }

        Intent intent = new Intent("android.intent.action.BADGE_COUNT_UPDATE");
        intent.putExtra("badge_count",count);
        intent.putExtra("badge_count_package_name", context.getPackageName());
        intent.putExtra("badge_count_class_name", launcherClassName);

        context.sendBroadcast(intent);

        ShortcutBadger.applyCount(context, count);

    }

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage){
        String body=(remoteMessage.getData().get("body"));
        String title=remoteMessage.getData().get("title");
        String badge=remoteMessage.getData().get("badge");
        showNotification(title,body,badge);
    }


    private int getNotificationIcon() {
        boolean useWhiteIcon = (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP);
        return useWhiteIcon ? R.drawable.logo_3 : R.drawable.logo_2;
    }


    public  void onCreate() {
        super.onCreate();

        ctx = getApplicationContext();
    }
    private void showNotification(String title,String body,String badge) {


        Resources res = ctx.getResources();
        sPref = getSharedPreferences("SmartFlat_Settings", Context.MODE_PRIVATE);//MODE_WORLD_READABLE);

        int Badge_i = 0;
        String Message;
        SharedPreferences.Editor ed;
        int i;

        Intent intent5 = new Intent(this, Main.class);
        intent5.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pIntent5 = PendingIntent.getActivity(this, 0,  intent5, 0);

        try{
            Badge_i = Integer.parseInt(badge);
        } catch (NumberFormatException nfe) {
            System.out.println("Could not parse" + nfe);
        }


        setBadge(ctx, Badge_i);


        Notification.Builder builder = new Notification.Builder(this)
                .setTicker("Уведомление")
                .setContentTitle(title)
                .setContentText("Просмотреть сообщения:")
                .setSmallIcon(getNotificationIcon())



                .setNumber(Badge_i)
//									.setSound(Uri.parse("android.resource://com.alarmic.ps_mclub/raw/" + sound))
//                               	.setSound(Uri.parse("android.resource://com.alarmic.ps_reks/raw/" + sound))

                .addAction(0, "Запустить приложение", pIntent5).setAutoCancel(true);


        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {

            builder.setLargeIcon(BitmapFactory.decodeResource(res, R.drawable.logo)); // BitmapFactory.decodeResource(res, R.drawable.logo)

        } else {

            //builder.setLargeIcon(getNotificationIcon()); // BitmapFactory.decodeResource(res, R.drawable.logo)
        }

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
            builder.setColor(ctx.getResources().getColor(R.color.Black));
        }

        Notification.InboxStyle NotificationStyle = new Notification.InboxStyle(builder);


        i = Badge_i+1;
        if (i > 10) {
            i = 10;
        }
        while(i != 1){
            Message = sPref.getString("PUSH_" + String.valueOf(i-1),"");
            if(!Message.equalsIgnoreCase("")) {
                ed = sPref.edit();
                ed.putString("PUSH_" + String.valueOf(i),Message);
                ed.commit();
            }
            i--;

        }

        ed = sPref.edit();
        ed.putString("PUSH_" + String.valueOf(1),body);
        ed.commit();

        i = 1;
        while ((i <= 10) && (i <= Badge_i)) {

            Message = sPref.getString("PUSH_" + String.valueOf(i),"");

            if(!Message.equalsIgnoreCase("")) {

               NotificationStyle.addLine(Message);

            } else {
                i = 11;
            }
            i++;
        }


        NotificationStyle.setSummaryText("Непросмотренных уведомлений:");
        Notification notification = NotificationStyle.build();

        notification.flags |= (Notification.FLAG_AUTO_CANCEL);


        notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        notificationManager.notify(1, notification);


    }
}