package com.inventoryapp;

import android.app.AlertDialog;
import android.app.LoaderManager;
import android.content.ContentValues;
import android.content.CursorLoader;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.NavUtils;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.inventoryapp.data.InventoryContract;

public class InventoryEditorActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {

    private EditText mProductName;
    private EditText mQuantity;
    private EditText mPrice;
    private EditText mSupplier;
    private EditText mEmail;
    private EditText mPhone;

    /**
     * Identifier for the item data loader
     */
    private static final int EXISTING_ITEM_LOADER = 0;

    /**
     * Content URI for the existing item (null if it's a new pet)
     */
    private Uri mCurrentItemUri;
    private boolean mItemHasChanged = false;

    /**
     * OnTouchListener that listens for any user touches on a View, implying that they are modifying
     * the view, and we change the mItemHasChanged boolean to true.
     */

    private View.OnTouchListener mTouchListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View v, MotionEvent event) {
            mItemHasChanged = true;
            return false;
        }
    };

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_inventory_editor);

        Intent intent = getIntent();
        mCurrentItemUri = intent.getData();

        if (mCurrentItemUri == null) {
            setTitle("Add Item");
            invalidateOptionsMenu();
        } else {
            setTitle("Edit Item");
            getLoaderManager().initLoader(EXISTING_ITEM_LOADER, null, this);
        }

        // Find all relevant views that we will need to read user input from
        mProductName = (EditText) findViewById(R.id.edit_product_name);
        mQuantity = (EditText) findViewById(R.id.edit_quantity_change);
        mPrice = (EditText) findViewById(R.id.edit_item_price);
        mSupplier = (EditText) findViewById(R.id.edit_supplier_name);
        mEmail = (EditText) findViewById(R.id.edit_email);
        mPhone = (EditText) findViewById(R.id.edit_phone);

        mProductName.setOnTouchListener(mTouchListener);
        mQuantity.setOnTouchListener(mTouchListener);
        mPrice.setOnTouchListener(mTouchListener);
        mSupplier.setOnTouchListener(mTouchListener);
        mEmail.setOnTouchListener(mTouchListener);
        mPhone.setOnTouchListener(mTouchListener);

    }

    private void saveItem() {

        String productName = mProductName.getText().toString().trim();
        String quantityString = mQuantity.getText().toString().trim();
        String priceString = mPrice.getText().toString().trim();
        String email = mEmail.getText().toString().trim();
        String phone = mPhone.getText().toString().trim();
        String supplier = mSupplier.getText().toString().trim();

        if (mCurrentItemUri == null && TextUtils.isEmpty(productName) && TextUtils.isEmpty(quantityString) && TextUtils.isEmpty(priceString) &&
                TextUtils.isEmpty(email) && TextUtils.isEmpty(phone) && TextUtils.isEmpty(supplier)) {
            Toast.makeText(this, "You need to complete all the fields or the item will not be saved", Toast.LENGTH_LONG).show();
            return;
        }

        ContentValues values = new ContentValues();
        values.put(InventoryContract.ItemEntry.COLUMN_PRODUCT_NAME, productName);
        values.put(InventoryContract.ItemEntry.COLUMN_QUANTITY, quantityString);
        values.put(InventoryContract.ItemEntry.COLUMN_PRICE, priceString);
        values.put(InventoryContract.ItemEntry.COLUMN_SUPPLIER, supplier);
        values.put(InventoryContract.ItemEntry.COLUMN_EMAIL, email);
        values.put(InventoryContract.ItemEntry.COLUMN_PHONE, phone);


        if (mCurrentItemUri == null) {
            Uri newUri = getContentResolver().insert(InventoryContract.ItemEntry.CONTENT_URI, values);
            if (newUri == null) {
                Toast.makeText(this, "Error saving Item", Toast.LENGTH_LONG).show();
            } else {
                Toast.makeText(this, "Item added successfully", Toast.LENGTH_LONG).show();
            }
        } else {
            // Otherwise this is an EXISTING item, so update the item with content URI: mCurrentItemUri
            // and pass in the new ContentValues.
            int rowsAffected = getContentResolver().update(
                    mCurrentItemUri,
                    values,
                    null,
                    null);
            if (rowsAffected == 0) {
                Toast.makeText(this, "Item update failed", Toast.LENGTH_LONG).show();
            } else {
                Toast.makeText(this, "Item update successful", Toast.LENGTH_LONG).show();
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_inventory_editor, menu);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
        if (mCurrentItemUri == null) {
            MenuItem menuItem = menu.findItem(R.id.action_delete);
            menuItem.setVisible(false);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_save:

                String productName = mProductName.getText().toString().trim();
                String quantityString = mQuantity.getText().toString().trim();
                String priceString = mPrice.getText().toString().trim();
                String email = mEmail.getText().toString().trim();
                String phone = mPhone.getText().toString().trim();
                String supplier = mSupplier.getText().toString().trim();


                if ((TextUtils.isEmpty(productName)) || (quantityString.length() < 1) || (TextUtils.isEmpty(priceString)) || (TextUtils.isEmpty(phone)) || (TextUtils.isEmpty(email)) || (TextUtils.isEmpty(supplier))) {
                    Toast.makeText(this, "Please do not leave empty fields!", Toast.LENGTH_LONG).show();
                } else {
                    saveItem();
                    finish();
                }
                return true;
            case R.id.action_delete:
                // Pop up confirmation dialog for deletion
                showDeleteConfirmationDialog();
                return true;
            case android.R.id.home:
                // If the item hasn't changed, continue with navigating up to parent activity
                if (!mItemHasChanged) {
                    NavUtils.navigateUpFromSameTask(this);
                    return true;
                }
                // Otherwise if there are unsaved changes:
                DialogInterface.OnClickListener discardButtonClickListener =
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                // User clicked "Discard" button, navigate to parent activity.
                                NavUtils.navigateUpFromSameTask(InventoryEditorActivity.this);
                            }
                        };
                showUnsavedChangesDialog(discardButtonClickListener);
                return true;

        }

        return super.onOptionsItemSelected(item);
    }

    private void showDeleteConfirmationDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.delete_dialog_msg);
        builder.setPositiveButton(R.string.delete, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int id) {
                deleteItem();
            }
        });
        builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int id) {
                // User clicked the "Cancel" button, so dismiss the dialog
                if (dialog != null) {
                    dialog.dismiss();
                }
            }
        });

        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    private void deleteItem() {
        if (mCurrentItemUri != null) {
            int rowsDeleted = getContentResolver().delete(mCurrentItemUri, null, null);
            // Show a toast message depending on whether or not the delete was successful.
            if (rowsDeleted == 0) {
                Toast.makeText(this, getString(R.string.editor_delete_item_failed),
                        Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, getString(R.string.editor_delete_item_successful),
                        Toast.LENGTH_SHORT).show();
            }
            finish();
        }
    }

    private void showUnsavedChangesDialog(DialogInterface.OnClickListener discardButtonClickListener) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.unsaved_changes_dialog_msg);
        builder.setPositiveButton(R.string.discard, discardButtonClickListener);
        builder.setNegativeButton(R.string.keep_editing, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int id) {
                if (dialog != null) {
                    dialog.dismiss();
                }
            }
        });
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {

        String[] projection = {
                InventoryContract.ItemEntry._ID,
                InventoryContract.ItemEntry.COLUMN_PRODUCT_NAME,
                InventoryContract.ItemEntry.COLUMN_QUANTITY,
                InventoryContract.ItemEntry.COLUMN_PRICE,
                InventoryContract.ItemEntry.COLUMN_SUPPLIER,
                InventoryContract.ItemEntry.COLUMN_EMAIL,
                InventoryContract.ItemEntry.COLUMN_PHONE
        };

        return new CursorLoader(this,
                mCurrentItemUri,
                projection,
                null,
                null,
                null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
//Abort if the cursor is null or less than 1 row
        if (cursor == null || cursor.getCount() < 1) {
            return;
        }
        if (cursor.moveToFirst()) {
            do {
                // Find the columns of item attributes
                int nameColumnIndex = cursor.getColumnIndex(InventoryContract.ItemEntry.COLUMN_PRODUCT_NAME);
                int quantityColumnIndex = cursor.getColumnIndex(InventoryContract.ItemEntry.COLUMN_QUANTITY);
                int priceColumnIndex = cursor.getColumnIndex(InventoryContract.ItemEntry.COLUMN_PRICE);
                int supplierColumnIndex = cursor.getColumnIndex(InventoryContract.ItemEntry.COLUMN_SUPPLIER);
                int emailColumnIndex = cursor.getColumnIndex(InventoryContract.ItemEntry.COLUMN_EMAIL);
                int phoneColumnIndex = cursor.getColumnIndex(InventoryContract.ItemEntry.COLUMN_PHONE);

                // Extract out the value from the Cursor for the given column index
                String productName = cursor.getString(nameColumnIndex);
                int quantity = cursor.getInt(quantityColumnIndex);
                int price = cursor.getInt(priceColumnIndex);
                String supplier = cursor.getString(supplierColumnIndex);
                String email = cursor.getString(emailColumnIndex);
                int phone = cursor.getInt(phoneColumnIndex);

                // Update the views on the screen with the values from the database
                mProductName.setText(productName);
                mPrice.setText(String.valueOf(price));
                mQuantity.setText(String.valueOf(quantity));
                mSupplier.setText(supplier);
                mEmail.setText(email);
                mPhone.setText(String.valueOf(phone));
            } while (cursor.moveToNext());
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        // If the loader is invalidated, clear out all the data from the input fields.
        mProductName.setText("");
        mPrice.setText("");
        mQuantity.setText("");
        mSupplier.setText("");
        mEmail.setText("");
        mPhone.setText("");

    }
}