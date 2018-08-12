package com.example.android.inventoryappr.fragments;

import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.drawable.GradientDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.example.android.inventoryappr.R;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;

import static com.example.android.inventoryappr.data.ProductContract.ProductEntry.COLUMN_PROD_ID;
import static com.example.android.inventoryappr.data.ProductContract.ProductEntry.COLUMN_PROD_NAME;
import static com.example.android.inventoryappr.data.ProductContract.ProductEntry.COLUMN_PROD_PRICE;
import static com.example.android.inventoryappr.data.ProductContract.ProductEntry.COLUMN_PROD_QUANTITY;
import static com.example.android.inventoryappr.data.ProductContract.ProductEntry.COLUMN_SUPPLIER_NAME;
import static com.example.android.inventoryappr.data.ProductContract.ProductEntry.COLUMN_SUPPLIER_PHONE;

/**
 * The {@link ProductListFragment} displays detailed information of the single product
 * (e.g. Name, Price, Quantity, Supplier Name, etc.).
 * Provides the way to change quantity of the product.
 */
public class ProductDetailsFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {

    /* Tag for the log messages */
    private static final String LOG_TAG = ProductDetailsFragment.class.getSimpleName();
    /* CursorLoader ID */
    private static final int LOADER_ID = 1;

    @BindView(R.id.product_name)
    TextView productNameView;
    @BindView(R.id.price)
    TextView priceView;
    @BindView(R.id.quantity)
    TextView quantityView;
    @BindView(R.id.supplier_name)
    TextView supplierNameView;
    @BindView(R.id.supplier_phone)
    TextView supplierPhoneView;
    private Unbinder unbinder;

    /* The URI for the product in the database */
    private Uri currentProductUri;
    /* Values of the product in the database */
    String currentProductName;
    int currentProductPrice;
    int currentProductQuantity;
    String currentSupplierName;
    String currentSupplierPhone;
    /* New product quantity set by the user */
    int quantityTracker;

    /* Reference to the Listener implemented in the holding activity */
    private OnEditOptionSelectedListener onEditOptionSelectedListener;

