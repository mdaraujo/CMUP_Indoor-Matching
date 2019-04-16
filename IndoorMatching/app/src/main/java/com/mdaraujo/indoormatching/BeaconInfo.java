package com.mdaraujo.indoormatching;

public class BeaconInfo {

    private String namespaceId;
    private String instanceId;
    private String macAddress;
    private int rssi;
    private Double distance;
    private boolean inRange;

    public BeaconInfo(String namespaceId, String instanceId, String macAddress, int rssi, Double distance, boolean inRange) {
        this.namespaceId = namespaceId;
        this.instanceId = instanceId;
        this.macAddress = macAddress;
        this.rssi = rssi;
        this.distance = distance;
        this.inRange = inRange;
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

    public int getRssi() {
        return rssi;
    }

    public void setRssi(int rssi) {
        this.rssi = rssi;
    }

    public Double getDistance() {
        return distance;
    }

    public void setDistance(Double distance) {
        this.distance = distance;
    }

    public boolean isInRange() {
        return inRange;
    }

    public void setInRange(boolean inRange) {
        this.inRange = inRange;
    }
}
