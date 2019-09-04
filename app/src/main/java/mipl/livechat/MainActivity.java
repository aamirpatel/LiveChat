package mipl.livechat;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.util.Base64;
import android.util.Log;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.firebase.iid.FirebaseInstanceId;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MainActivity extends AppCompatActivity {

    private TabLayout tabLayout;
    private ViewPager viewPager;
    StringRequest stringRequest;
    String refreshedToken;
    SharedPreferences sharedpreferences;
    String user, Phone;
    int activeCount = 0, pendingCount = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        refreshedToken = FirebaseInstanceId.getInstance().getToken();

        sharedpreferences = getSharedPreferences(commonVariables.mypreference, Context.MODE_PRIVATE);
        user = sharedpreferences.getString(commonVariables.Name, "");
        Phone = sharedpreferences.getString(commonVariables.Phone, "");

        viewPager = (ViewPager) findViewById(R.id.viewpager);
        tabLayout = (TabLayout) findViewById(R.id.tabs);

        getChats();

        setupViewPager(viewPager);
        tabLayout.setupWithViewPager(viewPager);

        sendFCMToken();
    }

    public void sendFCMToken() {

        stringRequest = new StringRequest(Request.Method.POST, "https://mipl.co.in/NewLivechat/support/api/login_app.php",
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {

                        Log.i("response", response);

                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {

//                Toast.makeText(MainActivity.this, error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        }) {

            @Override
            public Map<String, String> getHeaders() {

                String auth = new String("admin:mipl@1234");
                byte[] data = auth.getBytes();
                String base64 = Base64.encodeToString(data, Base64.NO_WRAP);
                HashMap<String, String> headers = new HashMap<String, String>();
                headers.put("Authorization", "Basic " + base64);
                return headers;
            }

            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<String, String>();
                params.put("phone", Phone);
                params.put("username", sharedpreferences.getString(commonVariables.Name, ""));
                params.put("token", refreshedToken);

                return params;
            }
        };

        stringRequest.setRetryPolicy(new DefaultRetryPolicy(
                10000,
                3,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));

        RequestQueue requestQueue = Volley.newRequestQueue(MainActivity.this);
        requestQueue.add(stringRequest);
    }

    private void setupViewPager(ViewPager viewPager) {
        ViewPagerAdapter adapter = new ViewPagerAdapter(getSupportFragmentManager());
        adapter.addFragment(new FragActive(), "Active " + activeCount);
        adapter.addFragment(new FragPending(), "Pending " + pendingCount);
        viewPager.setAdapter(adapter);
    }

    class ViewPagerAdapter extends FragmentPagerAdapter {
        private final List<Fragment> mFragmentList = new ArrayList<>();
        private final List<String> mFragmentTitleList = new ArrayList<>();

        public ViewPagerAdapter(FragmentManager manager) {
            super(manager);
        }

        @Override
        public Fragment getItem(int position) {
            return mFragmentList.get(position);
        }

        @Override
        public int getCount() {
            return mFragmentList.size();
        }

        public void addFragment(Fragment fragment, String title) {
            mFragmentList.add(fragment);
            mFragmentTitleList.add(title);
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return mFragmentTitleList.get(position);
        }
    }


    public void getChats() {

        stringRequest = new StringRequest(Request.Method.POST, commonVariables.baseURL + "/restapi/chats",
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {

                        Log.i("response", response);

                        JSONObject reader = null;
                        try {
                            reader = new JSONObject(response);

                            JSONArray list = reader.getJSONArray("list");

                            for (int i = 0; i < list.length(); i++) {
                                JSONObject chatList = list.getJSONObject(i);

                                String status = chatList.getString("status");
                                if (status.equals("1")) {
                                    activeCount++;
                                } else if (status.equals("0")) {
                                    pendingCount++;
                                }
                            }

                            setupViewPager(viewPager);
                            tabLayout.setupWithViewPager(viewPager);

                        } catch (Exception e) {

                        }


                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {

//                Toast.makeText(MainActivity.this, error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        }) {

            @Override
            public Map<String, String> getHeaders() {

//                String auth = new String(user + ":" + password);
                String auth = new String("admin:mipl@1234");
                byte[] data = auth.getBytes();
                String base64 = Base64.encodeToString(data, Base64.NO_WRAP);
                HashMap<String, String> headers = new HashMap<String, String>();
                headers.put("Authorization", "Basic " + base64);
                return headers;
            }
        };

        stringRequest.setRetryPolicy(new DefaultRetryPolicy(
                10000,
                3,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));

        RequestQueue requestQueue = Volley.newRequestQueue(MainActivity.this);
        requestQueue.add(stringRequest);
    }
}