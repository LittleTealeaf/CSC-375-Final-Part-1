package com.quinnipiac.edu.wificonnector.DeviceActivity;

import androidx.annotation.NonNull;

import com.harrysoft.androidbluetoothserial.SimpleBluetoothDeviceInterface;

import java.util.ArrayList;
import java.util.List;

public class Packet {
    private String topic, content;
    private final List<Packet> childPackets = new ArrayList<>();
    private boolean sent;

    public Packet() {
        this.content = "";
    }

    public Packet(String topic) {
        this.topic = topic;
        this.content = "";
    }

    public Packet(String topic, String content) {
        this.topic = topic;
        this.content = content;
    }

    public Packet setContent(String content) {
        this.content = content;
        return this;
    }

    public Packet setTopic(String topic) {
        this.topic = topic;
        return this;
    }

    public String getTopic() {
        return topic;
    }


    public String getContent() {
        return content;
    }

    public Packet addChild(Packet packet) {
        this.childPackets.add(packet);
        return this;
    }

    public Packet addChild(String topic) {
        return addChild(new Packet(topic));
    }

    public Packet addChild(String topic, String content) {
        return addChild(new Packet(topic,content));
    }

    public Packet send(SimpleBluetoothDeviceInterface device) {
        device.sendMessage(topic + ":" + content + "\n");
        return this;
    }

    public List<Packet> getChildPackets() {
        return childPackets;
    }

    public List<Packet> sendChildPackets(SimpleBluetoothDeviceInterface deviceInterface) {
        for(Packet packet : childPackets) {
            packet.send(deviceInterface);
        }
        return childPackets;
    }


}
