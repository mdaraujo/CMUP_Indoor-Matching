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