    /**
     * Empty constructor for the fragment.
     */
    public ProductDetailsFragment() {
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Check for passed arguments to this fragment
        Bundle argsBundle = getArguments();
        if (argsBundle != null) {
            currentProductUri = Uri.parse(argsBundle.getString("productUri"));
            Log.d(LOG_TAG, "TEST: currentProductUri is " + currentProductUri);
        }
        // Declare that this fragment needs to edit OptionsMenu, thus its onCreateOptionsMenu will be called
        setHasOptionsMenu(true);
    }
    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        menu.clear();
        // Inflate the menu options from the res/menu/product_details_menu.xml file
        // This adds menu items to the app bar
        inflater.inflate(R.menu.product_details_menu, menu);

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            // Respond to a click on the "Edit" menu option
            case R.id.action_edit:
                // Send the callback to the holding activity to launch EditProductFragment
                onEditOptionSelectedListener.onEditOptionSelected(currentProductUri);
                return true;
            // Respond to a click on the "Delete" menu option
            case R.id.action_delete:
                showDeleteConfirmationDialog();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Nullable
    @Override
    /* Put initial UI state of the fragment here (i.e. inflate layout views). */
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_product_details, container, false);
        // Bind views in the rootView(fragment layout) to this fragment
        unbinder = ButterKnife.bind(this, rootView);
        return rootView;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        // Clean up. Unbind views from this fragment before the fragment is destroyed
        unbinder.unbind();
    }

    @Override
    /* Put UI changing logic here (e.g. setAdapter to a ListView, etc) and
       code that needs activity created, e.g. getActivity(). */
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        // Start loading data
        getLoaderManager().initLoader(LOADER_ID, null, this);

        // Set the app bar title for this fragment
        getActivity().setTitle(getString(R.string.app_bar_title_product_details));
    }

    @NonNull
    @Override
    public Loader<Cursor> onCreateLoader(int i, @Nullable Bundle args) {
        // Columns to extract from a table
        String[] projection = {
                COLUMN_PROD_ID,
                COLUMN_PROD_NAME,
                COLUMN_PROD_PRICE,
                COLUMN_PROD_QUANTITY,
                COLUMN_SUPPLIER_NAME,
                COLUMN_SUPPLIER_PHONE
        };

        // This loader will execute the ContentProvider's query method on a background thread
        return new CursorLoader(getActivity(),  // Parent activity context
                currentProductUri,              // Query URI for the current product
                projection,                     // Columns to include in the resulting Cursor
                null,                   // No selection clause (i.e. WHERE _id=?)
                null,                // No selection arguments
                null);                  // Default sort order
    }

    @Override
    public void onLoadFinished(@NonNull Loader<Cursor> loader, Cursor data) {
        // Bail early if the cursor is null or there is less than 1 row in the cursor
        if (data == null || data.getCount() < 0) {
            Toast.makeText(getActivity(), R.string.toast_error_getting_product_info, Toast.LENGTH_LONG).show();
            Log.e(LOG_TAG, "Error getting the product info");
            return;
        }
        // Proceed with moving to the first row of the cursor and reading data from it
        // (This should be the only row in the cursor)
        if (data.moveToFirst()) {
            // Extract out the values from the Cursor for the given column index
            currentProductName = data.getString(data.getColumnIndex(COLUMN_PROD_NAME));
            currentProductPrice = data.getInt(data.getColumnIndex(COLUMN_PROD_PRICE));
            currentProductQuantity = data.getInt(data.getColumnIndex(COLUMN_PROD_QUANTITY));
            currentSupplierName = data.getString(data.getColumnIndex(COLUMN_SUPPLIER_NAME));
            currentSupplierPhone = data.getString(data.getColumnIndex(COLUMN_SUPPLIER_PHONE));

            // Instantiate quantityTracker
            quantityTracker = currentProductQuantity;

            // Set the proper background color on the quantity pane
            String productQuantityString = String.valueOf(currentProductQuantity);
            GradientDrawable quantityPane = (GradientDrawable) quantityView.getBackground();
            quantityPane.setColor(getQuantityColor(getContext(), productQuantityString));

            // Update the views on the screen with the values from the database
            productNameView.setText(currentProductName);
            priceView.setText(String.valueOf(currentProductPrice));
            quantityView.setText(String.valueOf(currentProductQuantity));
            supplierNameView.setText(currentSupplierName);
            supplierPhoneView.setText(currentSupplierPhone);
        } else {
            Toast.makeText(getActivity(), R.string.toast_error_no_such_product, Toast.LENGTH_LONG).show();
            Log.e(LOG_TAG, "No such product in the database");
        }
    }

    @Override
    public void onLoaderReset(@NonNull Loader<Cursor> loader) {
        // If the loader is invalidated, clear out all the data from the input fields
        productNameView.setText("");
        priceView.setText("");
        quantityView.setText("");
        supplierNameView.setText("");
        supplierPhoneView.setText("");
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        // This makes sure that the holding activity has implemented
        // the callback interface. If not, it throws an exception
        try {
            // Get the reference to the activity's listener when the activity and this fragment are connected
            // We can cast an Activity to OnEditOptionSelectedListener only if the activity implements this Listener
            onEditOptionSelectedListener = (OnEditOptionSelectedListener) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString() + " must implement OnEditOptionSelectedListener");
        }
    }

    /**
     * Listener interface that should be implemented by holding activity (InvetoryActivity in this case)
     * in order to get callbacks for the specified here actions.
     */
    public interface OnEditOptionSelectedListener {

        /**
         * Callback. When the Edit option from the Menu is selected.
         *
         * @param productUri URI of the currently selected product.
         */
        public void onEditOptionSelected(Uri productUri);
    }

    /**
     * Show a confirmation dialog to the user with options to proceed.
     */
    private void showDeleteConfirmationDialog() {
        // Create an AlertDialog.Builder and set the message, and click listeners
        // for the positive and negative buttons on the dialog.
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setMessage(R.string.delete_dialog_msg);
        builder.setPositiveButton(R.string.delete, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked the "Delete" button, so delete the product
                deleteProduct();
            }
        });
        builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked the "Cancel" button, so dismiss the dialog
                // and continue editing the product.
                if (dialog != null) {
                    dialog.dismiss();
                }
            }
        });
        // Create and show the AlertDialog
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    /**
     * Delete current product from the database.
     */
    private void deleteProduct() {
        // Only perform the deletion if this is an existing product
        if (currentProductUri != null) {
            int rowsDeleted = getActivity().getContentResolver().delete(currentProductUri, null, null);
            Log.d(LOG_TAG, "TEST: deleteProduct(). The number of rows deleted is " + rowsDeleted);
            switch (rowsDeleted){
                // Show a toast message depending on whether or not product deletion was successful
                case 1:
                    // Success
                    Toast.makeText(getActivity(), R.string.toast_details_deletion_success, Toast.LENGTH_LONG).show();
                    break;
                case 0:
                    // Fail
                    Toast.makeText(getActivity(), R.string.toast_details_deletion_failed, Toast.LENGTH_LONG).show();
                    break;
                default:
                    // Unexpected behavior
                    // return value shouldn't be more than 1 because we delete only one product at a time
                    Toast.makeText(getActivity(), R.string.toast_details_deletion_error, Toast.LENGTH_LONG).show();
                    Log.e(LOG_TAG, "Error deleting product");
            }
            // Finish this fragment
            getFragmentManager().popBackStack();
        }
    }

    /**
     * Call to a supplier of the product.
     */
    @OnClick(R.id.call_to_supplier_btn)
    public void callToSupplier() {
        Intent callIntent = new Intent(Intent.ACTION_DIAL);
        callIntent.setData(Uri.parse("tel:" + currentSupplierPhone));
        startActivity(callIntent);
    }

    /**
     * Decrease product quantity by 1 and display a new value.
     */
    @OnClick(R.id.decrease_quantity_btn)
    public void decreaseQuantity(){
        // Check for invalid numbers
        if (quantityTracker > 0) {
            quantityTracker--;
            quantityView.setText(String.valueOf(quantityTracker));
        } else {
            Toast.makeText(getActivity(), "Quantity cannot be negative", Toast.LENGTH_SHORT).show();
        }
    }
    /**
     * Increase product quantity by 1 and display a new value.
     */
    @OnClick(R.id.increase_quantity_btn)
    public void increaseQuantity(){
        // Check for invalid numbers
        if (quantityTracker < Integer.MAX_VALUE) {
            quantityTracker++;
            quantityView.setText(String.valueOf(quantityTracker));
        } else {
            Toast.makeText(getActivity(), "MAX Quantity reached", Toast.LENGTH_SHORT).show();
        }
    }
    /**
     * Update product quantity value in the database.
     */
    private void updateQuantity(){
        // Use a ContentValues object as a vessel for the data to be inserted
        ContentValues values;
        // Proceed if
        if (quantityTracker != currentProductQuantity // if quantity needs an update
                && quantityTracker >= 0     // if quantity isn't a negative value
                && currentProductUri != null) { // if the product hasn't been deleted
            values = new ContentValues();
            values.put(COLUMN_PROD_QUANTITY, quantityTracker);
        } else {
            // If nothing to update or new quantity < 0, do nothing and return
            Log.d(LOG_TAG, "TEST: currentProductQuantity " + currentProductQuantity + " | " + quantityTracker + " quantityTracker");
            return;
        }

        // Update database for the product URI
        int rowsUpdated = getActivity().getContentResolver().update(currentProductUri,
                values,
                null,
                null);
        Log.d(LOG_TAG, "TEST: The number of updated rows is " + rowsUpdated);
        switch (rowsUpdated) {
            // Show a toast message depending on whether or not the updating was successful
            case 1:
                // Success
                Toast.makeText(getActivity(), "Product updated", Toast.LENGTH_LONG).show();
                break;
            case 0:
                // Fail
                Toast.makeText(getActivity(), "Update failed", Toast.LENGTH_LONG).show();
                break;
            default:
                // Unexpected behavior
                // return value shouldn't be more than 1 because we update only one product at a time
                Toast.makeText(getActivity(), "Error updating product", Toast.LENGTH_LONG).show();
                Log.e(LOG_TAG, "Error updating product");
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        // Update to a new quantity value before leaving the fragment
        updateQuantity();
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
