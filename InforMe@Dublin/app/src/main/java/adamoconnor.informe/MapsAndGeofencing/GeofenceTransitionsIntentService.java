package adamoconnor.informe.MapsAndGeofencing;

import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.BitmapFactory;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.SystemClock;
import android.preference.PreferenceManager;
import android.support.v4.app.NotificationCompat;
import android.text.TextUtils;
import android.util.Log;
import adamoconnor.informe.PostingInformationAndComments.InformationFlipActivity;
import adamoconnor.informe.R;
import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofenceStatusCodes;
import com.google.android.gms.location.GeofencingEvent;
import java.util.ArrayList;
import java.util.List;

public class GeofenceTransitionsIntentService extends IntentService {

    protected static final String TAG = "GeofenceTransitionsIS";

    // getting description of area.
    public static String description;

    public GeofenceTransitionsIntentService() {
        super(TAG);  // use TAG to name the IntentService worker thread
    }

    @Override
    protected void onHandleIntent(Intent intent) {

        // geofence event triggered when entering geofence.
        GeofencingEvent event = GeofencingEvent.fromIntent(intent);
        if (event.hasError()) {
            Log.e(TAG, "GeofencingEvent Error: " + event.getErrorCode());
        }

        // get the description of the geofence.
        description = getGeofenceTransitionDetails(event);

        // allow a delay for the notification.
        SystemClock.sleep(3000);

        // call showNotifiation method to send notification when entering geofence.
        showNotification(description);
    }

    private static String getGeofenceTransitionDetails(GeofencingEvent event) {

        // getting the code of the geofence.
        String transitionString = GeofenceStatusCodes.getStatusCodeString(event.getGeofenceTransition());

        // getting trigger id.
        List triggeringIDs = new ArrayList();
        for (Geofence geofence : event.getTriggeringGeofences()) {
            triggeringIDs.add(geofence.getRequestId());
        }

        // return the strings of the geofence.
        return String.format("%s| %s", transitionString, TextUtils.join(", ", triggeringIDs));
    }

    public void showNotification(String text) {

        // getting the preferences which the user has set.
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);

        //declare string
        String sound = null;

        //declare URI for sound.
        Uri notificationSound = null;

        //declare int for the vibration.
        int vibrationSet;

        // set the sound of the preferences.
        try {
            notificationSound = Uri.parse(preferences.getString("notifications_new_message_ringtone",sound));
        }catch (NullPointerException ex) {

        }

        if(notificationSound == null) {
            notificationSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);

        }


        //set the vibration.
        if(preferences.getBoolean("notifications_new_message_vibrate",true) == true) {
            vibrationSet = Notification.DEFAULT_VIBRATE;
        }else {
            vibrationSet = 0;
        }

        // 1. Create a NotificationManager
        NotificationManager notificationManager =
                (NotificationManager) this.getSystemService(Context.NOTIFICATION_SERVICE);
        String[] splited = text.split("\\|");
        // 2. Create a PendingIntent for MapsActivity
        Intent intent = new Intent(this, InformationFlipActivity.class);
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

        //create a thread.
        MyThread myThread = new MyThread();
        myThread.start();
    }

    final static String MY_ACTION = "MY_ACTION";

    /**
     * create thread to send the data to the MapsActivity to populate dialog etc.
     */
    private class MyThread extends Thread{

        @Override
        public void run() {

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