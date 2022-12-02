package com.quinnipiac.edu.wificonnector.DeviceActivity;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import com.harrysoft.androidbluetoothserial.BluetoothConnectException;
import com.harrysoft.androidbluetoothserial.BluetoothManager;
import com.harrysoft.androidbluetoothserial.BluetoothSerialDevice;
import com.harrysoft.androidbluetoothserial.SimpleBluetoothDeviceInterface;
import com.quinnipiac.edu.wificonnector.MainActivity.MainActivity;
import com.quinnipiac.edu.wificonnector.R;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;

public class DeviceActivity extends AppCompatActivity {

    private static final String TAG = "DeviceActivity";

    private BluetoothManager bluetoothManager;
    private String mac;

    private TextView textConnectStatus, inputSSID, inputPassword;

    private SimpleBluetoothDeviceInterface deviceInterface;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_device);

        bluetoothManager = BluetoothManager.getInstance();

        TextView textDeviceName = findViewById(R.id.device_name);
        TextView textDeviceMac = findViewById(R.id.device_mac);
        inputSSID = findViewById(R.id.device_wifi_ssid);
        inputPassword = findViewById(R.id.device_wifi_password);
        textConnectStatus = findViewById(R.id.device_connect_status);

        mac = getIntent().getStringExtra(MainActivity.KEY_MAC);
        String name = getIntent().getStringExtra(MainActivity.KEY_NAME);

        findViewById(R.id.device_button_configure).setOnClickListener((view) -> sendWiFiPacket());

        textDeviceName.setText(name);
        textDeviceMac.setText(mac);

        connectDevice(mac);
    }

    @SuppressLint("CheckResult")
    private void connectDevice(String mac) {
        bluetoothManager.openSerialDevice(mac).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread()).subscribe(this::onConnected, this::onError);
    }

    private void onConnected(BluetoothSerialDevice device) {
        textConnectStatus.setText(R.string.connected);

        deviceInterface = device.toSimpleDeviceInterface();

        deviceInterface.setListeners(this::onMessageReceived, this::onMessageSend, this::onError);
        sendQueryPacket();
    }

    private void sendQueryPacket() {
        sendMessage("WIFI/QUERY",null);
    }

    private void sendWiFiPacket() {
        String ssid = inputSSID.getText().toString();
        String password = inputPassword.getText().toString();

        sendMessage("WIFI/CONNECT",ssid + "," + password);
    }

    private void sendMessage(String topic, String content) {
        deviceInterface.sendMessage(topic + (content == null ? "\n" : ("|" + content + "\n")));
    }

    private void onMessageSend(String message) {
        Log.d(TAG, "onMessageSend: " + message);
    }

    private void onMessageReceived(String message) {
        Log.d(TAG, "onMessageReceived: " + message);
    }

    private void onError(Throwable error) {
        Log.d(TAG, "onError: " + error.toString());
        textConnectStatus.setText(R.string.disconnected);
    }

    @Override
    protected void onStop() {
        super.onStop();
        bluetoothManager.closeDevice(mac);
    }
}