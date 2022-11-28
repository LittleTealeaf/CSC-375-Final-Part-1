package com.quinnipiac.edu.wificonnector;

import androidx.appcompat.app.AppCompatActivity;

import android.bluetooth.BluetoothDevice;
import android.os.Bundle;
import android.widget.ListView;
import android.widget.Toast;

import com.harrysoft.androidbluetoothserial.BluetoothManager;

import java.util.Collection;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        BluetoothManager bluetoothManager = BluetoothManager.getInstance();
        if(bluetoothManager == null) {
            Toast.makeText(this, "Bluetooth not available", Toast.LENGTH_LONG).show();
            finish();
        }

        ListView deviceList = findViewById(R.id.list_devices);


    }

    void refreshDevices() {
        Collection<BluetoothDevice> pairedDevices = BluetoothManager.getInstance().getPairedDevicesList();


    }
}