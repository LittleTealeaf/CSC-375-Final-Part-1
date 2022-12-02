package com.quinnipiac.edu.wificonnector.MainActivity;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
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

public class MainActivity extends AppCompatActivity {

    public static final String KEY_MAC = "MAC", KEY_NAME = "DEVICE_NAME";
    private static final String TAG = "MainActivity";
    private final ActivityResultLauncher<String> requestPermissionLauncher;
    private final ActivityResultLauncher<Intent> deviceLauncher;
    private final BroadcastReceiver receiver;
    private DeviceAdapter adapter;
    private BluetoothAdapter bluetoothAdapter;

    {
        requestPermissionLauncher = registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
            if (isGranted) {
                loadDevices();
            }
        });

        deviceLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), this::onDeviceActivityFeedback);

        receiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                String action = intent.getAction();
                if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                    BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                    adapter.addDevice(device);
                    Log.d(TAG, "onReceive: Device Found " + device.getAddress());
                } else if (BluetoothAdapter.ACTION_DISCOVERY_STARTED.equals(action)) {
                    Log.d(TAG, "onReceive: Discovery Started");
                } else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)) {
                    Log.d(TAG, "onReceive: Discovery Finished");
                } else if (BluetoothAdapter.ACTION_STATE_CHANGED.equals(action)) {
                    Log.d(TAG, "onReceive: Bluetooth State Changed");
                }
            }
        };
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

        registerReceiver(receiver, new IntentFilter(BluetoothDevice.ACTION_FOUND));
        registerReceiver(receiver, new IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_STARTED));
        registerReceiver(receiver, new IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_FINISHED));
        registerReceiver(receiver, new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED));
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadDevices();
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
        adapter.clearItems();

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_SCAN) != PackageManager.PERMISSION_GRANTED) {
            requestPermissionLauncher.launch(Manifest.permission.BLUETOOTH_SCAN);
            return;
        }
        if (bluetoothAdapter.startDiscovery()) {
            Log.d(TAG, "loadDevices: Starting Discovery");
        } else {
            Log.d(TAG, "loadDevices: Discovery Failed");
        }
    }

    public void requestAllPermissions() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
            requestPermissionLauncher.launch(Manifest.permission.BLUETOOTH_CONNECT);
        }
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_SCAN) != PackageManager.PERMISSION_GRANTED) {
            requestPermissionLauncher.launch(Manifest.permission.BLUETOOTH_SCAN);
        }
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_ADVERTISE) != PackageManager.PERMISSION_GRANTED) {
            requestPermissionLauncher.launch(Manifest.permission.BLUETOOTH_ADVERTISE);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(receiver);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_SCAN) == PackageManager.PERMISSION_GRANTED) {
            bluetoothAdapter.cancelDiscovery();
        }
    }

    public void onDeviceActivityFeedback(ActivityResult result) {

    }

    private void onDiscoverableSelected(ActivityResult feedback) {

    }
}