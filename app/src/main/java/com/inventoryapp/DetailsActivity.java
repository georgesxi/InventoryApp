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
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.text.InputType;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.inventoryapp.data.InventoryContract;

/**
 * THIS CLASS HANDLES THE DETAILS ACTIVITY
 */

public class DetailsActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {

    /**
     * Identifier for the item data loader
     */
    private static final int EXISTING_ITEM_LOADER = 0;

    /**
     * Content URI for the existing item (null if it's a new pet)
     */
    private Uri mCurrentItemUri;

    private TextView mProductName;
    private TextView mPrice;
    private TextView mQuantity;
    private TextView mListQuantity;
    private TextView mSupplier;
    private TextView mEmail;
    private TextView mPhone;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.details_layout);
        setTitle(getString(R.string.product_details));

        // Setup FAB to open InventoryEditorActivity
        FloatingActionButton fab = findViewById(R.id.delete_product_fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDeleteConfirmationDialog();

            }
        });

        // Find all relevant views that we will need to read
        mProductName = (TextView) findViewById(R.id.details_product_name);
        mPrice = (TextView) findViewById(R.id.details_item_price);
        mListQuantity = (TextView) findViewById(R.id.details_quantity);
        mQuantity = (TextView) findViewById(R.id.details_quantity_change);
        mSupplier = (TextView) findViewById(R.id.details_supplier_name);
        mEmail = (TextView) findViewById(R.id.details_email);
        mPhone = (TextView) findViewById(R.id.details_phone);

        Intent intent = getIntent();
        mCurrentItemUri = intent.getData();

        FloatingActionButton increaseButton = (FloatingActionButton) findViewById(R.id.increase);
        FloatingActionButton decreaseButton = (FloatingActionButton) findViewById(R.id.decrease);

        decreaseButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(DetailsActivity.this);
                final EditText edittext = new EditText(v.getContext());
                edittext.setInputType(InputType.TYPE_CLASS_NUMBER);
                builder.setMessage(getString(R.string.selling_quantity));
                builder.setView(edittext);

                builder.setPositiveButton(getString(R.string.confirm), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        int decrease;
                        if (TextUtils.isEmpty(edittext.getText().toString().trim())) {
                            decrease = 0;
                        } else {
                            decrease = Integer.parseInt(edittext.getText().toString().trim());
                        }
                        String[] projection =
                                {
                                        InventoryContract.ItemEntry._ID,
                                        InventoryContract.ItemEntry.COLUMN_PRODUCT_NAME,
                                        InventoryContract.ItemEntry.COLUMN_QUANTITY,
                                        InventoryContract.ItemEntry.COLUMN_PRICE,
                                        InventoryContract.ItemEntry.COLUMN_SUPPLIER,
                                        InventoryContract.ItemEntry.COLUMN_EMAIL,
                                        InventoryContract.ItemEntry.COLUMN_PHONE
                                };
                        Cursor cursor = getContentResolver().query(mCurrentItemUri, projection, null, null, null);
                        if (cursor.moveToFirst()) {
                            do {
                                int intName = cursor.getColumnIndex(InventoryContract.ItemEntry.COLUMN_PRODUCT_NAME);
                                int intPrice = cursor.getColumnIndex(InventoryContract.ItemEntry.COLUMN_PRICE);
                                int intQuantity = cursor.getColumnIndex(InventoryContract.ItemEntry.COLUMN_QUANTITY);
                                int intSupplier = cursor.getColumnIndex(InventoryContract.ItemEntry.COLUMN_SUPPLIER);
                                int intEmail = cursor.getColumnIndex(InventoryContract.ItemEntry.COLUMN_EMAIL);
                                int intPhone = cursor.getColumnIndex(InventoryContract.ItemEntry.COLUMN_PHONE);

                                String name = cursor.getString(intName);
                                int price = cursor.getInt(intPrice);
                                int quantity = cursor.getInt(intQuantity);
                                String supplier = cursor.getString(intSupplier);
                                String email = cursor.getString(intEmail);
                                int phone = cursor.getInt(intPhone);

                                if (quantity - decrease >= 0) {
                                    int newQuantity = (quantity - decrease);
                                    ContentValues values2 = new ContentValues();
                                    values2.put(InventoryContract.ItemEntry.COLUMN_PRODUCT_NAME, name);
                                    values2.put(InventoryContract.ItemEntry.COLUMN_PRICE, price);
                                    values2.put(InventoryContract.ItemEntry.COLUMN_QUANTITY, newQuantity);
                                    values2.put(InventoryContract.ItemEntry.COLUMN_SUPPLIER, supplier);
                                    values2.put(InventoryContract.ItemEntry.COLUMN_EMAIL, email);
                                    values2.put(InventoryContract.ItemEntry.COLUMN_PHONE, phone);

                                    getContentResolver().update(mCurrentItemUri, values2, null, null);
                                } else if (quantity - decrease < 0) {
                                    Toast.makeText(DetailsActivity.this, "Change value! Only" + quantity + " products available !", Toast.LENGTH_LONG).show();

                                }

                            }
                            while (cursor.moveToNext());
                        }
                    }
                });
                builder.setNegativeButton(getString(R.string.cancel), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });

                AlertDialog alertDialog = builder.create();
                alertDialog.show();

            }
        });

        increaseButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(DetailsActivity.this);
                final EditText editText1 = new EditText(v.getContext());
                editText1.setInputType(InputType.TYPE_CLASS_NUMBER);
                builder.setMessage(getString(R.string.received_quantity));
                builder.setView(editText1);

                builder.setPositiveButton(getString(R.string.confirm), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        int addition;
                        if (TextUtils.isEmpty(editText1.getText().toString().trim())) {
                            addition = 0;
                        } else {
                            addition = Integer.parseInt(editText1.getText().toString().trim());
                        }

                        String[] projection = {
                                InventoryContract.ItemEntry._ID,
                                InventoryContract.ItemEntry.COLUMN_PRODUCT_NAME,
                                InventoryContract.ItemEntry.COLUMN_PRICE,
                                InventoryContract.ItemEntry.COLUMN_QUANTITY,
                                InventoryContract.ItemEntry.COLUMN_SUPPLIER,
                                InventoryContract.ItemEntry.COLUMN_EMAIL,
                                InventoryContract.ItemEntry.COLUMN_PHONE
                        };

                        Cursor cursor = getContentResolver().query(mCurrentItemUri, projection, null, null, null);
                        if (cursor.moveToFirst()) {
                            do {
                                int intName = cursor.getColumnIndex(InventoryContract.ItemEntry.COLUMN_PRODUCT_NAME);
                                int intPrice = cursor.getColumnIndex(InventoryContract.ItemEntry.COLUMN_PRICE);
                                int intQuantity = cursor.getColumnIndex(InventoryContract.ItemEntry.COLUMN_QUANTITY);
                                int intSupplier = cursor.getColumnIndex(InventoryContract.ItemEntry.COLUMN_SUPPLIER);
                                int intEmail = cursor.getColumnIndex(InventoryContract.ItemEntry.COLUMN_EMAIL);
                                int intPhone = cursor.getColumnIndex(InventoryContract.ItemEntry.COLUMN_PHONE);

                                String name = cursor.getString(intName);
                                int price = cursor.getInt(intPrice);
                                int quantity = cursor.getInt(intQuantity);
                                String supplier = cursor.getString(intSupplier);
                                String email = cursor.getString(intEmail);
                                int phone = cursor.getInt(intPhone);

                                int currentQuantity = quantity + addition;


                                ContentValues values3 = new ContentValues();
                                values3.put(InventoryContract.ItemEntry.COLUMN_PRODUCT_NAME, name);
                                values3.put(InventoryContract.ItemEntry.COLUMN_PRICE, price);
                                values3.put(InventoryContract.ItemEntry.COLUMN_QUANTITY, currentQuantity);
                                values3.put(InventoryContract.ItemEntry.COLUMN_SUPPLIER, supplier);
                                values3.put(InventoryContract.ItemEntry.COLUMN_EMAIL, email);
                                values3.put(InventoryContract.ItemEntry.COLUMN_PHONE, phone);

                                getContentResolver().update(mCurrentItemUri, values3, null, null);


                            }
                            while (cursor.moveToNext());
                        }
                    }
                });

                builder.setNegativeButton(getString(R.string.cancel), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        dialog.dismiss();
                    }
                });

                AlertDialog alertDialog = builder.create();
                alertDialog.show();
            }
        });
        getLoaderManager().initLoader(EXISTING_ITEM_LOADER, null, DetailsActivity.this);

        FloatingActionButton fab1 = findViewById(R.id.call_supplier_fab);
        fab1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String phone_no = String.format("tel: %s",
                        mPhone.getText().toString());
                Intent callIntent = new Intent(Intent.ACTION_DIAL);
                callIntent.setData(Uri.parse(phone_no));
                if (callIntent.resolveActivity(getPackageManager()) != null) {
                    startActivity(callIntent);
                } else {
                    Toast.makeText(DetailsActivity.this, "Can't resolve app for ACTION_DIAL Intent.", Toast.LENGTH_LONG).show();

                }

            }
        });

    } //End

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.detailsactivity_menu, menu);
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
            case R.id.delete_current_product:
                showDeleteConfirmationDialog();
                return true;
            case R.id.edit_current_product:
                Intent intent = new Intent(DetailsActivity.this, InventoryEditorActivity.class);
                intent.setData(mCurrentItemUri);
                startActivity(intent);
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void showDeleteConfirmationDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(getString(R.string.delete_dialog_msg));
        builder.setPositiveButton(getString(R.string.delete), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int id) {
                deleteItem();
            }
        });
        builder.setNegativeButton(getString(R.string.cancel), new DialogInterface.OnClickListener() {
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

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {

        String[] projection = {
                InventoryContract.ItemEntry._ID,
                InventoryContract.ItemEntry.COLUMN_PRODUCT_NAME,
                InventoryContract.ItemEntry.COLUMN_QUANTITY,
                InventoryContract.ItemEntry.COLUMN_PRICE,
                InventoryContract.ItemEntry.COLUMN_SUPPLIER,
                InventoryContract.ItemEntry.COLUMN_EMAIL,
                InventoryContract.ItemEntry.COLUMN_PHONE};

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
                mPrice.setText(String.valueOf(price) + " $");
                mQuantity.setText(String.valueOf(quantity));
                mListQuantity.setText((String.valueOf(quantity)));
                mEmail.setText(email);
                mPhone.setText(String.valueOf(phone));
                mSupplier.setText(String.valueOf(supplier));

            } while (cursor.moveToNext());
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        // If the loader is invalidated, clear out all the data from the input fields.
        mProductName.setText("");
        mPrice.setText("");
        mQuantity.setText("");
        mListQuantity.setText("");
        mEmail.setText("");
        mPhone.setText("");
        mSupplier.setText("");
    }
}