package mipl.livechat;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.PowerManager;
import android.provider.Settings;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Base64;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.firebase.iid.FirebaseInstanceId;
import com.rahimlis.badgedtablayout.BadgedTabLayout;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

public class Drower extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    private BadgedTabLayout tabLayout;
    private ViewPager viewPager;
    StringRequest stringRequest;
    String refreshedToken;
    SharedPreferences sharedpreferences;
    String user, password, Location, UserID, Domain, role_id;
    int activeCount = 0, pendingCount = 0, transferCount = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_drower);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        setTitle("Chat List");

        Window window = Drower.this.getWindow();

        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            window.setStatusBarColor(ContextCompat.getColor(Drower.this, R.color.statusbar));
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            Intent intent = new Intent();
            String packageName = getPackageName();
            PowerManager pm = (PowerManager) getSystemService(POWER_SERVICE);
            if (!pm.isIgnoringBatteryOptimizations(packageName)) {
                intent.setAction(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS);
                intent.setData(Uri.parse("package:" + packageName));
                startActivity(intent);
            }
        }

        refreshedToken = FirebaseInstanceId.getInstance().getToken();

        sharedpreferences = getSharedPreferences(commonVariables.mypreference, Context.MODE_PRIVATE);
        user = sharedpreferences.getString(commonVariables.Name, "");
        password = sharedpreferences.getString(commonVariables.Pass, "");
        Location = sharedpreferences.getString(commonVariables.Location, "");
        UserID = sharedpreferences.getString(commonVariables.UserID, "");
        role_id = sharedpreferences.getString(commonVariables.role_id, "");
        Domain = sharedpreferences.getString(commonVariables.Domain, "");

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(Drower.this, Drower.class));
//                finish();
            }
        });

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        View headerView = navigationView.getHeaderView(0);
        TextView navUsername = (TextView) headerView.findViewById(R.id.tvHeaderName);
        navUsername.setText("Hello " + user);

        viewPager = (ViewPager) findViewById(R.id.viewpager);
        tabLayout = (BadgedTabLayout) findViewById(R.id.tabs);

        if (commonVariables.isInternetAvailable(Drower.this)) {
            getChats();
        } else {
            Toast.makeText(Drower.this, "Internet Connection not available", Toast.LENGTH_SHORT).show();
        }

        setupViewPager(viewPager);
        tabLayout.setupWithViewPager(viewPager);

        getURL();

        try {
            startService(new Intent(this, services.class));
        } catch (Exception e) {
            e.printStackTrace();
        }

