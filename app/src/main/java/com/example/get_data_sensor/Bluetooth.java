package com.example.get_data_sensor;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class Bluetooth extends AppCompatActivity {
    Button btnBtsimple, btnBtserial, btnBtlowenergy;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bluetooth);

        initViews();
        initEvent();
    }

    private void initViews() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        btnBtsimple = findViewById(R.id.btnBtsimple);
        btnBtserial = findViewById(R.id.btnBtserial);
        btnBtlowenergy = findViewById(R.id.btnBtlowenergy);
    }

    private void initEvent() {
        btnBtsimple.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(), BluetoothSimple.class);
                startActivity(intent);
            }
        });

        btnBtserial.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(), BluetoothSerial_Select.class);
                startActivity(intent);
            }
        });

        btnBtlowenergy.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(), BluetoothBLE.class);
                startActivity(intent);
            }
        });
    }
}
