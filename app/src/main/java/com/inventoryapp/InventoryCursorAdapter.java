package com.inventoryapp;

import android.content.Context;
import android.content.DialogInterface;
import android.database.Cursor;
import android.support.v7.app.AlertDialog;
import android.text.InputType;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.inventoryapp.data.InventoryContract;

/**
 * THIS CLASS HANDLES THE INCREASE AND DECREASE ON THE LIST ITEM
 */
public class InventoryCursorAdapter extends CursorAdapter {

    private MainActivity activity = new MainActivity();

    public InventoryCursorAdapter(Context context, Cursor c) {
        super(context, c, 0);
        this.activity = (MainActivity) context;
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        return LayoutInflater.from(context).inflate(R.layout.list_item, parent, false);

    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {

        final long id;
        final int currentQuantity;
        final String mName;
        final int mPrice;
        final String mSupplier;
        final String mEmail;
        final int mPhone;


        // Find fields to populate in inflated template
        TextView nameTextView = (TextView) view.findViewById(R.id.name);
        TextView priceTextView = (TextView) view.findViewById(R.id.price);
        TextView quantityTextView = (TextView) view.findViewById(R.id.quantity);
        ImageView itemSale = (ImageView) view.findViewById(R.id.sell_button);

        id = cursor.getLong(cursor.getColumnIndex(InventoryContract.ItemEntry._ID));
        int nameColumnIndex = cursor.getColumnIndex(InventoryContract.ItemEntry.COLUMN_PRODUCT_NAME);
        int priceColumnIndex = cursor.getColumnIndex(InventoryContract.ItemEntry.COLUMN_PRICE);
        int quantityColumnIndex = cursor.getColumnIndex(InventoryContract.ItemEntry.COLUMN_QUANTITY);
        int supplierColumnIndex = cursor.getColumnIndex(InventoryContract.ItemEntry.COLUMN_SUPPLIER);
        int emailColumnIndex = cursor.getColumnIndex(InventoryContract.ItemEntry.COLUMN_EMAIL);
        int phoneColumnIndex = cursor.getColumnIndex(InventoryContract.ItemEntry.COLUMN_PHONE);


        //Read the item attributes from the cursor for the current item
        String itemName = cursor.getString(nameColumnIndex);
        int itemPrice = cursor.getInt(priceColumnIndex);
        int itemQuantity = cursor.getInt(quantityColumnIndex);
        String supplier = cursor.getString(supplierColumnIndex);
        String email = cursor.getString(emailColumnIndex);
        int phone = cursor.getInt(phoneColumnIndex);

        mName = itemName;
        mPrice = itemPrice;
        currentQuantity = itemQuantity;
        mSupplier = supplier;
        mEmail = email;
        mPhone = phone;

        itemSale.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(v.getContext());
                final EditText edittext = new EditText(v.getContext());
                edittext.setInputType(InputType.TYPE_CLASS_NUMBER);
                builder.setMessage(activity.getString(R.string.selling_quantity));
                builder.setView(edittext);

                builder.setPositiveButton(activity.getString(R.string.confirm), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        int decrease;
                        if (TextUtils.isEmpty(edittext.getText().toString().trim())) {
                            decrease = 0;
                        } else {
                            decrease = Integer.parseInt(edittext.getText().toString().trim());
                        }

                        if (currentQuantity - decrease >= 0) {
                            activity.onSaleClick(id, mName, mPrice, mSupplier, mEmail, mPhone, currentQuantity, decrease);
                            Toast.makeText(activity, "Sell successfull", Toast.LENGTH_SHORT).show();

                        } else if (currentQuantity - decrease < 0) {
                            Toast.makeText(activity, "Only " + currentQuantity + " products available !", Toast.LENGTH_SHORT).show();
                        }

                    }
                });

                builder.setNegativeButton(activity.getString(R.string.cancel), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        if (dialog != null) {
                            dialog.dismiss();
                        }
                    }
                });

                AlertDialog alertDialog = builder.create();
                alertDialog.show();
            }
        });

        // Populate fields with extracted properties
        nameTextView.setText(itemName);
        priceTextView.setText(String.valueOf(itemPrice) + " $");
        quantityTextView.setText(String.valueOf(itemQuantity));
    }
}