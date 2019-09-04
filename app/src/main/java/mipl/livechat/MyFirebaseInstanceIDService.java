package mipl.livechat;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.FirebaseInstanceIdService;

public class MyFirebaseInstanceIDService extends FirebaseInstanceIdService {
    SharedPreferences sharedpreferences;
    public static final String MyPREFERENCES = "mypref";

    private static final String TAG = "MyFirebaseIIDService";

    @Override
    public void onTokenRefresh() {

        String refreshedToken =  FirebaseInstanceId.getInstance().getToken();

        Log.e("asd",refreshedToken.toString());
        Log.d(TAG, "Refreshed token:" + refreshedToken);
        sendRegistrationToServer(refreshedToken);
    }

    private void sendRegistrationToServer(String token) {
        // TODO: Implement this method to send token to your app server.
        Log.e("insert","test");
        SharedPrefManager.getInstance(getApplicationContext()).saveDeviceToken(token);
        storetoken(token);
    }

    public void storetoken(String tokenid)
    {
        Log.e("storeinsert",tokenid);
        sharedpreferences = getApplicationContext().getSharedPreferences(MyPREFERENCES, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedpreferences.edit();
        editor.putString("token","value");
        editor.commit();
    }
}