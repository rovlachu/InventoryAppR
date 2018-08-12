package com.example.android.inventoryappr;

import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.graphics.drawable.GradientDrawable;
import android.net.Uri;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CursorAdapter;
import android.widget.TextView;
import android.widget.Toast;

import butterknife.BindView;
import butterknife.ButterKnife;

import static com.example.android.inventoryappr.data.ProductContract.ProductEntry.COLUMN_PROD_ID;
import static com.example.android.inventoryappr.data.ProductContract.ProductEntry.COLUMN_PROD_NAME;
import static com.example.android.inventoryappr.data.ProductContract.ProductEntry.COLUMN_PROD_PRICE;
import static com.example.android.inventoryappr.data.ProductContract.ProductEntry.COLUMN_PROD_QUANTITY;
import static com.example.android.inventoryappr.data.ProductContract.ProductEntry.CONTENT_URI;

/**
 * {@link ProductCursorAdapter} is an adapter for a list or grid view
 * that uses a {@link Cursor} of product data as its data source. This adapter knows
 * how to create list items for each row of product data in the {@link Cursor}.
 */
public class ProductCursorAdapter extends CursorAdapter {

    /* Tag for the log messages */
    private static final String LOG_TAG = ProductCursorAdapter.class.getSimpleName();

    /**
     * Constructs a new {@link ProductCursorAdapter}.
     *
     * @param context The context
     * @param cursor       The cursor from which to get the data.
     */
    public ProductCursorAdapter(Context context, Cursor cursor) {
        super(context, cursor, 0 /* flags */);
    }

    /**
     * ViewHolder helps reduce findViewById() operations in the Adapter.
     */
    static class ViewHolder {
        // Find all the views in the list_item.xml which we need to populate with the new data
        // and bind the reference on these views to the viewHolder
        @BindView(R.id.product_name) TextView productNameView;
        @BindView(R.id.price) TextView priceView;
        @BindView(R.id.quantity) TextView quantityView;
        @BindView(R.id.sell_product_btn) Button sellButton;

        public ViewHolder(View convertView) {
            ButterKnife.bind(this,convertView);
        }
    }

    /**
     * Makes a new blank list item view. No data is set (or bound) to the views yet.
     *
     * @param context app context
     * @param cursor  The cursor from which to get the data. The cursor is already
     *                moved to the correct position.
     * @param parent  The parent to which the new view is attached to
     * @return the newly created list item view.
     */
    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        View newView = LayoutInflater.from(context).inflate(R.layout.list_item, parent, false);
        // Bind the reference on newView's child views to the viewHolder
        ViewHolder viewHolder = new ViewHolder(newView);
        newView.setTag(viewHolder);
        return newView;
    }

    /**
     * Populates list item view (returned from newView()) with product data (extracted from the cursor).
     *
     * @param view    Existing view, returned earlier by newView() method
     * @param context app context
     * @param cursor  The cursor from which to get the data. The cursor is already moved to the
     *                correct row.
     */
    @Override
    public void bindView(View view, final Context context, Cursor cursor) {
        // Extract properties from the cursor
        String productName = cursor.getString(cursor.getColumnIndex(COLUMN_PROD_NAME));
        String priceString = cursor.getString(cursor.getColumnIndex(COLUMN_PROD_PRICE));
        String quantityString = cursor.getString(cursor.getColumnIndex(COLUMN_PROD_QUANTITY));
        Integer productId = cursor.getInt(cursor.getColumnIndex(COLUMN_PROD_ID));

        // Get the ViewHolder object associated with this view that holds references to its child views
        ViewHolder viewHolder = (ViewHolder) view.getTag();

        // Use the ViewHolder to populate the child views with the extracted properties
        viewHolder.productNameView.setText(productName);
        viewHolder.priceView.setText(priceString);
        viewHolder.quantityView.setText(quantityString);

        // Set the proper background color on the quantity pane
        GradientDrawable quantityPane = (GradientDrawable) viewHolder.quantityView.getBackground();
        quantityPane.setColor(getQuantityColor(context, quantityString));

        // Set productId Integer object as a Tag on the button to use it when needed
        viewHolder.sellButton.setTag(productId);
        // Set ClickListener to perform product sales from the list view
        viewHolder.sellButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Get the current product quantity
                View listItem = (View) v.getParent();
                TextView quantityView = listItem.findViewById(R.id.quantity);
                int quantityInt = Integer.parseInt(quantityView.getText().toString());
                // Check for valid quantity
                if (quantityInt == 0) {
                    Toast.makeText(context, R.string.toast_no_products_for_sale, Toast.LENGTH_LONG).show();
                    return;
                }
                int newQuantity = quantityInt - 1;
                ContentValues values = new ContentValues();
                values.put(COLUMN_PROD_QUANTITY, newQuantity);

                // Get the product URI
                Integer currentProductId = (Integer) v.getTag();
                Uri currentProductUri = ContentUris.withAppendedId(CONTENT_URI, currentProductId);

                // Update database for the product URI
                int rowsUpdated = context.getContentResolver().update(currentProductUri,
                        values,
                        null,
                        null);
                Log.d(LOG_TAG, "TEST: sellProductButton. The number of updated rows is " + rowsUpdated);
                switch (rowsUpdated) {
                    // Show a toast message depending on whether or not the updating was successful
                    case 1:
                        // Success
                        Toast.makeText(context, R.string.toast_sale_success, Toast.LENGTH_LONG).show();
                        quantityView.setText(String.valueOf(newQuantity));
                        break;
                    case 0:
                        // Fail
                        Toast.makeText(context, R.string.toast_sale_failed, Toast.LENGTH_LONG).show();
                        break;
                    default:
                        // Unexpected behavior
                        // return value shouldn't be more than 1 because we update only one product at a time
                        Toast.makeText(context, R.string.toast_sale_error, Toast.LENGTH_LONG).show();
                        Log.e(LOG_TAG, "Error selling product");
                }
            }
        });
    }

    /**
     * Return color according to quantity.
     *
     * @param context Context to reach the resources.
     * @param quantityString The product quantity as a string.
     * @return Color object.
     */
    private int getQuantityColor(Context context, String quantityString) {
        int quantityColorResourceId;
        int quantityRange = quantityString.length();
        switch (quantityRange) {
            case 0:
            case 1:
                quantityColorResourceId = R.color.quantityTo10;
                break;
            case 2:
                quantityColorResourceId = R.color.quantityTo100;
                break;
            case 3:
                quantityColorResourceId = R.color.quantityTo1k;
                break;
            case 4:
                quantityColorResourceId = R.color.quantityTo10k;
                break;
            case 5:
                quantityColorResourceId = R.color.quantityTo100k;
                break;
            case 6:
                quantityColorResourceId = R.color.quantityTo1m;
                break;
            default:
                quantityColorResourceId = R.color.colorPrimary;
        }
        // Get color object from the resource ID and return it
        return context.getResources().getColor(quantityColorResourceId);
    }
}
