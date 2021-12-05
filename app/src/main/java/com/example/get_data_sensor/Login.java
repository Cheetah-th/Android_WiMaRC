package com.example.get_data_sensor;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;

import com.koushikdutta.async.future.FutureCallback;
import com.koushikdutta.ion.Ion;

public class Login extends AppCompatActivity {

    EditText txtUsername, txtPassword;
    Button btnLogin, btnControl, btnExit;
    CheckBox checkRemember;

    private static final String USER_LOGIN_URL = "http://203.185.137.241/wimarctest/android_PHP/check_login.php";
    private static final String TAG = "Login";
    ProgressDialog progressDialog;

    private SharedPreferences mPrefs;
    private static final String PREFS_NAME = "PrefsFile";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        mPrefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);

        initViews();
        initEvent();

        //Get from remember me
        getPreferencesData();
    }

    private void getPreferencesData() {
        SharedPreferences sp = getSharedPreferences (PREFS_NAME, MODE_PRIVATE) ;
        if (sp.contains("pref_name")) {
            String u = sp.getString("pref_name", "Not found");
            txtUsername.setText(u.toString());
        }
        if (sp.contains ("pref_pass")) {
            String p = sp.getString("pref_pass", "Not found");
            txtPassword.setText(p.toString());
        }
        if (sp.contains ("pref_check")) {
            Boolean b = sp.getBoolean("pref_check", false);
            checkRemember.setChecked(b);
        }
    }

    private void initViews() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        txtUsername = findViewById(R.id.txtUsername);
        txtPassword = findViewById(R.id.txtPassword);
        checkRemember = findViewById(R.id.checkRemember);
        btnLogin = findViewById(R.id.btnLogin);
        btnControl = findViewById(R.id.btnControl);
        btnExit = findViewById(R.id.btnExit);
    }

    private void initEvent() {
        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                progressDialog = ProgressDialog.show(Login.this,"Logging in","Please wait",false,false);
                userLogin(USER_LOGIN_URL);
            }
        });

        btnControl.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(), Bluetooth.class);
                startActivity(intent);
            }
        });

        btnExit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
                System.exit(0);
            }
        });
    }

    private void userLogin(@NonNull final String url) {
        //Get data
        String username = txtUsername.getText().toString();
        String password = txtPassword.getText().toString();

        //Check data
        if (username.isEmpty() || password.isEmpty()) {
            Toast.makeText(Login.this, "Please enter username and password!", Toast.LENGTH_LONG).show();
        } else {
            Ion.with(Login.this)
                    .load(url)
                    .setBodyParameter("username", username)
                    .setBodyParameter("pass", password)
                    .asString()
                    .setCallback(new FutureCallback<String>() {
                        @Override
                        public void onCompleted(Exception e, String result) {
                            progressDialog.dismiss();
                            if (result.equals("Login successfully")) {

                                //Remember me
                                rememberMe();

                                //go to MainActivity
                                Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                                intent.putExtra("username", username);
                                finish();
                                startActivity(intent);

                                if (!checkRemember.isChecked()) {
                                    clearText();
                                }
                            }

                            Log.e(TAG, "Network error: " + e);
                            Log.w(TAG, "Result: " + result);

                            if(result != null){
                                Toast.makeText(Login.this, result, Toast.LENGTH_LONG).show();
                            } else {
                                Toast.makeText(Login.this, "Error: " + e, Toast.LENGTH_LONG).show();
                            }

                        }
                    });
        }
    }

    private void rememberMe() {
        if (checkRemember.isChecked()) {
            Boolean boolIsChecked = checkRemember.isChecked();
            SharedPreferences.Editor editor = mPrefs.edit();
            editor.putString("pref_name", txtUsername.getText().toString());
            editor.putString("pref_pass", txtPassword.getText().toString());
            editor.putBoolean("pref_check", boolIsChecked);
            editor.apply();
//            Toast.makeText(getApplicationContext(), "Username and password have been saved", Toast.LENGTH_SHORT).show();
        } else {
            mPrefs.edit().clear().apply();
        }
    }

    private void clearText() {
        txtUsername.setText(null);
        txtPassword.setText(null);
    }
}