package com.inventoryapp;

import android.app.LoaderManager;
import android.content.ClipData;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.inventoryapp.data.InventoryContract;
import com.inventoryapp.data.InventoryDbHelper;

public class MainActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {

    private static final int ITEM_LOADER = 0;

    InventoryCursorAdapter mCursorAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Setup FAB to open InventoryEditorActivity
        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, InventoryEditorActivity.class);
                startActivity(intent);
            }
        });

        ListView itemListView = (ListView) findViewById(R.id.list);
        // Find and set empty view on the ListView, so that it only shows when the list has 0 items.
        View emptyView = findViewById(R.id.empty_view);
        itemListView.setEmptyView(emptyView);

        //Setup the adapter
        mCursorAdapter = new InventoryCursorAdapter(this, null);
        itemListView.setAdapter(mCursorAdapter);

        itemListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent = new Intent(MainActivity.this, DetailsActivity.class);

                Uri currentItemUri = ContentUris.withAppendedId(InventoryContract.ItemEntry.CONTENT_URI, id);
                intent.setData(currentItemUri);
                startActivity(intent);
            }
        });


        //Kick off the loader
        getLoaderManager().initLoader(ITEM_LOADER, null, this);
    }

    public void onSaleClick(long id, String name, int price, String supplier, String email, int phone, int previous_quantity, int subtract_quantity) {
        Uri currentProductUri = ContentUris.withAppendedId(InventoryContract.ItemEntry.CONTENT_URI, id);
        int current = (previous_quantity - subtract_quantity);

        ContentValues values = new ContentValues();
        values.put(InventoryContract.ItemEntry.COLUMN_PRODUCT_NAME, name);
        values.put(InventoryContract.ItemEntry.COLUMN_PRICE, price);
        values.put(InventoryContract.ItemEntry.COLUMN_SUPPLIER, supplier);
        values.put(InventoryContract.ItemEntry.COLUMN_EMAIL, email);
        values.put(InventoryContract.ItemEntry.COLUMN_QUANTITY, current);
        values.put(InventoryContract.ItemEntry.COLUMN_PHONE, phone);

        getContentResolver().update(currentProductUri, values, null, null);

    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_catalog, menu);
        return true;
    }

    private void insertItem() {

        ContentValues values = new ContentValues();
        values.put(InventoryContract.ItemEntry.COLUMN_PRODUCT_NAME, "Headphones");
        values.put(InventoryContract.ItemEntry.COLUMN_QUANTITY, "3");
        values.put(InventoryContract.ItemEntry.COLUMN_PRICE, "40");
        values.put(InventoryContract.ItemEntry.COLUMN_SUPPLIER, "Ebay");
        values.put(InventoryContract.ItemEntry.COLUMN_EMAIL, "dummy@mail.com");
        values.put(InventoryContract.ItemEntry.COLUMN_PHONE, "2103240559");

        getContentResolver().insert(InventoryContract.ItemEntry.CONTENT_URI, values);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            // Respond to a click on the "Insert dummy data" menu option
            case R.id.action_insert_dummy_data:
                insertItem();
                return true;
            case R.id.action_delete_all_entries:
                deleteAllItems();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void deleteAllItems() {
        int rowsDeleted = getContentResolver().delete(InventoryContract.ItemEntry.CONTENT_URI, null, null);
        Log.v("MainActivity", rowsDeleted + " rows deleted from inventory database");
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        String[] projection = {
                InventoryContract.ItemEntry._ID,
                InventoryContract.ItemEntry.COLUMN_PRODUCT_NAME,
                InventoryContract.ItemEntry.COLUMN_PRICE,
                InventoryContract.ItemEntry.COLUMN_QUANTITY,
                InventoryContract.ItemEntry.COLUMN_SUPPLIER,
                InventoryContract.ItemEntry.COLUMN_EMAIL,
                InventoryContract.ItemEntry.COLUMN_PHONE,
        };
        return new CursorLoader(this, InventoryContract.ItemEntry.CONTENT_URI, projection, null, null, null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        mCursorAdapter.swapCursor(data);

    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mCursorAdapter.swapCursor(null);

    }
}