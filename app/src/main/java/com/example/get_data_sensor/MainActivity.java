package com.example.get_data_sensor;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.koushikdutta.async.future.FutureCallback;
import com.koushikdutta.ion.Ion;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Timer;
import java.util.TimerTask;

public class MainActivity extends AppCompatActivity {

    TextView txtMessage, txtMessage0, txtMessage1, txtMessage2, txtMessage3, txtMessage4, txtMessage5, txtMessage6, txtMessage7, txtMessage8, txtMessage9, txtMessage10;
    ImageView imageView, imageView1;
    Button btnComment, btnLogout;

    private String id;
    private String date;
    private String time;
    private String Temp;
    private String Humid;
    private String Rain;
    private String WindS;
    private String WindD;
    private String M1;
    private String M2;
    private String Lux;

    private String dbname;
    private String url1;
    private String url2;

    TimerTask doTask;

    private boolean refresh = false;

    private static final String GET_DATA_SENSOR = "http://203.185.137.241/wimarctest/android_PHP/get_data_sensor.php";
    private static final String TAG = "MainActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        dbname = getIntent().getExtras().getString("username");

        url1 = "http://203.150.37.159/" + dbname + "/image/H06.jpg";
        url2 = "http://203.150.37.159/nectecGreen01/image/" + localDate() + "/H12.jpg";

        initViews();
        initEvent();

        viewEnvironmentData();
        autoRefresh();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            Toast.makeText(MainActivity.this, "Do something", Toast.LENGTH_LONG).show();
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_main2, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        switch (item.getItemId()) {
            case R.id.open_web:
                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("http://203.150.37.159/" + dbname + "/NETPIE/"));
                startActivity(intent);
                return true;

            case R.id.refresh:
                Intent intent1 = getIntent();
                doTask.cancel();
                finish();
                Toast.makeText(MainActivity.this, "Refresh successfully", Toast.LENGTH_LONG).show();
                startActivity(intent1);
                return true;

            case R.id.logout:
                Intent intent2 = new Intent(getApplicationContext(), Login.class);
                doTask.cancel();
                finish();
                Toast.makeText(MainActivity.this, "Logout successfully", Toast.LENGTH_LONG).show();
                startActivity(intent2);
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void autoRefresh() {
        //Auto refresh use thread
        final Handler handler = new Handler();
        Timer timer = new Timer();
        doTask = new TimerTask() {
            @Override
            public void run() {
                handler.post(new Runnable() {
                    @SuppressWarnings("unchecked")
                    public void run() {
                        try {
                            Intent intent = getIntent();
                            finish();
                            Toast.makeText(MainActivity.this, "Auto refresh", Toast.LENGTH_LONG).show();
                            startActivity(intent);
                        } catch (Exception e) {
                            // TODO Auto-generated catch block
                        }
                    }
                });
            }
        };
        //Refresh every 30 minutes
        timer.schedule(doTask, 1800000);
    }

    private void initViews() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        imageView = findViewById(R.id.imageView);
        imageView1 = findViewById(R.id.imageView1);
        txtMessage = findViewById(R.id.txtMessage);
        txtMessage0 = findViewById(R.id.txtMessage0);
        txtMessage1 = findViewById(R.id.txtMessage1);
        txtMessage2 = findViewById(R.id.txtMessage2);
        txtMessage3 = findViewById(R.id.txtMessage3);
        txtMessage4 = findViewById(R.id.txtMessage4);
        txtMessage5 = findViewById(R.id.txtMessage5);
        txtMessage6 = findViewById(R.id.txtMessage6);
        txtMessage7 = findViewById(R.id.txtMessage7);
        txtMessage8 = findViewById(R.id.txtMessage8);
        txtMessage9 = findViewById(R.id.txtMessage9);
        txtMessage10 = findViewById(R.id.txtMessage10);
        btnLogout = findViewById(R.id.btnLogout);
        btnComment = findViewById(R.id.btnComment);
    }

    private void initEvent() {
        btnLogout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(), Login.class);
                doTask.cancel();
                finish();
                Toast.makeText(MainActivity.this, "Logout successfully", Toast.LENGTH_LONG).show();
                startActivity(intent);
            }
        });

        btnComment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(), Send_Comment.class);
                intent.putExtra("username", dbname);
                doTask.cancel();
                finish();
                startActivity(intent);
            }
        });
    }

    private void viewEnvironmentData() {
        //Check and get data
        Ion.with(this)
                .load(GET_DATA_SENSOR) //URL to PHP script for getting data
                .setBodyParameter("dbname", dbname)
                .asJsonArray()
                .setCallback(new FutureCallback<JsonArray>() {
                    @Override
                    public void onCompleted(Exception e, JsonArray result) {

                        Log.w(TAG, "Error: " + e);
                        Log.w(TAG, "Result: " + result);

                        if (result != null){
                            JsonObject object = (JsonObject) result.get(0);
                            id = object.get("id").getAsString();
                            date = object.get("date").getAsString();
                            time = object.get("time").getAsString();
                            Temp = object.get("Temp").getAsString();
                            Humid = object.get("Humid").getAsString();
                            Rain = object.get("Rain").getAsString();
                            WindS = object.get("WindS").getAsString();
                            WindD = object.get("WindD").getAsString();
                            M1 = object.get("M1").getAsString();
                            M2 = object.get("M2").getAsString();
                            Lux = object.get("Lux").getAsString();

                            //Check list
                            if (result.size() < 1){
                                Toast.makeText(MainActivity.this,
                                        "Not found data!", Toast.LENGTH_LONG).show();
                            }

                            //Show progress
                            showData(dbname);
                        }
                        else {
                            //Show error
                            Toast.makeText(MainActivity.this, "Error: " + e, Toast.LENGTH_LONG).show();
                        }
                    }
                });
    }

    private void showData(String dbname) {
        Glide
                .with(this)
                .load(url1)
                .apply(new RequestOptions().override(400, 225))
                .dontAnimate()
                .into(imageView);

        txtMessage.setText("dbname: " + dbname);
        txtMessage0.setText(id + " :id");
        txtMessage1.setText("date: " + date);
        txtMessage2.setText("time: " + time);
        txtMessage3.setText("Temp: " + Temp);
        txtMessage4.setText("Humid: " + Humid);
        txtMessage5.setText("Rain: " + Rain);
        txtMessage6.setText(WindS + " :WindS");
        txtMessage7.setText(WindD + " :WindD");
        txtMessage8.setText(M1 + " :M1");
        txtMessage9.setText(M2 + " :M2");
        txtMessage10.setText(Lux + " :Lux");

        Glide
                .with(this)
                .load(url2)
                .apply(new RequestOptions().override(400, 225))
                .dontAnimate()
                .into(imageView1);
    }

    private String localDate() {
        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMdd");
        return dateFormat.format(calendar.getTime());
    }
}