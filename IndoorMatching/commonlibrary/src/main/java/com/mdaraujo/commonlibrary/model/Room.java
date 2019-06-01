package com.mdaraujo.commonlibrary.model;

import java.io.Serializable;

public class Room implements Serializable {

    public static final String ROOMS_COLLECTION_NAME = "rooms";

    private String name;
    private String serverURL;

    public Room() {
    }

    public Room(String name, String serverURL) {
        this.name = name;
        this.serverURL = serverURL;
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

}
