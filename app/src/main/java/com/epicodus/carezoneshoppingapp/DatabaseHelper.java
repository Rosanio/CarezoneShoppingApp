package com.epicodus.carezoneshoppingapp;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

public class DatabaseHelper extends SQLiteOpenHelper {

    // Logcat tag
    private static final String LOG = DatabaseHelper.class.getName();

    // Database Version
    private static final int DATABASE_VERSION = 1;

    // Database Name
    private static final String DATABASE_NAME = "savedItems";

    // Table Names
    private static final String TABLE_ITEM = "item";

    //Item Column Names
    private static final String KEY_ITEM_ID = "_id";
    private static final String KEY_NAME = "name";
    private static final String KEY_CATEGORY = "category";

    // Table Create Statements

    //Days table create statement
    private static final String CREATE_TABLE_ITEM = "CREATE TABLE "
            + TABLE_ITEM + "(" + KEY_ITEM_ID + " INTEGER PRIMARY KEY,"
            + KEY_NAME + " TEXT,"
            + KEY_CATEGORY + " TEXT" + ")";

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // creating required tables
        db.execSQL(CREATE_TABLE_ITEM);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // on upgrade drop older tables
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_ITEM);

        // create new tables
        onCreate(db);
    }

    /*
    *Logging data for the item
     */

    public long logItems(Item item) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(KEY_NAME, item.getName());
        values.put(KEY_CATEGORY, item.getCategory());

        //insert row
        long item_id = db.insert(TABLE_ITEM, null, values);
        return item_id;
    }

    /*
    * Get single item record
    */
    public Item getItem(long item_id) {
        SQLiteDatabase db = this.getReadableDatabase();

        String selectQuery = "SELECT * FROM " + TABLE_ITEM + " WHERE "
                + KEY_ITEM_ID + " = " + item_id;

        Log.e(LOG, selectQuery);
        Cursor c = db.rawQuery(selectQuery, null);

        if (c != null) {
            c.moveToFirst();
        }

        Item item = new Item("", "");
        item.setId(c.getInt(c.getColumnIndex(KEY_ITEM_ID)));
        item.setName(c.getString(c.getColumnIndex(KEY_NAME)));
        item.setCategory((c.getString(c.getColumnIndex(KEY_CATEGORY))));
        return item;
    }

    /*
    * Get all item records
    */
    public List<Item> getAllItemRecords() {
        List<Item> allItemRecords = new ArrayList<Item>();
        String selectQuery = "SELECT * FROM " + TABLE_ITEM;

        Log.e(LOG, selectQuery);

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor c = db.rawQuery(selectQuery, null);

        //looping through all rows and adding to list
        if (c.moveToFirst()) {
            do {
                Item item = new Item("", "");
                item.setId(c.getInt(c.getColumnIndex(KEY_ITEM_ID)));
                item.setName(c.getString(c.getColumnIndex(KEY_NAME)));
                item.setCategory((c.getString(c.getColumnIndex(KEY_CATEGORY))));

                allItemRecords.add(item);
            } while (c.moveToNext());
        }
        return allItemRecords;
    }

    /*
    * Updating an item record
    */
    public int updateItem(Item item) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(KEY_NAME, item.getName());
        values.put(KEY_CATEGORY, item.getCategory());

        //updating row
        return db.update(TABLE_ITEM, values, KEY_ITEM_ID + " = ?",
                new String[]{String.valueOf(item.getId())});
    }

    /*
    * Deleting a item record
    */
    public void deleteItemRecord(long item_id) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_ITEM, KEY_ITEM_ID + " = ?",
                new String[]{String.valueOf(item_id)});
    }

    /*
    * Deleting all item records
    */

    public void deleteAllItemRecords() {
        SQLiteDatabase db = this.getWritableDatabase();
        db.execSQL("delete from " + TABLE_ITEM);
    }

    /*
    * Close the database
    */
    public void closeDB() {
        SQLiteDatabase db = this.getReadableDatabase();
        if (db != null && db.isOpen()) {
            db.close();
        }
    }

}
