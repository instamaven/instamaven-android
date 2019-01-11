package com.instamaven.app.models;

import java.util.HashMap;

public abstract class Entity {

    private HashMap<String, Object> attributes;
    // page number
    private int _page = 1;
    // position (sequence number)
    private int _position = 0;

    public Entity() {
        attributes = new HashMap<>();
    }

    public void setPage(int page) {
        this._page = page;
    }

    public void setPosition(int position) {
        this._position = position;
    }

    public int getPage() {
        return this._page;
    }

    public int getPosition() {
        return this._position;
    }

    public void put(String name, Object obj) {
        attributes.put(name, obj);
    }

    public Object get(String name) {
        return attributes.get(name);
    }

    public String getString(String name) {
        return (String) attributes.get(name);
    }

    public Integer getInt(String name) {
        return (Integer) attributes.get(name);
    }

    public Double getDouble(String name) {
        return (Double) attributes.get(name);
    }

}
