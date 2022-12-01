package com.quinnipiac.edu.wificonnector.MainActivity;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.quinnipiac.edu.wificonnector.DeviceActivity.DeviceActivity;
import com.quinnipiac.edu.wificonnector.R;

import java.util.LinkedList;
import java.util.List;
import java.util.Set;

public class MainActivity extends AppCompatActivity {

    public static final String KEY_MAC = "MAC", KEY_NAME = "DEVICE_NAME";
    private static final String TAG = "MainActivity";
    private final ActivityResultLauncher<String> requestPermissionLauncher;
    private final ActivityResultLauncher<Intent> deviceLauncher;
    private DeviceAdapter adapter;
    private BluetoothAdapter bluetoothAdapter;

    {
        requestPermissionLauncher = registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
            if (isGranted) {
                loadDevices();
            }
        });
        deviceLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), this::onDeviceActivityFeedback);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ListView deviceList = findViewById(R.id.main_list_device);
        adapter = new DeviceAdapter(this);
        deviceList.setAdapter(adapter);
        deviceList.setOnItemClickListener(this::onDeviceClick);

        findViewById(R.id.main_button_refresh).setOnClickListener((view) -> loadDevices());

        BluetoothManager bluetoothManager = getSystemService(BluetoothManager.class);
        bluetoothAdapter = bluetoothManager.getAdapter();

        requestAllPermissions();
        loadDevices();
    }

    public void requestAllPermissions() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
            requestPermissionLauncher.launch(Manifest.permission.BLUETOOTH_CONNECT);
        }
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_SCAN) != PackageManager.PERMISSION_GRANTED) {
            requestPermissionLauncher.launch(Manifest.permission.BLUETOOTH_SCAN);
        }
    }

    public void onDeviceClick(AdapterView<?> adapterView, View view, int index, long id) {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
            requestPermissionLauncher.launch(Manifest.permission.BLUETOOTH_CONNECT);
            return;
        }
        Intent intent = new Intent(this, DeviceActivity.class);
        BluetoothDevice device = (BluetoothDevice) adapter.getItem(index);
        intent.putExtra(KEY_MAC, device.getAddress());
        intent.putExtra(KEY_NAME, device.getName());
        deviceLauncher.launch(intent);
    }

    public void loadDevices() {
        Log.d(TAG, "loadDevices: Loading Devices");
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
            requestPermissionLauncher.launch(Manifest.permission.BLUETOOTH_CONNECT);
            return;
        }
        Set<BluetoothDevice> pairedDevices = bluetoothAdapter.getBondedDevices();
        List<BluetoothDevice> deviceList = new LinkedList<>(pairedDevices);

        adapter.setItems(deviceList);
    }

    public void onDeviceActivityFeedback(ActivityResult result) {

    }
}