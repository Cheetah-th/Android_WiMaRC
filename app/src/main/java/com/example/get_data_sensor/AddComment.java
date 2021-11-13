package com.example.get_data_sensor;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.util.Patterns;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.koushikdutta.async.future.FutureCallback;
import com.koushikdutta.ion.Ion;

import java.util.Timer;
import java.util.TimerTask;

public class AddComment extends AppCompatActivity {
    TextView txtMessage1, txtMessage2, txtMessage3, txtMessage4, txtMessage5;
    Button btnUploadImage, btnSend;

    private String dbname;
    private String b;
    private String c;
    private String d;
    private String e;
    private String f;
    ProgressDialog progressDialog;

    private static final String ADD_COMMENT = "http://203.185.137.241/wimarctest/add-comment.php";
    private static final String TAG = "AddComment";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_comment);

        dbname = getIntent().getExtras().getString("username");

        initViews();
        initEvent();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            Intent intent = new Intent(getApplicationContext(), MainActivity.class);
            intent.putExtra("username", dbname);
            finish();
            startActivity(intent);
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_main1, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        switch (item.getItemId()) {
            case R.id.main_page:
                Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                intent.putExtra("username", dbname);
                finish();
                startActivity(intent);
                return true;

            case R.id.logout:
                Intent intent1 = new Intent(getApplicationContext(), Login.class);
                finish();
                Toast.makeText(AddComment.this, "Logout successfully", Toast.LENGTH_LONG).show();
                startActivity(intent1);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void getData() {
        b = txtMessage1.getText().toString();
        c = txtMessage2.getText().toString();
        d = txtMessage3.getText().toString();
        e = txtMessage4.getText().toString();
        f = txtMessage5.getText().toString();
    }

    private void initViews() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        txtMessage1 = findViewById(R.id.txtMessage1);
        txtMessage2 = findViewById(R.id.txtMessage2);
        txtMessage3 = findViewById(R.id.txtMessage3);
        txtMessage4 = findViewById(R.id.txtMessage4);
        txtMessage5 = findViewById(R.id.txtMessage5);
        btnUploadImage = findViewById(R.id.btnUploadImage);
        btnSend = findViewById(R.id.btnSend);
    }

    private void initEvent() {
        btnSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getData();
                if (b != null || c != null || c != null || d != null || e != null || f != null) {
                    progressDialog = ProgressDialog.show(AddComment.this,"Sending a message","Please wait",false,false);
                    uploadCommentToServer(ADD_COMMENT);
                } else {
                    Toast.makeText(AddComment.this, "Please enter message", Toast.LENGTH_LONG).show();
                }

            }
        });

        btnUploadImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(), AddImage.class);
                intent.putExtra("username", dbname);
                finish();
                startActivity(intent);
            }
        });
    }

    private void uploadCommentToServer(@NonNull final String url) {
        Ion.with(AddComment.this)
                .load(url)
                .setBodyParameter("A", dbname)
                .setBodyParameter("B", b)
                .setBodyParameter("C", c)
                .setBodyParameter("D", d)
                .setBodyParameter("E", e)
                .setBodyParameter("F", f)
                .asString()
                .setCallback(new FutureCallback<String>() {
                    @Override
                    public void onCompleted(Exception e, String result) {
                        if (result.equals("New record created successfully")) {
                            clearText();
                        }
                        progressDialog.dismiss();

                        Log.e(TAG, "Network error: " + e);
                        Log.w(TAG, "Result: " + result);

                        if(result != null){
                            Toast.makeText(AddComment.this, result, Toast.LENGTH_LONG).show();
                        } else {
                            Toast.makeText(AddComment.this, "Error: " + e, Toast.LENGTH_LONG).show();
                        }

                    }
                });
    }

    private void clearText() {
        txtMessage1.setText(null);
        txtMessage2.setText(null);
        txtMessage3.setText(null);
        txtMessage4.setText(null);
        txtMessage5.setText(null);
    }
}