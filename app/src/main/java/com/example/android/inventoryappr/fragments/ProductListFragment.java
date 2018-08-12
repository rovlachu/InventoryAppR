package com.example.android.inventoryappr.fragments;

import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
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
import android.widget.AdapterView;
import android.widget.CursorAdapter;
import android.widget.ListView;
import android.widget.Toast;

import com.example.android.inventoryappr.ProductCursorAdapter;
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
import static com.example.android.inventoryappr.data.ProductContract.ProductEntry.CONTENT_URI;

/**
 * The {@link ProductListFragment} displays the list of all products in the inventory.
 */
public class ProductListFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {

    /* Tag for the log messages */
    private static final String LOG_TAG = ProductListFragment.class.getSimpleName();
    /* CursorLoader ID */
    private static final int LOADER_ID = 1;

    @BindView(R.id.list_view_products)
    ListView productsListView;
    @BindView(R.id.empty_view)
    View emptyView;
    @BindView(R.id.add_product_fab)
    FloatingActionButton addProductFab;
    private Unbinder unbinder;

    /**
     * Reference to the {@link ProductCursorAdapter}
     */
    private CursorAdapter productCursorAdapter;

    /* Reference to the Listener implemented in the holding activity */
    private OnProductListListener onProductListListener;

    /* Empty constructor for the fragment */
    public ProductListFragment() {
    }

    @Nullable
    @Override
    /* Put initial UI state of the fragment here (i.e. inflate layout views). */
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_product_list, container, false);
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

        // Set the app bar title for this fragment
        getActivity().setTitle(getString(R.string.app_bar_title_product_list));

        // Setup an Adapter to create a list item view for each row of the product data
        // Adapter is empty for now because we use CursorLoader
        productCursorAdapter = new ProductCursorAdapter(getActivity(), null);
        productsListView.setAdapter(productCursorAdapter);

        // Set empty view to display to the user when there is no data
        productsListView.setEmptyView(emptyView);
        productsListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                // For a CursorAdapter the "long id" parameter returns the row ID in the table, that is _id
                Uri currentProductUri = ContentUris.withAppendedId(CONTENT_URI, id);
                // Send the callback and the product URI to the holding activity using the reference
                onProductListListener.onProductSelected(currentProductUri);
            }
        });
        // Start loading data
        getLoaderManager().initLoader(LOADER_ID, null, this);
    }

    @NonNull
    @Override
    public Loader<Cursor> onCreateLoader(int id, @Nullable Bundle args) {
        // Columns to extract from the "products" table. "_id" column is required
        String[] projection = {
                COLUMN_PROD_ID,
                COLUMN_PROD_NAME,
                COLUMN_PROD_PRICE,
                COLUMN_PROD_QUANTITY,
                COLUMN_SUPPLIER_NAME,
                COLUMN_SUPPLIER_PHONE
        };
        // Return a CursorLoader that loads all product rows from the database
        return new CursorLoader(getActivity(),
                CONTENT_URI,    // products table URI
                projection,
                null,   // WHERE clause (null means everything)
                null,
                null);  // Default sorting order
    }

    @Override
    public void onLoadFinished(@NonNull Loader<Cursor> loader, Cursor data) {
        // Change cursor in the adapter to the one with new loaded data
        productCursorAdapter.swapCursor(data);
    }

    @Override
    public void onLoaderReset(@NonNull Loader<Cursor> loader) {
        // Clean up. Pass null, to have the cursor closed before the loader is destroyed
        productCursorAdapter.swapCursor(null);
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
        // Clear the previous state of the menu before inflating the new one
        menu.clear();
        // Inflate the menu options from the res/menu/product_list_menu.xml file
        // This adds menu items to the app bar
        inflater.inflate(R.menu.product_list_menu, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // User clicked on a menu option in the app bar overflow menu
        switch (item.getItemId()) {
            // Respond to a click on the "Delete all products" menu option
            case R.id.action_dummy_product:
                insertDummyProduct();
                return true;
            // Respond to a click on the "Delete all products" menu option
            case R.id.action_delete_all:
                showDeleteConfirmationDialog();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        // This makes sure that the holding activity has implemented
        // the callback interface. If not, it throws an exception
        try {
            // Get the reference to the activity's listener when the activity and this fragment are connected
            onProductListListener = (OnProductListListener) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString() + " must implement OnProductListListener");
        }
    }

    /**
     * Listener interface that should be implemented by holding activity (InvetoryActivity in this case)
     * in order to get callbacks for the specified here actions.
     */
    public interface OnProductListListener {

        /**
         * Callback. When a product from the list is selected.
         *
         * @param productUri URI of the selected product.
         */
        public void onProductSelected(Uri productUri);

        /**
         * Callback. When the user clicked on AddProduct element.
         */
        public void onAddProductClicked();
    }

    /**
     * Show a confirmation dialog to the user with options to proceed.
     */
    private void showDeleteConfirmationDialog() {
        // Create an AlertDialog.Builder and set the message, and click listeners
        // for the positive and negative buttons on the dialog.
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setMessage(R.string.delete_all_dialog_msg);
        builder.setPositiveButton(R.string.delete, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked the "Delete" button, so delete the product
                deleteAllProducts();
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
     * Delete all products from the database.
     */
    private void deleteAllProducts() {
        int rowsDeleted = getActivity().getContentResolver().delete(CONTENT_URI, null, null);
        Log.d(LOG_TAG, "TEST: deleteAllProducts(). The number of rows deleted is " + rowsDeleted);
        switch (rowsDeleted){
            // Show a toast message depending on whether or not product deletion was successful
            case 0:
                // Fail
                Toast.makeText(getActivity(), "Deletion failed", Toast.LENGTH_LONG).show();
                break;
            default:
                // Success
                Toast.makeText(getActivity(), "All products deleted", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Helper method for debugging. Insert a dummy product into the database.
     */
    private void insertDummyProduct() {
        Uri newProdUri = null;
        for (int i = 1; i < 100000000; i = i * 9) {
            // Use a ContentValues object as a vessel for the data to be inserted
            ContentValues values = new ContentValues();
            values.put(COLUMN_PROD_NAME, "Long Dummy Product Name Placeholder v.1.36");
            values.put(COLUMN_PROD_PRICE, 199);
            values.put(COLUMN_PROD_QUANTITY, i);
            values.put(COLUMN_SUPPLIER_NAME, "Dummy Supplier Name");
            values.put(COLUMN_SUPPLIER_PHONE, "+0314159265359");

            // Insert a new product into the provider, returning the content URI for the new product
            newProdUri = getActivity().getContentResolver().insert(CONTENT_URI, values);
        }
        Log.d(LOG_TAG, "TEST: The new dummy product's Uri is " + newProdUri);

        // Show a toast message depending on whether or not the insertion was successful
        if (newProdUri == null) {
            // If the new content URI is null, then there was an error with insertion
            Toast.makeText(getActivity(), getString(R.string.toast_add_product_failed), Toast.LENGTH_LONG).show();
        } else {
            // Otherwise, the insertion was successful and we can display a toast
            Toast.makeText(getActivity(), getString(R.string.toast_add_product_success), Toast.LENGTH_LONG).show();
        }
    }

    /**
     * Respond to a click on the FAB.
     */
    @OnClick(R.id.add_product_fab)
    public void onAddProductClicked() {
        // Send the callback to the holding activity using the reference to its listener implementation
        onProductListListener.onAddProductClicked();
    }
}
