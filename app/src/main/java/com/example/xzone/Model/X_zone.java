package com.example.xzone.Model;
//USED TO RETRIEVE DATA FROM DATABASE
public class X_zone {
    String name;
    String proximity_length;
    //int[] hourly_frequency = new int[24];

    public X_zone(String name, String proximity_length) {
        this.name = name;
        this.proximity_length = proximity_length;
    }

    public String getName() {
        return name;
    }

    public String getProximity_length() {
        return proximity_length;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setProximity_length(String proximity_length) {
        this.proximity_length = proximity_length;
    }
}
