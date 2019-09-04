package mipl.livechat;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.RingtoneManager;
import android.net.Uri;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

public class MyFirebaseMessagingService extends FirebaseMessagingService {

    private static final String TAG = "FirebaseMessageService";
    Bitmap bitmap;
    String TrueOrFlase;
    String message;
    Uri uri;
    public static final String MyRing = "MyPrefs";
    SharedPreferences sharedpreferences;
    /**
     * Called when message is received.
     *
     * @param remoteMessage Object representing the message received from Firebase Cloud Messaging.
     */
    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        // There are two types of messages data messages and notification messages. Data messages are handled
        // here in onMessageReceived whether the app is in the foreground or background. Data messages are the type
        // traditionally used with GCM. Notification messages are only received here in onMessageReceived when the app
        // is in the foreground. When the app is in the background an automatically generated notification is displayed.
        // When the user taps on the notification they are returned to the app. Messages containing both notification
        // and data payloads are treated as notification messages. The Firebase console always sends notification
        // messages. For more see: https://firebase.google.com/docs/cloud-messaging/concept-options
        //
        Log.d(TAG, "From: " + remoteMessage.getFrom());

        // Check if message contains a data payload.
        if (remoteMessage.getData().size() > 0) {
            Log.d(TAG, "Message data payload: " + remoteMessage.getData());
        }

        // Check if message contains a notification payload.
        if (remoteMessage.getNotification() != null) {
            Log.d(TAG, "Message Notification Body: " + remoteMessage.getNotification().getBody());
            message=remoteMessage.getData().get("message");
        }

        //The message which i send will have keys named [message, image, AnotherActivity] and corresponding values.
        //You can change as per the requirement.

        //message will contain the Push Message

        //imageUri will contain URL of the image to be displayed with Notification
       // String imageUri = remoteMessage.getData().get("image");
        //If the key AnotherActivity has  value as True then when the user taps on notification, in the app AnotherActivity will be opened.
        //If the key AnotherActivity has  value as False then when the user taps on notification, in the app MainActivity will be opened.
        TrueOrFlase = remoteMessage.getData().get("AnotherActivity");
        message=remoteMessage.getData().get("dataset");

        Log.d(TAG, "Message Notification Activity: " +TrueOrFlase);

        //To get a Bitmap image from the URL received
     //   bitmap = getBitmapfromUrl(imageUri);

        sendNotification(message, TrueOrFlase);
    }

    /**
     * Create and show a simple notification containing the received FCM message.
     */

    private void sendNotification(String messageBody, String TrueOrFalse) {

        Intent intent = new Intent(this, Login.class);
        intent.putExtra("AnotherActivity","True");
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0 /* Request code */, intent,
                PendingIntent.FLAG_ONE_SHOT);

       // Uri defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);

        /*sharedpreferences  = getApplicationContext().getSharedPreferences(MyRing,MODE_PRIVATE);

        String ringti = sharedpreferences.getString("ring","");

        //   uri=Uri.parse(ringti);

        if(!ringti.equals(""))
        {
            uri= Uri.parse(ringti);
        }
        else
        {
            uri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        }*/

        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this)
              //  .setLargeIcon(image)/*Notification icon image*/
                .setSmallIcon(R.drawable.icon)
//                .setLargeIcon(BitmapFactory.decodeResource(getResources(), R.drawable.icon))
                .setContentTitle(messageBody)
               //*Notification with Image*/
                .setAutoCancel(true)
//                .setSound(uri)
                .setContentIntent(pendingIntent);

        NotificationManager notificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        notificationManager.notify(0 /* ID of notification */, notificationBuilder.build());
    }

    /*
    *To get a Bitmap image from the URL received
    * */
}