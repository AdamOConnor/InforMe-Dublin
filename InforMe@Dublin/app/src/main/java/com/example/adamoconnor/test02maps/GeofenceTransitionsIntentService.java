package com.example.adamoconnor.test02maps;

import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;;
import android.content.SharedPreferences;
import android.graphics.BitmapFactory;
import android.os.SystemClock;
import android.preference.PreferenceManager;
import android.support.v4.app.NotificationCompat;
import android.text.TextUtils;
import android.util.Log;
import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofenceStatusCodes;
import com.google.android.gms.location.GeofencingEvent;

import android.net.Uri;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Adam O'Connor on 08/02/2017.
 */

public class GeofenceTransitionsIntentService extends IntentService {
    protected static final String TAG = "GeofenceTransitionsIS";
    public static String description;

    public GeofenceTransitionsIntentService() {
        super(TAG);  // use TAG to name the IntentService worker thread
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        GeofencingEvent event = GeofencingEvent.fromIntent(intent);
        if (event.hasError()) {
            Log.e(TAG, "GeofencingEvent Error: " + event.getErrorCode());
        }
        description = getGeofenceTransitionDetails(event);
        SystemClock.sleep(4000);
        //sendInformation(description);
        showNotification(description);
    }

    private static String getGeofenceTransitionDetails(GeofencingEvent event) {

        String transitionString =
                GeofenceStatusCodes.getStatusCodeString(event.getGeofenceTransition());
        List triggeringIDs = new ArrayList();
        for (Geofence geofence : event.getTriggeringGeofences()) {
            triggeringIDs.add(geofence.getRequestId());
        }
        return String.format("%s| %s", transitionString, TextUtils.join(", ", triggeringIDs));
    }

    public void showNotification(String text) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        String test = null;
        Uri notificationSound;
        int vibrationSet;
        notificationSound = Uri.parse(preferences.getString("notifications_new_message_ringtone",test));
        if(preferences.getBoolean("notifications_new_message_vibrate",true) == true) {
            vibrationSet = Notification.DEFAULT_VIBRATE;
        }else {
            vibrationSet = 0;
        }


        // 1. Create a NotificationManager
        NotificationManager notificationManager =
                (NotificationManager) this.getSystemService(Context.NOTIFICATION_SERVICE);
        String[] splited = text.split("\\|");
        // 2. Create a PendingIntent for AllGeofencesActivity
        Intent intent = new Intent(this, InformationFlipActivity.class);//Information.class
        intent.putExtra("1", splited[1]);
        intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_CLEAR_TOP);//FLAG_UPDATE_CURRENT
        PendingIntent pendingNotificationIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent. FLAG_UPDATE_CURRENT);



        // 3. Create and send a notification
        Notification notification = new NotificationCompat.Builder(this)
                .setLargeIcon(BitmapFactory.decodeResource(this.getResources(), R.drawable.informe))
                .setSmallIcon(R.drawable.informe)
                .setContentTitle("Historic Site Entered")
                .setStyle(new NotificationCompat.BigTextStyle().bigText("Hey you have entered a site of historic significance - "+splited[1]))
                .setContentIntent(pendingNotificationIntent)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setSound(notificationSound)
                .setDefaults(vibrationSet)
                .setAutoCancel(true)
                .build();

        notificationManager.notify(0, notification);

        MyThread myThread = new MyThread();
        myThread.start();
    }

    final static String MY_ACTION = "MY_ACTION";
    final static String INFORMATION_ACTION = "INFORMATION_ACTION";

    public class MyThread extends Thread{

        @Override
        public void run() {
            // TODO Auto-generated method stub
           // for(int i=0; i<10; i++){
                try {
                    Thread.sleep(3000);
                    String[] splited = description.split("\\|");
                    Intent intent = new Intent();
                    intent.setAction(MY_ACTION);
                    intent.putExtra("DATAPASSED", splited[1]);

                    sendBroadcast(intent);

                }catch(InterruptedException e){

                }
            stopSelf();
        }



    }

}