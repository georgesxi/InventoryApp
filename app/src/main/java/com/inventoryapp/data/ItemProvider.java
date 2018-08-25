package com.inventoryapp.data;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

public class ItemProvider extends ContentProvider {

    public static final String LOG_TAG = ItemProvider.class.getSimpleName();

    /**
     * URI matcher code for the content URI for the items table
     */
    private static final int ITEMS = 100;

    private static final int ITEMS_ID = 101;

    private static final UriMatcher sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);

    static {
        sUriMatcher.addURI(InventoryContract.CONTENT_AUTHORITY, InventoryContract.PATH_ITEMS, ITEMS);

        sUriMatcher.addURI(InventoryContract.CONTENT_AUTHORITY, InventoryContract.PATH_ITEMS + "/#", ITEMS_ID);
    }

    private InventoryDbHelper mDbHelper;


    @Override
    public boolean onCreate() {
        mDbHelper = new InventoryDbHelper(getContext());
        return true;
    }
    @Nullable
    @Override
    public Cursor query(@NonNull Uri uri, @Nullable String[] projection,@Nullable String selection,@Nullable String[] selectionArgs,@Nullable String sortOrder) {
        SQLiteDatabase database = mDbHelper.getReadableDatabase();

        // This cursor will hold the result of the query
        Cursor cursor;

        // Figure out if the URI matcher can match the URI to a specific code
        int match = sUriMatcher.match(uri);

        switch (match) {
            case ITEMS:
                cursor = database.query(InventoryContract.ItemEntry.TABLE_NAME,
                        projection,
                        selection,
                        selectionArgs,
                        null,
                        null,
                        sortOrder);
                break;
            case ITEMS_ID:
                selection = InventoryContract.ItemEntry._ID + "=?";
                selectionArgs = new String[]{String.valueOf(ContentUris.parseId(uri))};
                cursor = database.query(InventoryContract.ItemEntry.TABLE_NAME,
                        projection,
                        selection,
                        selectionArgs,
                        null,
                        null,
                        sortOrder);
                break;
            default:
                throw new IllegalArgumentException("Cannot query unknown URI " + uri);

        }
        cursor.setNotificationUri(getContext().getContentResolver(), uri);

        return cursor;
    }

    @Override
    public String getType(Uri uri) {
        final int match = sUriMatcher.match(uri);
        switch (match) {
            case ITEMS:
                return InventoryContract.ItemEntry.CONTENT_LIST_TYPE;
            case ITEMS_ID:
                return InventoryContract.ItemEntry.CONTENT_ITEM_TYPE;
            default:
                throw new IllegalStateException("Unknown URI " + uri + " with match " + match);
        }
    }

    @Nullable
    @Override
    public Uri insert(@NonNull Uri uri, @Nullable ContentValues contentValues) {
        final int match = sUriMatcher.match(uri);
        switch (match) {
            case ITEMS:
                return insertItem(uri, contentValues);
            default:
                throw new IllegalArgumentException("Insertion is not supported for " + uri);
        }
    }

    /**
     * Insert item into the database with the given content values. Return the new content URI
     * for that specific row in the database.
     */

    private Uri insertItem(Uri uri, ContentValues values) {
        // Check that the name is not null
        String name = values.getAsString(InventoryContract.ItemEntry.COLUMN_PRODUCT_NAME);
        if (name == null) {
            throw new IllegalArgumentException("Item needs to have a name");
        }

        Integer quantity = values.getAsInteger(InventoryContract.ItemEntry.COLUMN_QUANTITY);
        if (quantity < 0) {
            throw new IllegalArgumentException("Item quantity needs to be positive number");
        }

        Integer price = values.getAsInteger(InventoryContract.ItemEntry.COLUMN_PRICE);
        if (price == null || price < 0) {

            throw new IllegalArgumentException("Item price needs to be a valid number");
        }
        String supplier = values.getAsString(InventoryContract.ItemEntry.COLUMN_SUPPLIER);
        if (supplier == null) {
            throw new IllegalArgumentException("Item requires a supplier name");
        }
        String email = values.getAsString(InventoryContract.ItemEntry.COLUMN_EMAIL);
        if (email == null) {
            throw new IllegalArgumentException("Item requires a supplier email");
        }
        Integer phone = values.getAsInteger(InventoryContract.ItemEntry.COLUMN_PHONE);
        if (phone == null) {
            throw new IllegalArgumentException("Item requires a supplier phone");
        }

        SQLiteDatabase database = mDbHelper.getWritableDatabase();
        long id = database.insert(InventoryContract.ItemEntry.TABLE_NAME, null, values);
        if (id == -1) {
            Log.e(LOG_TAG, "Failed to insert row for " + uri);
        }
// Notify all listeners that the data has changed for the item content URI
        getContext().getContentResolver().notifyChange(uri, null);
        return ContentUris.withAppendedId(uri, id);
    }

    @Override
    public int delete(@NonNull Uri uri,@Nullable String selection, @Nullable String[] selectionArgs) {
        SQLiteDatabase database = mDbHelper.getReadableDatabase();

        int rowsDeleted;
        final int match = sUriMatcher.match(uri);
        switch (match) {
            case ITEMS:
                // Delete all rows that match the selection and selection args
                rowsDeleted = database.delete(InventoryContract.ItemEntry.TABLE_NAME, selection, selectionArgs);
                break;
            case ITEMS_ID:
                // Delete a single row given by the ID in the URI
                selection = InventoryContract.ItemEntry._ID + "=?";
                selectionArgs = new String[]{String.valueOf(ContentUris.parseId(uri))};
                rowsDeleted = database.delete(InventoryContract.ItemEntry.TABLE_NAME, selection, selectionArgs);
                break;
            default:
                throw new IllegalArgumentException("Deletion is not supported for " + uri);
        }

        // If 1 or more rows were deleted, then notify all listeners that the data at the
        // given URI has changed
        if (rowsDeleted != 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }

        // Return the number of rows deleted
        return rowsDeleted;
    }

    @Override
    public int update(@NonNull Uri uri, @Nullable ContentValues contentValues, @Nullable String selection, @Nullable String[] selectionArgs) {

        final int match = sUriMatcher.match(uri);
        switch (match) {
            case ITEMS:
                return updateItem(uri, contentValues, selection, selectionArgs);
            case ITEMS_ID:
                selection = InventoryContract.ItemEntry._ID + "=?";
                selectionArgs = new String[]{String.valueOf(ContentUris.parseId(uri))};
                return updateItem(uri, contentValues, selection, selectionArgs);
            default:
                throw new IllegalArgumentException("Update is not supported " + uri);
        }
    }

    private int updateItem(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        // If the {@link ItemEntry#COLUMN_PRODUCT_NAME} key is present,
        // check that the name value is not null.
        if (values.containsKey(InventoryContract.ItemEntry.COLUMN_PRODUCT_NAME)) {
            String name = values.getAsString(InventoryContract.ItemEntry.COLUMN_PRODUCT_NAME);
            if (name == null) {
                throw new IllegalArgumentException("Product requires a name");
            }
        }

        // check that the quantity value is valid.
        if (values.containsKey(InventoryContract.ItemEntry.COLUMN_QUANTITY)) {
            // Check that the quantity is greater than or equal to 0
            Integer quantity = values.getAsInteger(InventoryContract.ItemEntry.COLUMN_QUANTITY);
            if (quantity != null && quantity < 0) {
                throw new IllegalArgumentException("Item requires valid quantity");
            }
        }
        // check that the price value is valid.
       if (values.containsKey(InventoryContract.ItemEntry.COLUMN_PRICE)) {
            // Check that the price is not null and greater than 0
            Integer price = values.getAsInteger(InventoryContract.ItemEntry.COLUMN_PRICE);
            if (price != null && price <= 0) {
                throw new IllegalArgumentException("Item requires valid price");
            }
        }
        if (values.containsKey(InventoryContract.ItemEntry.COLUMN_SUPPLIER)) {
            String supplier = values.getAsString(InventoryContract.ItemEntry.COLUMN_SUPPLIER);
            if (supplier == null) {
                throw new IllegalArgumentException("Item requires valid supplier");
            }
        }
        if (values.containsKey(InventoryContract.ItemEntry.COLUMN_EMAIL)) {
            String email = values.getAsString(InventoryContract.ItemEntry.COLUMN_EMAIL);
            if (email == null) {
                throw new IllegalArgumentException("Item requires a supplier email");
            }
        }
        if (values.containsKey(InventoryContract.ItemEntry.COLUMN_PHONE)) {
            Integer phone = values.getAsInteger(InventoryContract.ItemEntry.COLUMN_PHONE);
            if (phone == null) {
                throw new IllegalArgumentException("Item requires a supplier phone");
            }
        }
        if (values.size() == 0) {
            return 0;
        }
        SQLiteDatabase database = mDbHelper.getWritableDatabase();

        // Perform the update on the database and get the number of rows affected
        int rowsUpdated = database.update(InventoryContract.ItemEntry.TABLE_NAME, values, selection, selectionArgs);
        // If 1 or more rows were updated, then notify all listeners that the data at the
        // given URI has changed
        if (rowsUpdated != 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }

        // Return the number of rows updated
        return rowsUpdated;

    }
}