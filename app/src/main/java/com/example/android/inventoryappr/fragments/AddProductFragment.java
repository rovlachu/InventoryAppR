package com.example.android.inventoryappr.fragments;

import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.TextInputLayout;
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

import static com.example.android.inventoryappr.data.ProductContract.ProductEntry.COLUMN_PROD_NAME;
import static com.example.android.inventoryappr.data.ProductContract.ProductEntry.COLUMN_PROD_PRICE;
import static com.example.android.inventoryappr.data.ProductContract.ProductEntry.COLUMN_PROD_QUANTITY;
import static com.example.android.inventoryappr.data.ProductContract.ProductEntry.COLUMN_SUPPLIER_NAME;
import static com.example.android.inventoryappr.data.ProductContract.ProductEntry.COLUMN_SUPPLIER_PHONE;
import static com.example.android.inventoryappr.data.ProductContract.ProductEntry.CONTENT_URI;

/**
 * The {@link ProductListFragment} displays fields to input detailed information of a single product
 * (e.g. Name, Price, Quantity, Supplier Name, etc.).
 * Provides the way to add a new product to the database.
 */
public class AddProductFragment extends CustomFragment {

    /* Tag for the log messages */
    private static final String LOG_TAG = AddProductFragment.class.getSimpleName();

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

    /* Indicator of changes in the product info */
    private boolean productHasChanged = false;

    /**
     * Empty constructor for the fragment.
     */
    public AddProductFragment() {
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Declare that this fragment needs to edit OptionsMenu, thus its onCreateOptionsMenu will be called
        setHasOptionsMenu(true);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        menu.clear();
        // Inflate the menu options from the res/menu/add_product_menu.xml file
        // This adds menu items to the app bar
        inflater.inflate(R.menu.add_product_menu, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            // Respond to a click on the "Save" menu option
            case R.id.action_save:
                // Hide the keyboard
                InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(getView().getWindowToken(), 0);
                // Save a new product
                if (insertProduct()) {
                    // Finish this fragment when insertion is done
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
        // Set up productHasChanged listeners
        setupOnEditTextChangedListener(textInputLayoutList);
        return rootView;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        // Set the app bar title for this fragment
        getActivity().setTitle(getString(R.string.app_bar_title_add_product));
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        // Clean up. Unbind views from this fragment before the fragment is destroyed
        unbinder.unbind();
    }

    /**
     * Insert a new product into the database.
     *
     * @return true when product insertion was successful, otherwise false.
     */
    private boolean insertProduct() {
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
        values.put(COLUMN_PROD_NAME, productNameString);
        values.put(COLUMN_PROD_PRICE, priceInt);
        values.put(COLUMN_PROD_QUANTITY, quantityInt);
        values.put(COLUMN_SUPPLIER_NAME, supplierNameString);
        values.put(COLUMN_SUPPLIER_PHONE, supplierPhoneString);

        // Insert a new product into the provider, returning the content URI for the new product
        Uri newProdUri = getActivity().getContentResolver().insert(CONTENT_URI, values);
        Log.d(LOG_TAG, "TEST: The new product's Uri is " + newProdUri);

        // Show a toast message depending on whether or not the insertion was successful
        if (newProdUri == null) {
            // If the new content URI is null, then there was an error with insertion
            Toast.makeText(getActivity(), getString(R.string.toast_add_product_failed), Toast.LENGTH_LONG).show();
            return false;
        } else {
            // Otherwise, the insertion was successful and we can display a toast
            Toast.makeText(getActivity(), getString(R.string.toast_add_product_success), Toast.LENGTH_LONG).show();
            return true;
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

                /**
                 * Check the text if the user has typed sth in.
                 * Live example: http://qaru.site/img/4be4fa196a5db22822a1de2dc90dcf07.gif
                 *
                 * @param s A text, but it can't be changed within this method.
                 * @param start Start index of the char in the current word.
                 * @param before Number of chars before now.
                 * @param count Number of chars now.
                 */
                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    // Setup Floating Label Error on EditText
                    if (start == 0 && before == 1) {
                        // When the user typed sth in and then deleted it
                        textInputLayout.setError(getString(R.string.edittext_floating_error_label));
                        textInputLayout.setErrorEnabled(true);
                    } else {
                        textInputLayout.setErrorEnabled(false);
                        // Change productHasChanged boolean to true
                        productHasChanged = true;
                    }
                }
                // We don't need these two methods yet, so we leave them as is
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
                @Override
                public void afterTextChanged(Editable inputText) {}
            });
        }
    }
}
