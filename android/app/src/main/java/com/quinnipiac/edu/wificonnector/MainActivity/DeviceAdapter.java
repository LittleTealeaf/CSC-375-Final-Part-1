package com.quinnipiac.edu.wificonnector.MainActivity;

import android.Manifest;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.pm.PackageManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import androidx.core.app.ActivityCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.quinnipiac.edu.wificonnector.R;

import java.util.LinkedList;
import java.util.List;

public class DeviceAdapter extends BaseAdapter {

    private final Context context;
    private List<BluetoothDevice> devices;
    private final LayoutInflater inflater;

    public DeviceAdapter(Context context) {
        this.devices = new LinkedList<>();
        this.context = context;
        inflater = LayoutInflater.from(context);
    }

    public void setItems(List<BluetoothDevice> devices) {
        this.devices = devices;
        notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        return devices.size();
    }

    @Override
    public Object getItem(int i) {
        return devices.get(i);
    }

    @Override
    public long getItemId(int i) {
        return 0;
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        if (view == null) {
            view = inflater.inflate(R.layout.list_device_item, viewGroup, false);
        }

        BluetoothDevice item = (BluetoothDevice) getItem(i);

        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return view;
        }

        ((TextView) view.findViewById(R.id.list_device_item_name)).setText(item.getName());
        ((TextView) view.findViewById(R.id.list_device_item_mac)).setText(item.getAddress());

        return view;
    }
}
