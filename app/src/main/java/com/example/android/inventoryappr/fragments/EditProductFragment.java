package com.example.android.inventoryappr.fragments;

import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.TextInputLayout;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.AlertDialog;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.Toast;

import com.example.android.inventoryappr.R;

import java.util.List;

import butterknife.BindView;
import butterknife.BindViews;
import butterknife.ButterKnife;
import butterknife.Unbinder;

import static com.example.android.inventoryappr.data.ProductContract.ProductEntry.COLUMN_PROD_ID;
import static com.example.android.inventoryappr.data.ProductContract.ProductEntry.COLUMN_PROD_NAME;
import static com.example.android.inventoryappr.data.ProductContract.ProductEntry.COLUMN_PROD_PRICE;
import static com.example.android.inventoryappr.data.ProductContract.ProductEntry.COLUMN_PROD_QUANTITY;
import static com.example.android.inventoryappr.data.ProductContract.ProductEntry.COLUMN_SUPPLIER_NAME;
import static com.example.android.inventoryappr.data.ProductContract.ProductEntry.COLUMN_SUPPLIER_PHONE;

/**
 * The {@link ProductListFragment} displays detailed information of a single product
 * (e.g. Name, Price, Quantity, Supplier Name, etc.).
 * Provides the way to edit all the details of the product.
 */
public class EditProductFragment extends CustomFragment implements LoaderManager.LoaderCallbacks<Cursor> {

    /* Tag for the log messages */
    private static final String LOG_TAG = EditProductFragment.class.getSimpleName();
    /* CursorLoader ID */
    private static final int LOADER_ID = 1;

    @BindView(R.id.product_name)
    EditText productNameView;
    @BindView(R.id.price)
    EditText priceView;
    @BindView(R.id.quantity)
    EditText quantityView;
    @BindView(R.id.supplier_name)
    EditText supplierNameView;
    @BindView(R.id.supplier_phone)
    EditText supplierPhoneView;
    @BindViews({ R.id.product_name_text_input_layout, R.id.price_text_input_layout,
            R.id.quantity_text_input_layout, R.id.supplier_name_text_input_layout,
            R.id.supplier_phone_text_input_layout })
    List<TextInputLayout> textInputLayoutList;
    private Unbinder unbinder;

    /* The URI for the product in the database */
    private Uri currentProductUri;
    /* Values of the product in the database */
    String currentProductName;
    int currentProductPrice;
    int currentProductQuantity;
    String currentSupplierName;
    String currentSupplierPhone;

    /* Indicator of changes in the product info */
    private boolean productHasChanged = false;

