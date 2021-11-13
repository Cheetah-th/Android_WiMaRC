package com.example.get_data_sensor;

import androidx.appcompat.widget.Toolbar;

import android.app.Activity;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import me.aflak.bluetooth.Bluetooth;

public class BluetoothSerial_Scan extends Activity implements Bluetooth.DiscoveryCallback, AdapterView.OnItemClickListener {
    private me.aflak.bluetooth.Bluetooth bluetooth;
    private ListView listView;
    private ArrayAdapter<String> adapter;
    private TextView state;
    private ProgressBar progress;
    private Button scan;
    private List<BluetoothDevice> devices;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bluetooth_serial_scan);

        listView = findViewById(R.id.scan_list);
        state = findViewById(R.id.scan_state);
        progress = findViewById(R.id.scan_progress);
        scan = findViewById(R.id.scan_scan_again);

        adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, new ArrayList<String>());
        listView.setAdapter(adapter);
        listView.setOnItemClickListener(this);

        bluetooth = new Bluetooth(this);
        bluetooth.setDiscoveryCallback(this);

        bluetooth.scanDevices();
        progress.setVisibility(View.VISIBLE);
        state.setText("Scanning...");
        listView.setEnabled(false);

        scan.setEnabled(false);
        devices = new ArrayList<>();

        scan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        adapter.clear();
                        scan.setEnabled(false);
                    }
                });

                devices = new ArrayList<>();
                progress.setVisibility(View.VISIBLE);
                state.setText("Scanning...");
                bluetooth.scanDevices();
            }
        });
    }

    private void setText(final String txt){
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                state.setText(txt);
            }
        });
    }

    private void setProgressVisibility(final int id){
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                progress.setVisibility(id);
            }
        });
    }

    @Override
    public void onFinish() {
        setProgressVisibility(View.INVISIBLE);
        setText("Scan finished!");
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                scan.setEnabled(true);
                listView.setEnabled(true);
            }
        });
    }

    @Override
    public void onDevice(final BluetoothDevice device) {
        final BluetoothDevice tmp = device;
        devices.add(device);

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                adapter.add(tmp.getAddress()+" - "+tmp.getName());
            }
        });
    }

    @Override
    public void onPair(BluetoothDevice device) {
        setProgressVisibility(View.INVISIBLE);
        setText("Paired!");
        Intent i = new Intent(BluetoothSerial_Scan.this, BluetoothSerial_Select.class);
        startActivity(i);
        finish();
    }

    @Override
    public void onUnpair(BluetoothDevice device) {
        setProgressVisibility(View.INVISIBLE);
        setText("Paired!");
    }

    @Override
    public void onError(String message) {
        setProgressVisibility(View.INVISIBLE);
        setText("Error: "+message);
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        setProgressVisibility(View.VISIBLE);
        setText("Pairing...");
        bluetooth.pair(devices.get(position));
    }
}
