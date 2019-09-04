package mipl.livechat;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Base64;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ImageView;
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

import org.json.JSONArray;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

public class ChatWindow extends AppCompatActivity {

    StringRequest stringRequest;
    ProgressDialog myDialog1;

    private RecyclerView mRecyclerView;
    private UserAdapter mUserAdapter;
    ImageView button_chatbox_send;
    EditText edittext_chatbox;
    LinearLayout layout_chatbox;

    private List<PojoMsg> mUsers;
    String chatID, Email, IP, Country, From, browser, nick, hash, Contact;
    String CHAT, chatMsg;

    SharedPreferences sharedpreferences;
    String user, password, Location;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat_window);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setBackgroundDrawable(new ColorDrawable(getResources().getColor(R.color.blue)));

        sharedpreferences = getSharedPreferences(commonVariables.mypreference, Context.MODE_PRIVATE);
        user = sharedpreferences.getString(commonVariables.Name, "");
        password = sharedpreferences.getString(commonVariables.Pass, "");
        Location = sharedpreferences.getString(commonVariables.Location, "");

        layout_chatbox = (LinearLayout) findViewById(R.id.layout_chatbox);

        mUsers = new ArrayList<>();
        try {
            Intent intent = getIntent();
            chatID = intent.getStringExtra("chatID");
            Email = intent.getStringExtra("Email");
            IP = intent.getStringExtra("IP");
            Country = intent.getStringExtra("Location");
            From = intent.getStringExtra("From");
            browser = intent.getStringExtra("browser");
            nick = intent.getStringExtra("nick");
            hash = intent.getStringExtra("hash");
            Contact = intent.getStringExtra("Contact");
            setTitle(nick);

            CHAT = intent.getStringExtra("CHAT");
            if (CHAT.equals("CLOSED"))
                layout_chatbox.setVisibility(View.GONE);

        } catch (Exception e) {
            e.printStackTrace();
        }

        mRecyclerView = (RecyclerView) findViewById(R.id.reyclerview_message_list);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        button_chatbox_send = (ImageView) findViewById(R.id.button_chatbox_send);
        edittext_chatbox = (EditText) findViewById(R.id.edittext_chatbox);

        if (commonVariables.isInternetAvailable(ChatWindow.this)) {
            getChatsMsg();
        } else {
            Toast.makeText(ChatWindow.this, "Internet Connection not available", Toast.LENGTH_SHORT).show();
        }

        button_chatbox_send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (edittext_chatbox.getText().toString().equals("")) {
                    Toast.makeText(ChatWindow.this, "Add Message", Toast.LENGTH_SHORT).show();
                } else {
                    chatMsg = edittext_chatbox.getText().toString();
                    edittext_chatbox.setText("");
                    sendMsg(chatMsg);
                }
            }
        });

        Timer timer = new Timer();
        timer.schedule(new TimerTask() {
            public void run() {
                getChatsMsg();
            }
        }, 15000, 15000);
    }

    public void getChatsMsg() {

        mUsers.clear();

        stringRequest = new StringRequest(Request.Method.GET, Location + "/index.php/restapi/fetchchatmessages?chat_id=" + chatID,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {

                        Log.i("response", response);

                        JSONObject reader = null;
                        try {
                            reader = new JSONObject(response);

                            String error = reader.getString("error");
                            JSONObject result = reader.getJSONObject("result");

                            String closed = result.getString("closed");
                            String check_status = result.getString("check_status");

                            JSONArray messages = result.getJSONArray("messages");

                            for (int i = 0; i < messages.length(); i++) {
                                JSONObject chatList = messages.getJSONObject(i);

                                String id = chatList.getString("id");
                                String msg = chatList.getString("msg");
                                String meta_msg = chatList.getString("meta_msg");
                                String time = chatList.getString("time");
                                String chat_id = chatList.getString("chat_id");
                                String user_id = chatList.getString("user_id");
                                String name_support = chatList.getString("name_support");

                                long unixSeconds = Long.parseLong(time);
                                Date date = new Date(unixSeconds * 1000L);
                                SimpleDateFormat sdf = new SimpleDateFormat("dd-MMM-yyyy, HH:mm");
                                String formattedDate = sdf.format(date);

                                PojoMsg user = new PojoMsg();
                                user.setId(id);
                                user.setMsg(msg);
                                user.setMeta_msg(meta_msg);
                                user.setTime(formattedDate);
                                user.setChat_id(chat_id);
                                user.setUser_id(user_id);
                                user.setName_support(name_support);

                                mUsers.add(user);

                                mUserAdapter = new UserAdapter();
                                mRecyclerView.setAdapter(mUserAdapter);
                            }

                        } catch (Exception e) {
                            e.printStackTrace();
                        }
//                        getChatsMsg();
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {

//                Toast.makeText(ChatWindow.this, error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        }) {

            @Override
            public Map<String, String> getHeaders() {

                String auth = new String(user + ":" + password);
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

        RequestQueue requestQueue = Volley.newRequestQueue(ChatWindow.this);
        requestQueue.add(stringRequest);
    }

    static class UserViewHolder extends RecyclerView.ViewHolder {
        public TextView tvReceiveMsg, tvReceiveTime;
        public TextView tvSystemMsg, tvSystemTime;
        public TextView tvSendMsg, tvSendTime;
        public TextView tvSupportName;
        public LinearLayout llSend, llSystem, llReceive;

        public UserViewHolder(View itemView) {
            super(itemView);
            tvReceiveMsg = (TextView) itemView.findViewById(R.id.tvReceiveMsg);
            tvReceiveTime = (TextView) itemView.findViewById(R.id.tvReceiveTime);
            tvSystemMsg = (TextView) itemView.findViewById(R.id.tvSystemMsg);
            tvSystemTime = (TextView) itemView.findViewById(R.id.tvSystemTime);
            tvSendMsg = (TextView) itemView.findViewById(R.id.tvSendMsg);
            tvSendTime = (TextView) itemView.findViewById(R.id.tvSendTime);
            tvSupportName = (TextView) itemView.findViewById(R.id.tvSupportName);
            llSend = (LinearLayout) itemView.findViewById(R.id.llSend);
            llSystem = (LinearLayout) itemView.findViewById(R.id.llSystem);
            llReceive = (LinearLayout) itemView.findViewById(R.id.llReceive);
        }
    }

    class UserAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

        private final int VIEW_TYPE_ITEM = 0;
        private final int VIEW_TYPE_LOADING = 1;

        public UserAdapter() {
            final LinearLayoutManager linearLayoutManager = (LinearLayoutManager) mRecyclerView.getLayoutManager();
            mRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
                @Override
                public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                    super.onScrolled(recyclerView, dx, dy);
                }
            });
        }

        @Override
        public int getItemViewType(int position) {
            return mUsers.get(position) == null ? VIEW_TYPE_LOADING : VIEW_TYPE_ITEM;
        }

        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            if (viewType == VIEW_TYPE_ITEM) {
                View view = LayoutInflater.from(ChatWindow.this).inflate(R.layout.chatcard, parent, false);
                return new UserViewHolder(view);
            }
            return null;
        }

        @Override
        public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
            if (holder instanceof UserViewHolder) {
                final PojoMsg user = mUsers.get(position);
                mRecyclerView.smoothScrollToPosition(position);

                final UserViewHolder userViewHolder = (UserViewHolder) holder;

                if (user.getUser_id().equals("0")) {
                    userViewHolder.llSend.setVisibility(View.GONE);
                    userViewHolder.llSystem.setVisibility(View.GONE);
                    userViewHolder.tvSupportName.setVisibility(View.GONE);

                    userViewHolder.tvReceiveMsg.setText(user.getMsg());
                    userViewHolder.tvReceiveTime.setText(user.getTime());

                } else if (user.getUser_id().equals("-1")) {
                    userViewHolder.llReceive.setVisibility(View.GONE);
                    userViewHolder.llSend.setVisibility(View.GONE);
                    userViewHolder.tvSupportName.setVisibility(View.GONE);

                    userViewHolder.tvSystemMsg.setText(user.getMsg());
                    userViewHolder.tvSystemTime.setText(user.getTime());

                } else {
                    userViewHolder.llSystem.setVisibility(View.GONE);
                    userViewHolder.llReceive.setVisibility(View.GONE);

                    userViewHolder.tvSendMsg.setText(user.getMsg());
                    userViewHolder.tvSendTime.setText(user.getTime());
                    userViewHolder.tvSupportName.setText(user.getName_support());
                }
            }
        }

        @Override
        public int getItemCount() {
            return mUsers == null ? 0 : mUsers.size();
        }
    }

    public void sendMsg(final String chatMsg) {

        stringRequest = new StringRequest(Request.Method.POST, Location + "/index.php/restapi/addmsgadmin",
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {

                        Log.i("response", response);

                        try {
                            JSONObject resp = new JSONObject(response);
                            String error = resp.getString("error");

                            if (error.equals("false")) {
                                edittext_chatbox.setText("");
                                getChatsMsg();
                            } else {
                                Toast.makeText(ChatWindow.this, "Error", Toast.LENGTH_SHORT).show();
                            }

                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {

//                Log.i("Error", error.getMessage());
                Toast.makeText(ChatWindow.this, "error", Toast.LENGTH_SHORT).show();
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
                params.put("msg", chatMsg);
                params.put("chat_id", chatID);
//                params.put("hash", hash);
                params.put("sender", sharedpreferences.getString(commonVariables.UserID, ""));

                return params;
            }
        };

        stringRequest.setRetryPolicy(new DefaultRetryPolicy(
                10000,
                3,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));

        RequestQueue requestQueue = Volley.newRequestQueue(ChatWindow.this);
        requestQueue.add(stringRequest);
    }

    public void showUserInfo() {
        final Dialog dialog = new Dialog(ChatWindow.this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.userinfo);
        Window window = dialog.getWindow();
        WindowManager.LayoutParams wlp = window.getAttributes();
        wlp.gravity = Gravity.BOTTOM;
        wlp.width = WindowManager.LayoutParams.FILL_PARENT;

        TextView tvUserID, tvUserEmail, tvUserIP, tvUserFrom, tvUserBrowser, tvUserCountry, tvUserContact, tvUserName;

        tvUserID = (TextView) dialog.findViewById(R.id.tvUserID);
        tvUserEmail = (TextView) dialog.findViewById(R.id.tvUserEmail);
        tvUserIP = (TextView) dialog.findViewById(R.id.tvUserIP);
        tvUserFrom = (TextView) dialog.findViewById(R.id.tvUserFrom);
        tvUserBrowser = (TextView) dialog.findViewById(R.id.tvUserBrowser);
        tvUserCountry = (TextView) dialog.findViewById(R.id.tvUserCountry);
        tvUserContact = (TextView) dialog.findViewById(R.id.tvUserContact);
        tvUserName = (TextView) dialog.findViewById(R.id.tvUserName);

        tvUserID.setText(chatID);
        tvUserEmail.setText(Email);
        tvUserIP.setText(IP);
        tvUserFrom.setText(From);
        tvUserBrowser.setText(browser);
        tvUserCountry.setText(Country);
        tvUserContact.setText(Contact);
        tvUserName.setText(nick);

        dialog.show();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.add:

                showUserInfo();

                return (true);
        }
        return (super.onOptionsItemSelected(item));
    }

    @Override
    public boolean onSupportNavigateUp() {

        if (CHAT.equals("Pending")) {
            startActivity(new Intent(ChatWindow.this, Drower.class));
            finish();
        } else {
            finish();
        }
        return true;
    }

    @Override
    public void onBackPressed() {
//        startActivity(new Intent(ChatWindow.this, Drower.class));
        if (CHAT.equals("Pending")) {
            startActivity(new Intent(ChatWindow.this, Drower.class));
            finish();
        } else {
            finish();
        }
    }
}