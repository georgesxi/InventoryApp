package com.inventoryapp.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class InventoryDbHelper extends SQLiteOpenHelper {

    /**
     * Name of the database file
     */
    private static final String DATABASE_NAME = "Inventory.db";
    private static final int DATABASE_VERSION = 1;

    public InventoryDbHelper(Context context) {
        super(context,
                DATABASE_NAME,
                null,
                DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String SQL_CREATE_INVENTORY_TABLE = " CREATE TABLE " + InventoryContract.ItemEntry.TABLE_NAME + " ("
                + InventoryContract.ItemEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + InventoryContract.ItemEntry.COLUMN_PRODUCT_NAME + " TEXT NOT NULL, "
                + InventoryContract.ItemEntry.COLUMN_QUANTITY + " INTEGER NOT NULL, "
                + InventoryContract.ItemEntry.COLUMN_PRICE + " INTEGER NOT NULL, "
                + InventoryContract.ItemEntry.COLUMN_SUPPLIER + " TEXT NOT NULL, "
                + InventoryContract.ItemEntry.COLUMN_EMAIL + " TEXT,"
                + InventoryContract.ItemEntry.COLUMN_PHONE + " TEXT );";

        db.execSQL(SQL_CREATE_INVENTORY_TABLE);

    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
//NOTHING YET
    }
}