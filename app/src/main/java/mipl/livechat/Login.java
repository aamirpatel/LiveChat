package mipl.livechat;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
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

import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class Login extends AppCompatActivity {

    TextView tabRegistration, tabLogin;
    EditText etUserName, etPassword, etURL;
    EditText etName, etEmail, etContact, etCompanyName, etAddress, etDomain;
    Button btnLogin, btnRegister;
    LinearLayout llLogin, llRegistration, llTab;

    boolean status = true;
    String refreshedToken;
    String Message, StatusCode;

    ProgressDialog myDialog1, myDialog2;
    StringRequest stringRequest;

    SharedPreferences sharedpreferences;
    String website, support_location, username, password, user_id, role_id;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        requestWindowFeature(Window.FEATURE_NO_TITLE);
        this.getWindow().setFlags(
                WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_login);
        getSupportActionBar().hide();
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);

        sharedpreferences = getSharedPreferences(commonVariables.mypreference, Context.MODE_PRIVATE);

        try {
            String session = getIntent().getStringExtra("AnotherActivity");
            if (session.equals("True")) {
                startActivity(new Intent(Login.this, Drower.class));
                finish();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (sharedpreferences.contains(commonVariables.Name)) {
            startActivity(new Intent(this, Drower.class));
            finish();
        }

        etUserName = (EditText) findViewById(R.id.etUserName);
        etPassword = (EditText) findViewById(R.id.etPassword);
        etURL = (EditText) findViewById(R.id.etURL);
        btnLogin = (Button) findViewById(R.id.btnLogin);

        tabRegistration = (TextView) findViewById(R.id.tabRegistration);
        tabLogin = (TextView) findViewById(R.id.tabLogin);

        llRegistration = (LinearLayout) findViewById(R.id.llRegistration);
        llLogin = (LinearLayout) findViewById(R.id.llLogin);
        llTab = (LinearLayout) findViewById(R.id.llTab);

        etName = (EditText) findViewById(R.id.etName);
        etEmail = (EditText) findViewById(R.id.etEmail);
        etContact = (EditText) findViewById(R.id.etContact);
        etCompanyName = (EditText) findViewById(R.id.etCompanyName);
        etAddress = (EditText) findViewById(R.id.etAddress);
        etDomain = (EditText) findViewById(R.id.etDomain);
        btnRegister = (Button) findViewById(R.id.btnRegister);

        tabLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                tabRegistration.setTextColor(getResources().getColor(R.color.blue));
                tabLogin.setTextColor(getResources().getColor(R.color.white));

                tabRegistration.setBackgroundResource(R.color.white);
                tabLogin.setBackgroundResource(R.color.blue);

                llLogin.setVisibility(View.VISIBLE);
                llRegistration.setVisibility(View.GONE);
                tabRegistration.setBackgroundResource(R.drawable.customborder);
            }
        });

        tabRegistration.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                tabRegistration.setTextColor(getResources().getColor(R.color.white));
                tabLogin.setTextColor(getResources().getColor(R.color.blue));

                tabRegistration.setBackgroundResource(R.color.blue);
                tabLogin.setBackgroundResource(R.color.white);

                llLogin.setVisibility(View.GONE);
                llRegistration.setVisibility(View.VISIBLE);
                tabLogin.setBackgroundResource(R.drawable.customborder);
            }
        });

        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (commonVariables.isInternetAvailable(Login.this)) {
                    if (validationLogin()) {
//                    getLogin();
                        refreshedToken = FirebaseInstanceId.getInstance().getToken();
                        getURL();
                    } else {
                        status = true;
                    }
                } else {
                    Toast.makeText(Login.this, "Internet Connection not available", Toast.LENGTH_SHORT).show();
                }
            }
        });

        btnRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (validationRegistration()) {
                    sendRegistration();
                } else {
                    status = true;
                }

