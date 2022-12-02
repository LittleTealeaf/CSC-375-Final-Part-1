package com.quinnipiac.edu.wificonnector.DeviceActivity;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.util.Log;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.harrysoft.androidbluetoothserial.BluetoothManager;
import com.harrysoft.androidbluetoothserial.BluetoothSerialDevice;
import com.harrysoft.androidbluetoothserial.SimpleBluetoothDeviceInterface;
import com.quinnipiac.edu.wificonnector.MainActivity.MainActivity;
import com.quinnipiac.edu.wificonnector.R;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;

public class DeviceActivity extends AppCompatActivity {

    // TODO add "ordered" messages, which the device automatically will respond a PACKET/RECEIVED message indicating "hey, I got this message" (the content is the topic)jjkj

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

    private final Set<Packet> outstandingPackets = new HashSet<>();

    private SimpleBluetoothDeviceInterface deviceInterface;
    private BluetoothManager bluetoothManager;

    private EditText inputSSID, inputPassword;

    @SuppressLint("CheckResult")
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_device);

        ((TextView) findViewById(R.id.text_device_name)).setText(getIntent().getStringExtra(MainActivity.KEY_NAME));
        ((TextView) findViewById(R.id.text_device_mac)).setText(getIntent().getStringExtra(MainActivity.KEY_MAC));

        inputSSID = findViewById(R.id.input_wifi_ssid);
        inputPassword = findViewById(R.id.input_wifi_password);

        findViewById(R.id.button_join_wifi).setOnClickListener((view) -> connectDeviceToWifi(inputSSID.getText().toString(), inputPassword.getText().toString()));

        bluetoothManager = BluetoothManager.getInstance();
    }

    private void connectDeviceToWifi(String ssid, String password) {
//        sendPacket(new Packet("WIFI/SET_SSID", ssid).addChild(new Packet("WIFI/SET_PASSWORD", password).addChild("WIFI/DO_CONNECT")));
        sendPackerQueue(new Packet[]{
                new Packet("WIFI/SET_SSID", ssid), new Packet("WIFI/SET_PASSWORD", password), new Packet("WIFI/DO_CONNECT")
        });
    }

    public void sendPackerQueue(Packet[] packets) {
        for (int i = 0; i < packets.length - 1; i++) {
            packets[i].addChild(packets[i + 1]);
        }
        sendPacket(packets[0]);
    }

    public void sendPacket(Packet packet) {
        outstandingPackets.add(packet.send(deviceInterface));
    }

    @Override
    protected void onPause() {
        super.onPause();
        bluetoothManager.closeDevice(getIntent().getStringExtra(MainActivity.KEY_MAC));
    }

    @SuppressLint("CheckResult")
    @Override
    protected void onResume() {
        super.onResume();
        bluetoothManager.openSerialDevice(getIntent().getStringExtra(MainActivity.KEY_MAC)).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread()).subscribe(
                this::onConnected, this::onConnectionError);
    }

    private void onConnected(BluetoothSerialDevice device) {
        Log.d(TAG, "onConnected: " + device.getMac());
        deviceInterface = device.toSimpleDeviceInterface();
        deviceInterface.setListeners(this::onMessageReceived, this::onMessageSend, this::onCommunicationError);
//        sendPacket("DEVICE/GET_VERSION");
        sendPacket(new Packet("DEVICE/GET_VERSION"));
    }

    private void onConnectionError(Throwable throwable) {
        Log.d(TAG, "onConnectionError: " + throwable.toString());
    }

    private void onMessageReceived(String message) {
        int index = message.indexOf(':');
        String topic = message.substring(0, index);
        String content = message.substring(index + 1);
        handlePacket(topic, content);
    }

    private void onMessageSend(String message) {
        Log.d(TAG, "onMessageSend: " + message);
    }

    private void onCommunicationError(Throwable throwable) {
        Log.d(TAG, "onCommunicationError: " + throwable.toString());
    }

    private void handlePacket(String topic, String content) {
        switch (topic) {
            case "PACKET/SUCCESS":
                onPacketSuccess(content);
                break;
            case "DEVICE/VERSION":
                onDeviceVersion(Integer.parseInt(content));
                break;
            case "WIFI/STATUS":
                onUpdateWiFiStatus(Integer.parseInt(content));
                break;
            case "WIFI/ERROR":
                onWifiError(content);
                break;
            case "WIFI/SSID":
                onWifiSSID(content);
                break;
            case "WIFI/PASSWORD":
                onWifiPassword(content);
                break;
            case "WIFI/LOCAL_IP":
                onWifiLocalIP(content);
                break;
            default:
                Log.d(TAG, "handlePacket: Unknown Message " + content);
        }
    }

    private void onPacketSuccess(String topic) {
        Log.d(TAG, "onPacketSuccess: " + topic);
        outstandingPackets.addAll(outstandingPackets
                                          .stream()
                                          .filter(packet -> packet.getTopic().equals(topic))
                                          .map(packet -> packet.sendChildPackets(deviceInterface))
                                          .reduce(new ArrayList<Packet>(), (
                                                  (packets, packets2) -> {
                                                      packets.addAll(packets2);
                                                      return packets;
                                                  }
                                          )));
    }

    private void onDeviceVersion(int version) {
        if (COMPATIBLE_VERSIONS.contains(version)) {
            Log.d(TAG, "onDeviceVersion: Device is Compatible");
            sendPacket(new Packet("WIFI/GET_STATUS"));
            sendPacket(new Packet("WIFI/GET_SSID"));
            sendPacket(new Packet("WIFI/GET_PASSWORD"));
        }
    }

    private void onUpdateWiFiStatus(int status) {
        Log.d(TAG, "onUpdateWiFiStatus: " + WIFI_STATUS_MAP.get(status));
        if (status == 3) {
            sendPacket(new Packet("WIFI/GET_LOCAL_IP"));
        } else {
//          set local ip to be null
        }
    }

    private void onWifiError(String error) {
        Log.d(TAG, "onWifiError: " + error);
    }

    private void onWifiSSID(String ssid) {
        Log.d(TAG, "onWifiSSID: " + ssid);
        inputSSID.setText(ssid);
    }

    private void onWifiPassword(String password) {
        Log.d(TAG, "onWifiPassword: " + password);
        inputPassword.setText(password);
    }

    public void onWifiLocalIP(String ip) {
        Log.d(TAG, "onWifiLocalIP: " + ip);
    }

    public void sendPacket(String topic) {
        sendPacket(new Packet(topic));
    }

    public void sendPacket(String topic, String content) {
        sendPacket(new Packet(topic, content));
    }
}