package com.quinnipiac.edu.wificonnector.DeviceActivity;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.quinnipiac.edu.wificonnector.R;

import java.util.LinkedList;
import java.util.List;

public class DebugAdapter extends BaseAdapter {

    private final List<String> log;
    private final LayoutInflater inflater;

    public DebugAdapter(Context context) {
        this.log = new LinkedList<>();
        this.inflater = LayoutInflater.from(context);
    }

    public void addDebug(String message) {
        log.add(0,message);
        notifyDataSetChanged();
    }

    public List<String> getLog() {
        return log;
    }

    @Override
    public int getCount() {
        return log.size();
    }

    @Override
    public Object getItem(int i) {
        return log.get(i);
    }

    @Override
    public long getItemId(int i) {
        return 0;
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        if(view == null) {
            view = inflater.inflate(R.layout.list_debug_item,viewGroup,false);
        }

        String message = (String) getItem(i);

        ((TextView) view.findViewById(R.id.list_debug_item_content)).setText(message);

        return view;

    }
}
