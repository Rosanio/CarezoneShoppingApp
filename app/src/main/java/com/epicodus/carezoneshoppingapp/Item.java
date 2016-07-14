package com.epicodus.carezoneshoppingapp;

public class Item {
    private long mId;
    private String mName;
    private String mCategory;
    private long mServerId;

    public Item(String name, String category) {
        mName = name;
        mCategory = category;
    }

    public String getName() {
        return mName;
    }

    public String getCategory() {
        return mCategory;
    }

    public void setName(String mName) {
        this.mName = mName;
    }

    public void setCategory(String mCategory) {
        this.mCategory = mCategory;
    }

    public long getId() {
        return mId;
    }

    public void setId(long id) {
        this.mId = id;
    }

    public long getServerId() {
        return mServerId;
    }

    public void setServerId(long serverId) {
        this.mServerId = serverId;
    }
}
