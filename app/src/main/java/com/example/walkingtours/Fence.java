package com.example.walkingtours;

public class Fence {
    private String id;
    private String address;
    private double lat;
    private double longitude;
    private float radius;
    private String description;
    private String fenceColor;
    private String url;

    public Fence(String id, String address, double lat, double longitude, float radius, String description, String fenceColor, String url) {
        this.id = id;
        this.address = address;
        this.lat = lat;
        this.longitude = longitude;
        this.radius = radius;
        this.description = description;
        this.fenceColor = fenceColor;
        this.url = url;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public void setLat(double lat) {
        this.lat = lat;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public void setRadius(float radius) {
        this.radius = radius;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setFenceColor(String fenceColor) {
        this.fenceColor = fenceColor;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getId() {
        return id;
    }

    public String getAddress() {
        return address;
    }

    public double getLat() {
        return lat;
    }

    public double getLongitude() {
        return longitude;
    }

    public float getRadius() {
        return radius;
    }

    public String getDescription() {
        return description;
    }

    public String getFenceColor() {
        return fenceColor;
    }

    public String getUrl() {
        return url;
    }
}