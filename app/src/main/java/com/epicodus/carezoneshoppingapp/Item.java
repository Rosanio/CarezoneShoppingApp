package com.epicodus.carezoneshoppingapp;

public class Item {
    private long mId;
    private String mName;
    private String mCategory;

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
}
