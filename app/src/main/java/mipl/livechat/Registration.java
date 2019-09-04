package mipl.livechat;

import android.support.design.widget.TabItem;
import android.support.design.widget.TabLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class Registration extends AppCompatActivity {

    EditText etName, etEmail, etContact, etCompanyName, etAddress, etDomain;
    Button btnRegister;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registration);

        etName = (EditText) findViewById(R.id.etName);
        etEmail = (EditText) findViewById(R.id.etEmail);
        etContact = (EditText) findViewById(R.id.etContact);
        etCompanyName = (EditText) findViewById(R.id.etCompanyName);
        etAddress = (EditText) findViewById(R.id.etAddress);
        etDomain = (EditText) findViewById(R.id.etDomain);
        btnRegister = (Button) findViewById(R.id.btnRegister);


    }
}