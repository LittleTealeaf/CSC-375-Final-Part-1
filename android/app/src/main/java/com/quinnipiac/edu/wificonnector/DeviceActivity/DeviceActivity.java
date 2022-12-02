package com.quinnipiac.edu.wificonnector.DeviceActivity;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.harrysoft.androidbluetoothserial.BluetoothManager;
import com.harrysoft.androidbluetoothserial.BluetoothSerialDevice;
import com.harrysoft.androidbluetoothserial.SimpleBluetoothDeviceInterface;
import com.quinnipiac.edu.wificonnector.MainActivity.MainActivity;
import com.quinnipiac.edu.wificonnector.R;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;

public class DeviceActivity extends AppCompatActivity {

    private static final String TAG = "DeviceActivity";
    private static final Set<Integer> COMPATIBLE_VERSIONS;
    private static final Map<Integer, String> WIFI_STATUS_MAP;

    static {
        WIFI_STATUS_MAP = new HashMap<Integer, String>() {{
            put(255, "No Shield");
            put(0, "Idle");
            put(1, "No SSID Available");
            put(2, "Scan Complete");
            put(3, "Connected");
            put(4, "Connection Failed");
            put(5, "Connection Lost");
            put(6, "Disconnected");
        }};
        COMPATIBLE_VERSIONS = new HashSet<Integer>() {{
            add(1);
        }};
    }

    private BluetoothManager bluetoothManager;
    private SimpleBluetoothDeviceInterface deviceInterface;

    @SuppressLint("CheckResult")
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_device);

        bluetoothManager = BluetoothManager.getInstance();

        bluetoothManager.openSerialDevice(getIntent().getStringExtra(MainActivity.KEY_MAC)).subscribeOn(Schedulers.io()).observeOn(
                AndroidSchedulers.mainThread()).subscribe(this::onConnected, this::onConnectionError);
    }

    private void onConnected(BluetoothSerialDevice device) {
        Log.d(TAG, "onConnected: " + device.getMac());
        deviceInterface = device.toSimpleDeviceInterface();
        deviceInterface.setListeners(this::onMessageReceived, this::onMessageSend, this::onCommunicationError);
        sendPacket("DEVICE/GET_VERSION");
    }

    private void onConnectionError(Throwable throwable) {
        Log.d(TAG, "onConnectionError: " + throwable.toString());
    }

    private void onMessageReceived(String message) {
        Log.d(TAG, "onMessageReceived: " + message);
        int index = message.indexOf(':');
        String topic = message.substring(0,index);
        String content = message.substring(index+1);
        handlePacket(topic,content);
    }

    private void onMessageSend(String message) {
        Log.d(TAG, "onMessageSend: " + message);
    }

    private void onCommunicationError(Throwable throwable) {
        Log.d(TAG, "onCommunicationError: " + throwable.toString());
    }

    private void sendPacket(String topic) {
        sendPacket(topic, "");
    }

    private void sendPacket(String topic, String content) {
        deviceInterface.sendMessage(topic + ":" + content + "\n");
    }



    private void handlePacket(String topic, String content) {
        switch(topic) {
            case "DEVICE/VERSION":
                onDeviceVersion(Integer.parseInt(content));
                break;
            case "WIFI/STATUS":
                onUpdateWiFiStatus(Integer.parseInt(content));
                break;
            default:
                Log.d(TAG, "handlePacket: Unknown Message Received");
        }
    }

    private void onDeviceVersion(int version) {
        if (COMPATIBLE_VERSIONS.contains(version)) {
            Log.d(TAG, "onDeviceVersion: Device is Compatible");
            sendPacket("WIFI/GET_STATUS");
        }
    }

    private void onUpdateWiFiStatus(int status) {
        Log.d(TAG, "onUpdateWiFiStatus: " + WIFI_STATUS_MAP.get(status));
        

    }
}