//        sendFCMToken();
//        getUserDetails();

        Timer timer = new Timer();
        timer.schedule(new TimerTask() {
            public void run() {
                if (commonVariables.isInternetAvailable(Drower.this)) {
                    getChats1();
                }
            }
        }, 15000, 15000);
    }

    private void setupViewPager(ViewPager viewPager) {
        ViewPagerAdapter adapter = new ViewPagerAdapter(getSupportFragmentManager());
        adapter.addFragment(new FragPending(), "Pending");// + pendingCount + "");
        adapter.addFragment(new FragActive(), "Active");// + activeCount + "");
        adapter.addFragment(new FragTransfer(), "Closed");// + transferCount + "");

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

        stringRequest = new StringRequest(Request.Method.POST, Location + "/index.php/restapi/chats?limit=1000",
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {

                        try {
                            pendingCount = 0;
                            activeCount = 0;
                            transferCount = 0;

                            if (response.contains("Authorization failed")) {
                                Toast.makeText(Drower.this, "Authorization failed", Toast.LENGTH_SHORT).show();
                            } else {

                                JSONObject reader = null;

                                reader = new JSONObject(response);

                                JSONArray list = reader.getJSONArray("list");

                                String a = String.valueOf(list.length());
                                Log.i("size", a);

                                for (int i = 0; i < list.length(); i++) {
                                    JSONObject chatList = list.getJSONObject(i);

                                    String status = chatList.getString("status");
                                    String user_id = chatList.getString("user_id");
                                    Log.i("Role", role_id);

                                    if (status.equals("1")) {

                                        if (role_id.equals("1")) {
                                            if (user_id.equals(UserID)) {
                                                activeCount++;
                                            }
                                        } else {
                                            activeCount++;
                                        }

                                    } else if (status.equals("0")) {
                                        pendingCount++;

                                    } else if (status.equals("2")) {

                                        if (role_id.equals("1")) {
                                            if (user_id.equals(UserID)) {
                                                transferCount++;
                                            }
                                        } else {
                                            transferCount++;
                                        }
                                    }
                                }


                            }

                            setupViewPager(viewPager);
                            tabLayout.setupWithViewPager(viewPager);

                            tabLayout.setBadgeText(0, String.valueOf(pendingCount));
                            tabLayout.setBadgeText(1, String.valueOf(activeCount));
                            tabLayout.setBadgeText(2, String.valueOf(transferCount));
                        } catch (Exception e) {

                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {

//                Toast.makeText(Drower.this, error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        }) {

            @Override
            public Map<String, String> getHeaders() {

                String auth = new String(user + ":" + password);
//                String auth = new String("admin:mipl@1234");
                byte[] data = auth.getBytes();
                String base64 = Base64.encodeToString(data, Base64.NO_WRAP);
                HashMap<String, String> headers = new HashMap<String, String>();
                headers.put("Authorization", "Basic " + base64);
                return headers;
            }
        };

        stringRequest.setRetryPolicy(new

                DefaultRetryPolicy(
                10000,
                3,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));

        RequestQueue requestQueue = Volley.newRequestQueue(Drower.this);
        requestQueue.add(stringRequest);
    }

    public void getChats1() {

        stringRequest = new StringRequest(Request.Method.POST, Location + "/index.php/restapi/chats?limit=1000",
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {

                        pendingCount = 0;
                        activeCount = 0;
                        transferCount = 0;

                        Log.i("response", response);

                        JSONObject reader = null;
                        try {
                            reader = new JSONObject(response);

                            JSONArray list = reader.getJSONArray("list");

                            String a = String.valueOf(list.length());
                            Log.i("size", a);

                            for (int i = 0; i < list.length(); i++) {
                                JSONObject chatList = list.getJSONObject(i);

                                String status = chatList.getString("status");
                                String user_id = chatList.getString("user_id");

                                if (status.equals("1")) {

                                    if (!UserID.equals("1")) {
                                        if (user_id.equals(UserID)) {
                                            activeCount++;
                                        }
                                    } else {
                                        activeCount++;
                                    }

                                } else if (status.equals("0")) {
                                    pendingCount++;

                                } else if (status.equals("2")) {

                                    if (!UserID.equals("1")) {
                                        if (user_id.equals(UserID)) {
                                            transferCount++;
                                        }
                                    } else {
                                        transferCount++;
                                    }
                                }
                            }

//                            setupViewPager(viewPager);
//                            tabLayout.setupWithViewPager(viewPager);

                            tabLayout.setBadgeText(0, String.valueOf(pendingCount));
                            tabLayout.setBadgeText(1, String.valueOf(activeCount));
                            tabLayout.setBadgeText(2, String.valueOf(transferCount));

                        } catch (Exception e) {

                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {

//                Toast.makeText(Drower.this, error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        }) {

            @Override
            public Map<String, String> getHeaders() {

                String auth = new String(user + ":" + password);
//                String auth = new String("admin:mipl@1234");
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

        RequestQueue requestQueue = Volley.newRequestQueue(Drower.this);
        requestQueue.add(stringRequest);
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
//            super.onBackPressed();
            new AlertDialog.Builder(this).setIcon(android.R.drawable.ic_dialog_alert).setTitle("Exit")
                    .setMessage("Are you sure?")
                    .setPositiveButton("yes", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {

                            Intent intent = new Intent(Intent.ACTION_MAIN);
                            intent.addCategory(Intent.CATEGORY_HOME);
                            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);//***Change Here***
                            startActivity(intent);
                            finish();
                            System.exit(0);

                        }
                    }).setNegativeButton("no", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                }
            }).show();
        }
    }

   /* @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.drower, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }*/

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_Logout) {

            new AlertDialog.Builder(this).setIcon(android.R.drawable.ic_dialog_alert).setTitle("Logout?")
                    .setMessage("Are you sure?")
                    .setPositiveButton("yes", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {

                            SharedPreferences.Editor editor = sharedpreferences.edit();
                            editor.clear();
                            editor.commit();

                            Intent i = new Intent(Drower.this, Login.class);
                            startActivity(i);
                            System.exit(0);

                        }
                    }).setNegativeButton("no", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                }
            }).show();

        } else if (id == R.id.nav_Privacy) {
            startActivity(new Intent(Drower.this, WebPages.class));

        } /*else if (id == R.id.nav_Terms) {
            final Dialog dialog = new Dialog(Drower.this);
            dialog.setContentView(R.layout.dialog);
            dialog.setTitle("Terms & conditions");

            Button dialogButton = (Button) dialog.findViewById(R.id.dialogButtonOK);
            // if button is clicked, close the custom dialog
            dialogButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    dialog.dismiss();
                }
            });
            dialog.show();

        }else if (id == R.id.nav_Offline) {
            startActivity(new Intent(Drower.this, ClosedChat.class));
        }*/

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);

        return true;
    }

    public void getUserDetails() {

        stringRequest = new StringRequest(Request.Method.POST, commonVariables.baseURL + "/index.php/restapi/getuser",
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {

                        Log.i("response", response);
                        JSONObject reader = null;
                        try {
                            reader = new JSONObject(response);

                            String status = reader.getString("error");
                            if (status.equals("false")) {

                                JSONObject jsonObject = reader.getJSONObject("result");
                                String id = jsonObject.getString("id");

                                SharedPreferences.Editor editor = sharedpreferences.edit();
                                editor.putString(commonVariables.UserID, id);

                                editor.commit();

                            } else {
                                Toast.makeText(Drower.this, "error", Toast.LENGTH_SHORT).show();
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {

//                Toast.makeText(Drower.this, error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        }) {

            @Override
            public Map<String, String> getHeaders() {

                String auth = new String(user + ":" + password);
//                String auth = new String("admin:mipl@1234");
                byte[] data = auth.getBytes();
                String base64 = Base64.encodeToString(data, Base64.NO_WRAP);
                HashMap<String, String> headers = new HashMap<String, String>();
                headers.put("Authorization", "Basic " + base64);
                return headers;
            }

            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<String, String>();
                params.put("username", sharedpreferences.getString(commonVariables.Name, ""));
                params.put("password", sharedpreferences.getString(commonVariables.Pass, ""));

                return params;
            }
        };

        stringRequest.setRetryPolicy(new DefaultRetryPolicy(
                10000,
                3,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));

        RequestQueue requestQueue = Volley.newRequestQueue(Drower.this);
        requestQueue.add(stringRequest);
    }


    public void getURL() {

        stringRequest = new StringRequest(Request.Method.POST, "http://demo.mahapage.com/Livechat_master/api/verify_login.php",
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {

                        if (response.contains("Invalid User")) {
                        } else {
                            try {
                                getLogin();
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.i("responce", error.toString());
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

        RequestQueue requestQueue = Volley.newRequestQueue(Drower.this);
        requestQueue.add(stringRequest);
    }

    public void getLogin() {

        stringRequest = new StringRequest(Request.Method.POST, Location + "/index.php/restapi/login",
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {

                        JSONObject reader = null;
                        try {
                            reader = new JSONObject(response);

                            String status = reader.getString("error");
                            if (status.equals("false")) {

                            } else {
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.i("responce", error.toString());
            }
        }) {

            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<String, String>();
                params.put("username", user);
                params.put("password", password);

                return params;
            }
        };

        stringRequest.setRetryPolicy(new DefaultRetryPolicy(
                10000,
                3,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));

        RequestQueue requestQueue = Volley.newRequestQueue(Drower.this);
        requestQueue.add(stringRequest);
    }
}