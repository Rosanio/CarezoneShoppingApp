package com.epicodus.carezoneshoppingapp;

public class Item {
    private long mId;
    private String mName;
    private String mCategory;
    private String mDateCreated;
    private String mUpdatedAt;
    private long mUserId;

    public Item(String name, String category, String dateCreated, long userId) {
        mName = name;
        mCategory = category;
        mDateCreated = dateCreated;
        mUpdatedAt = mDateCreated;
        mUserId = userId;
    }

    public long getId() {
        return mId;
    }

    public String getName() {
        return mName;
    }

    public String getDateCreated() {
        return mDateCreated;
    }

    public String getCategory() {
        return mCategory;
    }

    public String getUpdatedAt() {
        return mUpdatedAt;
    }

    public long getUserId() {
        return mUserId;
    }

    public void setId(long mId) {
        this.mId = mId;
    }

    public void setName(String mName) {
        this.mName = mName;
    }

    public void setCategory(String mCategory) {
        this.mCategory = mCategory;
    }

    public void setUpdatedAt(String mUpdatedAt) {
        this.mUpdatedAt = mUpdatedAt;
    }

    public void setDateCreated(String mDateCreated) {
        this.mDateCreated = mDateCreated;
    }

    public void setUserId(long mUserId) {
        this.mUserId = mUserId;
    }
}