    /**
     * Empty constructor for the fragment.
     */
    public EditProductFragment() {
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
        // Inflate the menu options from the res/menu/edit_product_menu.xml file
        // This adds menu items to the app bar
        inflater.inflate(R.menu.edit_product_menu, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            // Respond to a click on the "Save" menu option
            case R.id.action_save:
                // Hide the keyboard
                InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(getView().getWindowToken(), 0);
                // Update the product
                if (updateProduct()) {
                    // Finish this fragment when updating is done
                    getFragmentManager().popBackStack();
                }
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Nullable
    @Override
    /* Put initial UI state of the fragment here (i.e. inflate layout views). */
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_edit_product, container, false);
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
        getActivity().setTitle(getString(R.string.app_bar_title_edit_product));
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

            // Update the views on the screen with the values from the database
            productNameView.setText(currentProductName);
            priceView.setText(String.valueOf(currentProductPrice));
            quantityView.setText(String.valueOf(currentProductQuantity));
            supplierNameView.setText(currentSupplierName);
            supplierPhoneView.setText(currentSupplierPhone);

            // Set up productHasChanged listeners
            setupOnEditTextChangedListener(textInputLayoutList);
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

    /**
     * Update current product in the database.
     *
     * @return true when an update was successful or nothing to update, otherwise false.
     */
    private boolean updateProduct() {
        // Get the input values from the fields
        String productNameString = productNameView.getText().toString().trim();
        String priceString = priceView.getText().toString().trim();
        String quantityString = quantityView.getText().toString().trim();
        String supplierNameString = supplierNameView.getText().toString().trim();
        String supplierPhoneString = supplierPhoneView.getText().toString().trim();

        // If fields are empty, prompt the user and return
        if (TextUtils.isEmpty(productNameString)
                || TextUtils.isEmpty(priceString)
                || TextUtils.isEmpty(quantityString)
                || TextUtils.isEmpty(supplierNameString)
                || TextUtils.isEmpty(supplierPhoneString)
                ) {
            Toast.makeText(getActivity(), R.string.toast_no_blank_fields, Toast.LENGTH_LONG).show();
            return false;
        }

        // Price and Quantity cannot be less than 0
        if (priceString.length() > 9) {
            // No more than 9-digit number allowed
            Toast.makeText(getActivity(), R.string.toast_invalid_price, Toast.LENGTH_LONG).show();
            return false;
        }
        int priceInt = Integer.parseInt(priceString);
        if (priceInt < 0) {
            Toast.makeText(getActivity(), R.string.toast_invalid_price, Toast.LENGTH_LONG).show();
            return false;
        }

        if (quantityString.length() > 9) {
            // No more than 9-digit number allowed
            Toast.makeText(getActivity(), R.string.toast_invalid_quantity, Toast.LENGTH_LONG).show();
            return false;
        }
        int quantityInt = Integer.parseInt(quantityString);
        if (quantityInt < 0) {
            Toast.makeText(getActivity(), R.string.toast_invalid_quantity, Toast.LENGTH_LONG).show();
            return false;
        }

        // Use a ContentValues object as a vessel for the data to be inserted
        ContentValues values = new ContentValues();
        // Put a value for update only if it has been changed
        if (!productNameString.equals(currentProductName)) {
            values.put(COLUMN_PROD_NAME, productNameString);
        }
        if (priceInt != currentProductPrice) {
            values.put(COLUMN_PROD_PRICE, priceInt);
        }
        if (quantityInt != currentProductQuantity) {
            values.put(COLUMN_PROD_QUANTITY, quantityInt);
        }
        if (!supplierNameString.equals(currentSupplierName)) {
            values.put(COLUMN_SUPPLIER_NAME, supplierNameString);
        }
        if (!supplierPhoneString.equals(currentSupplierPhone)) {
            values.put(COLUMN_SUPPLIER_PHONE, supplierPhoneString);
        }
        // If nothing to update, do nothing and return
        if (values.size() == 0) {
            Log.d(LOG_TAG, "TEST: Number of values to update is zero");
            return true;
        }

        // Update database for the product URI
        int rowsUpdated = getActivity().getContentResolver().update(currentProductUri, values, null, null);
        Log.d(LOG_TAG, "TEST: The number of updated rows is " + rowsUpdated);
        switch (rowsUpdated) {
            // Show a toast message depending on whether or not the updating was successful
            case 1:
                // Success
                Toast.makeText(getActivity(), R.string.toast_editor_update_success, Toast.LENGTH_LONG).show();
                return true;
            case 0:
                // Fail
                Toast.makeText(getActivity(), R.string.toast_editor_update_failed, Toast.LENGTH_LONG).show();
                return false;
            default:
                // Unexpected behavior
                // return value shouldn't be more than 1 because we update only one product at a time
                Toast.makeText(getActivity(), R.string.toast_editor_error_updating, Toast.LENGTH_LONG).show();
                return false;
        }
    }

    /**
     * Show a confirmation dialog to the user with options to proceed.
     *
     * @param discardButtonClickListener callback that performs discard actions.
     */
    private void showUnsavedChangesDialog(DialogInterface.OnClickListener discardButtonClickListener) {
        // Create an AlertDialog.Builder and set the message, and click listeners
        // for the positive and negative buttons on the dialog
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setMessage(R.string.unsaved_changes_dialog_msg);
        // If discard, send callback to the discardButtonClickListener
        builder.setPositiveButton(R.string.discard, discardButtonClickListener);
        builder.setNegativeButton(R.string.keep_editing, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked the "Keep editing" button, so dismiss the dialog
                // and continue editing the product
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
     * This method can be called from the activity's onBackPress() method.
     *
     * @return true if onBackPress was handled by the fragment.
     */
    @Override
    public boolean onBackPressed() {
        // If there are unsaved changes, setup a dialog to warn the user
        if (productHasChanged) {
            // Create a click listener to handle the user confirmation that
            // changes should be discarded
            DialogInterface.OnClickListener discardButtonClickListener =
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            // User clicked "Discard" button, finish this fragment
                            getFragmentManager().popBackStack();
                        }
                    };
            // Show a dialog that notifies the user they have unsaved changes
            showUnsavedChangesDialog(discardButtonClickListener);
            return true;
        }
        return false;
    }

    /**
     * Set {@link TextWatcher} that listens for any text changes on EditText views.
     * The listener sets up Floating Error Label if text input doesn't meet defined conditions,
     * and it changes productHasChanged boolean to true.
     *
     * @param layoutList The List of TextInputLayouts that contain EditText views.
     */
    public void setupOnEditTextChangedListener (final List<TextInputLayout> layoutList) {
        // Iterate through each layout in the list
        for (final TextInputLayout textInputLayout : layoutList) {
            // Set a TextWatcher on the EditText view to listen for changes in the input text
            textInputLayout.getEditText().addTextChangedListener(new TextWatcher() {
                // We don't need these two methods yet, so we leave them as is
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {}

                // Check the text after the user has changed it
                @Override
                public void afterTextChanged(Editable inputText) {
                    // Change productHasChanged boolean to true
                    productHasChanged = true;
                    // setupFloatingLabelErrorOnEditText
                    if (TextUtils.isEmpty(inputText)) {
                        textInputLayout.setError(getString(R.string.edittext_floating_error_label));
                        textInputLayout.setErrorEnabled(true);
                    } else {
                        textInputLayout.setErrorEnabled(false);
                    }
                }
            });
        }
    }
}
