package mipl.livechat;

import android.app.ProgressDialog;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;

public class commonVariables {

//    public static String baseURL = "http://demo.livehelperchat.com/";
    public static String baseURL = "https://mipl.co.in/NewLivechat/support/index.php";

    public static final String mypreference = "login";
    public static final String token = "token";
    public static final String UserID = "UserID";
    public static final String Name = "name";
    public static final String Pass = "Pass";
    public static final String Email = "email";
    public static final String Phone = "Phone";
    public static final String Location = "Location";
    public static final String Website = "Website";
    public static final String Domain = "Domain";
    public static final String role_id = "role_id";

    public static ProgressDialog showProgressDialog(Context context, String message) {
        ProgressDialog m_Dialog = new ProgressDialog(context);
        m_Dialog.setMessage(message);
        m_Dialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        m_Dialog.setCancelable(false);
        m_Dialog.show();
        return m_Dialog;
    }

    public static boolean isInternetAvailable(Context context)
    {
        ConnectivityManager cm = (ConnectivityManager)context. getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        if (netInfo != null && netInfo.isConnectedOrConnecting()) {
            return true;
        }
        Log.e("no","no");
        return false;
    }
}