//                validationRegistration()?sendRegistration():status = true;
            }
        });
    }

    /* ***************   Registration  ************************************ */

    public void sendRegistration() {
        myDialog1 = commonVariables.showProgressDialog(Login.this, "Registering ...");

        stringRequest = new StringRequest(Request.Method.POST, "http://demo.mahapage.com/Livechat_master/api/user_registration.php",
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {

                        Log.i("response", response);

                        JSONObject reader = null;
                        try {
                            reader = new JSONObject(response);

                            Message = reader.getString("Message");
                            StatusCode = reader.getString("Status");

                            if (StatusCode.equals("200")){
                                Toast.makeText(Login.this, Message, Toast.LENGTH_SHORT).show();

                                etName.setText("");
                                etEmail.setText("");
                                etContact.setText("");
                                etCompanyName.setText("");
                                etAddress.setText("");
                                etDomain.setText("");

                            } else {
                                Toast.makeText(Login.this, Message, Toast.LENGTH_SHORT).show();
                            }

                        } catch (Exception e) {
                            e.printStackTrace();
                        }

                        myDialog1.dismiss();
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {

//                Toast.makeText(Login.this, error.getMessage(), Toast.LENGTH_SHORT).show();
                myDialog1.dismiss();
            }
        }) {

            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<String, String>();
                params.put("name", etName.getText().toString());
                params.put("email_id", etEmail.getText().toString());
                params.put("contact_no", etContact.getText().toString());
                params.put("company_name", etCompanyName.getText().toString());
                params.put("company_address", etAddress.getText().toString());
                params.put("domain_name", etDomain.getText().toString());

                return params;
            }
        };

        stringRequest.setRetryPolicy(new DefaultRetryPolicy(
                10000,
                3,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));

        RequestQueue requestQueue = Volley.newRequestQueue(Login.this);
        requestQueue.add(stringRequest);
    }

    /* ***************   Login  ************************************ */

    public void getURL() {
        myDialog1 = commonVariables.showProgressDialog(Login.this, "Loging ...");

//        stringRequest = new StringRequest(Request.Method.POST, "https://mipl.co.in/NewLivechat/support/api/verify_login.php",
        stringRequest = new StringRequest(Request.Method.POST, "http://demo.mahapage.com/Livechat_master/api/verify_login.php",
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {

                        Log.i("response", response);

                        if (response.contains("Invalid User")) {
                            Toast.makeText(Login.this, "Invalid user", Toast.LENGTH_SHORT).show();
                        } else {

                            JSONObject reader = null;
                            try {
                                reader = new JSONObject(response);

                                website = reader.getString("website");
                                support_location = reader.getString("support_location");
                                username = reader.getString("username");
                                password = reader.getString("password");
                                user_id = reader.getString("user_id");
                                role_id = reader.getString("role_id");

                                getLogin();
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }

                        myDialog1.dismiss();
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {

//                Toast.makeText(Login.this, error.getMessage(), Toast.LENGTH_SHORT).show();
                myDialog1.dismiss();
            }
        }) {

            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<String, String>();
                params.put("username", etUserName.getText().toString());
                params.put("password", etPassword.getText().toString());
                params.put("website", etURL.getText().toString());
                params.put("device_token", refreshedToken);

                return params;
            }
        };

        stringRequest.setRetryPolicy(new DefaultRetryPolicy(
                10000,
                3,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));

        RequestQueue requestQueue = Volley.newRequestQueue(Login.this);
        requestQueue.add(stringRequest);
    }


    public void getLogin() {
        myDialog2 = commonVariables.showProgressDialog(Login.this, "Loging ...");

        stringRequest = new StringRequest(Request.Method.POST, support_location + "index.php/restapi/login",
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {

                        Log.i("response", response);

                        JSONObject reader = null;
                        try {
                            reader = new JSONObject(response);

                            String status = reader.getString("error");
                            if (status.equals("false")) {

                                SharedPreferences.Editor editor = sharedpreferences.edit();
                                editor.putString(commonVariables.Name, etUserName.getText().toString());
                                editor.putString(commonVariables.Pass, etPassword.getText().toString());
                                editor.putString(commonVariables.Location, support_location);
                                editor.putString(commonVariables.Website, website);
                                editor.putString(commonVariables.UserID, user_id);
                                editor.putString(commonVariables.Domain, etURL.getText().toString());
                                editor.putString(commonVariables.role_id, role_id);
                                editor.commit();

                                Intent i = new Intent(Login.this, Drower.class);
                                startActivity(i);
                                finish();

                            } else {
                                Toast.makeText(Login.this, "error", Toast.LENGTH_SHORT).show();
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }

                        myDialog2.dismiss();

                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {

                Toast.makeText(Login.this, "error", Toast.LENGTH_SHORT).show();
                myDialog2.dismiss();
            }
        }) {

            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<String, String>();
                params.put("username", username);
                params.put("password", password);

                return params;
            }
        };

        stringRequest.setRetryPolicy(new DefaultRetryPolicy(
                10000,
                3,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));

        RequestQueue requestQueue = Volley.newRequestQueue(Login.this);
        requestQueue.add(stringRequest);
    }

    public boolean validationLogin() {

        if (etUserName.getText().toString().equals("")) {
            etUserName.setError("Enter User Name");
            etUserName.requestFocus();
            status = false;
        }
        if (etPassword.getText().toString().equals("")) {
            etPassword.setError("Enter Password");
            etPassword.requestFocus();
            status = false;
        }
        if (etURL.getText().toString().equals("")) {
            etURL.setError("Enter URL");
            etURL.requestFocus();
            status = false;
        }

        return status;
    }

    public boolean validationRegistration() {

        if (etName.getText().toString().equals("")) {
            etName.setError("Enter User Name");
            etName.requestFocus();
            status = false;
        }
        if (etEmail.getText().toString().equals("")) {
            etEmail.setError("Enter Email");
            etEmail.requestFocus();
            status = false;
        }
        if (etCompanyName.getText().toString().equals("")) {
            etCompanyName.setError("Enter Company Name");
            etCompanyName.requestFocus();
            status = false;
        }
        if (etAddress.getText().toString().equals("")) {
            etAddress.setError("Enter Company Address");
            etAddress.requestFocus();
            status = false;
        }
        if (etDomain.getText().toString().equals("")) {
            etDomain.setError("Enter Domain");
            etDomain.requestFocus();
            status = false;
        }
        if (etContact.getText().toString().length() != 10){
            etContact.setError("Enter Valid Contact number");
            etContact.requestFocus();
            status = false;
        }
        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(etEmail.getText().toString()).matches()) {
            etEmail.setError("Enter valid mail ID");
            etEmail.requestFocus();
            status = false;
        }

        if (!Patterns.WEB_URL.matcher(etDomain.getText().toString()).matches()) {
            etDomain.setError("Enter valid Email");
            etDomain.requestFocus();
            status = false;
        }

        return status;
    }
}