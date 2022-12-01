package com.quinnipiac.edu.wificonnector.DeviceActivity;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import com.harrysoft.androidbluetoothserial.BluetoothManager;
import com.harrysoft.androidbluetoothserial.BluetoothSerialDevice;
import com.quinnipiac.edu.wificonnector.MainActivity.MainActivity;
import com.quinnipiac.edu.wificonnector.R;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;

public class DeviceActivity extends AppCompatActivity {

    private static final String TAG = "DeviceActivity";

    private BluetoothManager bluetoothManager;
    private String mac;

    private TextView textStatus;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_device);

        bluetoothManager = BluetoothManager.getInstance();

        textStatus = findViewById(R.id.device_text_status);

        connectDevice(getIntent().getStringExtra(MainActivity.KEY_MAC));
    }

    private void connectDevice(String mac) {
        bluetoothManager.openSerialDevice(mac).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread()).subscribe(this::onConnected, this::onError);
    }

    private void onConnected(BluetoothSerialDevice device) {
        textStatus.setText(R.string.connected);
    }

    private void onMessageSend(String message) {

    }

    private void onMessageReceived(String message) {

    }

    private void onError(Throwable error) {
        textStatus.setText(R.string.disconnected);
    }

    @Override
    protected void onStop() {
        super.onStop();
        bluetoothManager.closeDevice(mac);
    }
}