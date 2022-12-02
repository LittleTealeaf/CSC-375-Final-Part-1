package com.quinnipiac.edu.wificonnector.DeviceActivity;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.harrysoft.androidbluetoothserial.BluetoothManager;
import com.harrysoft.androidbluetoothserial.BluetoothSerialDevice;
import com.harrysoft.androidbluetoothserial.SimpleBluetoothDeviceInterface;
import com.quinnipiac.edu.wificonnector.MainActivity.MainActivity;
import com.quinnipiac.edu.wificonnector.R;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;

public class DeviceActivity extends AppCompatActivity {

    // TODO add "ordered" messages, which the device automatically will respond a PACKET/RECEIVED message indicating "hey, I got this message" (the content is the topic)jjkj

    private static final String TAG = "DeviceActivity";
    private static final int COMMUNICATION_VERSION = 1;
    private static final int STATUS_NO_SHIELD = 255, STATUS_IDLE = 0, STATUS_NO_SSID = 1, STATUS_SCAN_COMPLETE = 2, STATUS_CONNECTED = 3, STATUS_CONNECTION_FAILED = 4,
            STATUS_CONNECTION_LOST = 5, STATUS_DISCONNECTED = 6;

    private final Set<Packet> outstandingPackets = new HashSet<>();

    private SimpleBluetoothDeviceInterface deviceInterface;
    private BluetoothManager bluetoothManager;

    private TextView deviceWifiStatus, deviceBluetoothStatus, deviceLocalIP;

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
        deviceBluetoothStatus = findViewById(R.id.text_device_bluetooth);
        deviceWifiStatus = findViewById(R.id.text_device_wifi);
        deviceLocalIP = findViewById(R.id.text_device_ip);

        findViewById(R.id.button_join_wifi).setOnClickListener((view) -> connectDeviceToWifi(inputSSID.getText().toString(), inputPassword.getText().toString()));
        findViewById(R.id.button_connect_retry).setOnClickListener(view -> connectToDevice());

        bluetoothManager = BluetoothManager.getInstance();
    }

    @SuppressLint("CheckResult")
    private void connectToDevice() {
        bluetoothManager.openSerialDevice(getIntent().getStringExtra(MainActivity.KEY_MAC)).subscribeOn(
                Schedulers.io()).observeOn(AndroidSchedulers.mainThread()).subscribe(this::onConnected, this::onConnectionError);
        deviceWifiStatus.setText("");
        deviceWifiStatus.setText("");
        deviceLocalIP.setText("");
        deviceBluetoothStatus.setText(R.string.connecting);
    }

    private void connectDeviceToWifi(String ssid, String password) {
        sendPackerQueue(new Packet[]{
                new Packet("WIFI/SET_SSID", ssid), new Packet("WIFI/SET_PASSWORD", password), new Packet("WIFI/DO_CONNECT")
        });
    }

    private void onConnected(BluetoothSerialDevice device) {
        Log.d(TAG, "onConnected: " + device.getMac());
        deviceInterface = device.toSimpleDeviceInterface();
        deviceInterface.setListeners(this::onMessageReceived, this::onMessageSend, this::onCommunicationError);
        sendPacket(new Packet("DEVICE/GET_VERSION"));
        deviceBluetoothStatus.setText(R.string.connected);
        setConfigurable(true);
        findViewById(R.id.button_connect_retry).setVisibility(View.GONE);
    }

    private void onConnectionError(Throwable throwable) {
        Log.d(TAG, "onConnectionError: " + throwable.toString());
        deviceBluetoothStatus.setText(R.string.disconnected);
        setConfigurable(false);
        findViewById(R.id.button_connect_retry).setVisibility(View.VISIBLE);
    }

    public void sendPackerQueue(Packet[] packets) {
        for (int i = 0; i < packets.length - 1; i++) {
            packets[i].addChild(packets[i + 1]);
        }
        sendPacket(packets[0]);
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
        bluetoothManager.closeDevice(getIntent().getStringExtra(MainActivity.KEY_MAC));
        setConfigurable(false);
        deviceBluetoothStatus.setText(R.string.disconnected);
        deviceWifiStatus.setText("");
        deviceLocalIP.setText("");
        findViewById(R.id.button_connect_retry).setVisibility(View.VISIBLE);
    }

    public void sendPacket(Packet packet) {
        outstandingPackets.add(packet.send(deviceInterface));
    }

    private void setConfigurable(boolean configurable) {
        inputSSID.setEnabled(configurable);
        inputPassword.setEnabled(configurable);
        findViewById(R.id.button_join_wifi).setEnabled(configurable);
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
        Set<Packet> children = new HashSet<>();
        outstandingPackets.removeAll(outstandingPackets.stream().filter(packet -> packet.getTopic().equals(topic)).map((packet) -> {
            children.addAll(packet.sendChildPackets(deviceInterface));
            return packet;
        }).collect(Collectors.toSet()));
        outstandingPackets.addAll(children);
    }

    private void onDeviceVersion(int version) {
        if (version == COMMUNICATION_VERSION) {
            Log.d(TAG, "onDeviceVersion: Device is Compatible");
            sendPacket(new Packet("WIFI/GET_STATUS"));
            sendPacket(new Packet("WIFI/GET_SSID"));
            sendPacket(new Packet("WIFI/GET_PASSWORD"));
        }
    }

    private void onUpdateWiFiStatus(int status) {
        Log.d(TAG, "onUpdateWiFiStatus: " + status);
        if (status == STATUS_CONNECTED) {
            sendPacket(new Packet("WIFI/GET_LOCAL_IP"));
            deviceWifiStatus.setText(R.string.wifi_connected);
        } else {
            deviceLocalIP.setText("");

            if (status == STATUS_IDLE) {
                deviceWifiStatus.setText(R.string.wifi_idle);
            } else if (status == STATUS_NO_SSID) {
                deviceWifiStatus.setText(R.string.wifi_no_ssid);
            } else if (status == STATUS_NO_SHIELD) {
                deviceWifiStatus.setText(R.string.wifi_no_shield);
            } else if (status == STATUS_DISCONNECTED) {
                deviceWifiStatus.setText(R.string.wifi_disconnected);
            } else if (status == STATUS_CONNECTION_LOST) {
                deviceWifiStatus.setText(R.string.wifi_lost);
            }
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

    private void onWifiLocalIP(String ip) {
        Log.d(TAG, "onWifiLocalIP: " + ip);
        deviceLocalIP.setText(ip);
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
        connectToDevice();
        setConfigurable(false);
    }

    private void sendPacket(String topic) {
        sendPacket(new Packet(topic));
    }

    private void sendPacket(String topic, String content) {
        sendPacket(new Packet(topic, content));
    }
}