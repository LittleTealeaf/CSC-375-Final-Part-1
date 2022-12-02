package com.quinnipiac.edu.wificonnector.DeviceActivity;

public enum WifiStatus {
    IDLE_STATUS("Idle"),
    NO_SSID_AVAILABLE("No SSID Available"),
    SCAN_COMPLETED("Scan Complete"),
    CONNECTED("Connected"),
    CONNECTION_FAILED("Connection Failed"),
    CONNECTION_LOST("Connection Lost"),
    DISCONNECTED("Disconnected");


    private String text;

    WifiStatus(String text) {
        this.text = text;
    }

}
