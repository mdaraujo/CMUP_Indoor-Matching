package com.mdaraujo.commonlibrary.model;

import com.google.firebase.firestore.Exclude;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

public class BeaconInfo implements Serializable {

    public static final String BEACONS_COLLECTION_NAME = "beacons";

    private String namespaceId;
    private String instanceId;
    private String macAddress;
    private String name;
    private int color;
    private int rssi;
    private Double distance;
    private boolean inRange;
    private float posX;
    private float posY;
    private String roomKey;

    public BeaconInfo() {
    }

    public BeaconInfo(String namespaceId, String instanceId, String macAddress, int rssi, Double distance, boolean inRange) {
        this.namespaceId = namespaceId;
        this.instanceId = instanceId;
        this.macAddress = macAddress;
        this.name = instanceId;
        this.color = 0x0000ff;
        this.rssi = rssi;
        this.distance = distance;
        this.inRange = inRange;
        this.posX = 0;
        this.posY = 0;
        this.roomKey = null;
    }

    public BeaconInfo(String namespaceId, String instanceId, String macAddress, String name, int color, int rssi, Double distance, boolean inRange) {
        this.namespaceId = namespaceId;
        this.instanceId = instanceId;
        this.macAddress = macAddress;
        this.name = name;
        this.color = color;
        this.rssi = rssi;
        this.distance = distance;
        this.inRange = inRange;
        this.posX = 0;
        this.posY = 0;
        this.roomKey = null;
    }

    public Map<String, Object> toMap() {
        Map<String, Object> map = new HashMap<>();
        map.put("namespaceId", namespaceId);
        map.put("instanceId", instanceId);
        map.put("macAddress", macAddress);
        map.put("name", name);
        map.put("color", color);
        map.put("posX", posX);
        map.put("posY", posY);
        map.put("roomKey", roomKey);

        return map;
    }

    public String getNamespaceId() {
        return namespaceId;
    }

    public void setNamespaceId(String namespaceId) {
        this.namespaceId = namespaceId;
    }

    public String getInstanceId() {
        return instanceId;
    }

    public void setInstanceId(String instanceId) {
        this.instanceId = instanceId;
    }

    public String getMacAddress() {
        return macAddress;
    }

    public void setMacAddress(String macAddress) {
        this.macAddress = macAddress;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getColor() {
        return color;
    }

    public void setColor(int color) {
        this.color = color;
    }

    @Exclude
    public int getRssi() {
        return rssi;
    }

    public void setRssi(int rssi) {
        this.rssi = rssi;
    }

    @Exclude
    public Double getDistance() {
        return distance;
    }

    public void setDistance(Double distance) {
        this.distance = distance;
    }

    @Exclude
    public boolean isInRange() {
        return inRange;
    }

    public void setInRange(boolean inRange) {
        this.inRange = inRange;
    }

    public float getPosX() {
        return posX;
    }

    public void setPosX(float posX) {
        this.posX = posX;
    }

    public float getPosY() {
        return posY;
    }

    public void setPosY(float posY) {
        this.posY = posY;
    }

    public String getRoomKey() {
        return roomKey;
    }

    public void setRoomKey(String roomKey) {
        this.roomKey = roomKey;
    }
}
