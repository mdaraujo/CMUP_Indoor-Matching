package com.mdaraujo.indoormatching;

public class Room {

    private String name;
    private String serverURL;
    private float width;
    private float height;

    public Room() {
    }

    public Room(String name, String serverURL, float width, float height) {
        this.name = name;
        this.serverURL = serverURL;
        this.width = width;
        this.height = height;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getServerURL() {
        return serverURL;
    }

    public void setServerURL(String serverURL) {
        this.serverURL = serverURL;
    }

    public float getWidth() {
        return width;
    }

    public void setWidth(float width) {
        this.width = width;
    }

    public float getHeight() {
        return height;
    }

    public void setHeight(float height) {
        this.height = height;
    }

}
