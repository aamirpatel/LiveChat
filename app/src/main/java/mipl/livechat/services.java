package mipl.livechat;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.IBinder;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.firebase.iid.FirebaseInstanceId;

import java.util.HashMap;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import static com.google.android.gms.internal.zzahg.runOnUiThread;

public class services extends Service {

    String user, password, Domain;
    SharedPreferences sharedpreferences;
    String refreshedToken;

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Override
    public void onStart(Intent intent, int startId) {
        super.onStart(intent, startId);
    }

    @Override
    public void onDestroy() {
        startService(new Intent(this, services.class));
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        Timer timer = new Timer();
        timer.schedule(new TimerTask() {
            public void run() {

                sharedpreferences = getSharedPreferences(commonVariables.mypreference, Context.MODE_PRIVATE);
                user = sharedpreferences.getString(commonVariables.Name, "");
                password = sharedpreferences.getString(commonVariables.Pass, "");
                Domain = sharedpreferences.getString(commonVariables.Domain, "");

                refreshedToken = FirebaseInstanceId.getInstance().getToken();

                try {
                    sendToken();
                } catch (Exception e) {
                }
            }
        }, 10000, 10000);

        return Service.START_STICKY;
    }

    public void sendToken() {
        StringRequest stringRequest = new StringRequest(Request.Method.POST, "http://demo.mahapage.com/Livechat_master/api/verify_login.php",
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {

                        try {
                            if (response.contains("Invalid User")) {
                            } else {

                                runOnUiThread(new Runnable() {
                                    public void run() {
                                        // UI code goes here
//                                        Toast.makeText(services.this, "hit", Toast.LENGTH_SHORT).show();
                                    }
                                });
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
            }
        }) {

            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<String, String>();
                params.put("username", user);
                params.put("password", password);
                params.put("website", Domain);
                params.put("device_token", refreshedToken);

                return params;
            }
        };

        stringRequest.setRetryPolicy(new DefaultRetryPolicy(
                10000,
                3,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));

        RequestQueue requestQueue = Volley.newRequestQueue(this);
        requestQueue.add(stringRequest);
    }
}
