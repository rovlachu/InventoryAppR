package com.example.android.inventoryappr.data;

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

import static com.example.android.inventoryappr.data.ProductContract.ProductEntry.COLUMN_PROD_ID;
import static com.example.android.inventoryappr.data.ProductContract.ProductEntry.COLUMN_PROD_NAME;
import static com.example.android.inventoryappr.data.ProductContract.ProductEntry.COLUMN_PROD_PRICE;
import static com.example.android.inventoryappr.data.ProductContract.ProductEntry.COLUMN_PROD_QUANTITY;
import static com.example.android.inventoryappr.data.ProductContract.ProductEntry.COLUMN_SUPPLIER_NAME;
import static com.example.android.inventoryappr.data.ProductContract.ProductEntry.COLUMN_SUPPLIER_PHONE;
import static com.example.android.inventoryappr.data.ProductContract.ProductEntry.CONTENT_ITEM_TYPE;
import static com.example.android.inventoryappr.data.ProductContract.ProductEntry.CONTENT_LIST_TYPE;
import static com.example.android.inventoryappr.data.ProductContract.ProductEntry.TABLE_NAME;

public class ProductProvider extends ContentProvider {
    /** Tag for the log messages */
    private static final String LOG_TAG = ProductProvider.class.getSimpleName();

    /** Database helper object */
    private ProductDbHelper productDbHelper;

    /** URI matcher code for the content URI for the products table */
    private static final int PRODUCTS = 100;

    /** URI matcher code for the content URI for a single product in the products table */
    private static final int PRODUCT_ID = 101;

    /**
     * UriMatcher object to match a content URI to a corresponding code.
     * The input passed into the constructor represents the code to return for the root URI.
     * It's common to use NO_MATCH as the input for this case.
     */
    private static final UriMatcher uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
    // Static initializer. This is run the first time anything is called from this class.
    static {
        // The calls to addURI() go here, for all of the content URI patterns that the provider
        // should recognize. All paths added to the UriMatcher have a corresponding code to return
        // when a match is found.
        uriMatcher.addURI(ProductContract.CONTENT_AUTHORITY, ProductContract.PATH_PRODUCTS, PRODUCTS);
        uriMatcher.addURI(ProductContract.CONTENT_AUTHORITY, ProductContract.PATH_PRODUCTS + "/#", PRODUCT_ID);
    }

    /**
     * Initialize the provider and the database helper object.
     */
    @Override
    public boolean onCreate() {
        // Create and initialize a ProductDbHelper object to gain access to the inventory database.
        productDbHelper = new ProductDbHelper(getContext());
        return true;
    }

    /**
     * Perform the query for the given URI. Use the given projection, selection, selection arguments, and sort order.
     */
    @Nullable
    @Override
    public Cursor query(@NonNull Uri uri, @Nullable String[] projection, @Nullable String selection, @Nullable String[] selectionArgs, @Nullable String sortOrder) {
        // Get readable database
        SQLiteDatabase database = productDbHelper.getReadableDatabase();

        // This cursor will hold the result of the query
        Cursor cursor;

        // Figure out if the URI matcher can match the URI to a specific code
        final int match = uriMatcher.match(uri);
        switch (match) {
            case PRODUCTS:
                // Query to the whole products table
                cursor = database.query(TABLE_NAME, projection,
                        selection, selectionArgs,
                        null, null, sortOrder);
                break;
            case PRODUCT_ID:
                // Query for a single product
                selection = COLUMN_PROD_ID + "=?";
                selectionArgs = new String[] {String.valueOf(ContentUris.parseId(uri))};
                cursor = database.query(TABLE_NAME ,projection,
                        selection, selectionArgs,
                        null, null, sortOrder);
                break;
            default:
                throw new IllegalArgumentException("Cannot query unknown URI " + uri);
        }
        // Set notification URI on the Cursor,
        // so we know what content URI the Cursor was created for.
        // If the data at this URI changes, then we know we need to update the Cursor.
        cursor.setNotificationUri(getContext().getContentResolver(), uri);
        return cursor;
    }

    /**
     * Returns the MIME type of data for the content URI.
     */
    @Nullable
    @Override
    public String getType(@NonNull Uri uri) {
        final int match = uriMatcher.match(uri);
        switch (match){
            case PRODUCTS:
                return CONTENT_LIST_TYPE;
            case PRODUCT_ID:
                return CONTENT_ITEM_TYPE;
            default:
                throw new IllegalStateException("Unknown URI " + uri + " with match " + match);
        }
    }

    /**
     * Insert new data into the provider with the given ContentValues.
     */
    @Nullable
    @Override
    public Uri insert(@NonNull Uri uri, @Nullable ContentValues values) {
        final int match = uriMatcher.match(uri);
        switch (match) {
            case PRODUCTS:
                return insertProduct(uri, values);
            default:
                throw new IllegalArgumentException("Insertion is not supported for " + uri);
        }
    }

    /**
     * Insert a product into the database with the given content values. Return the new content URI
     * for that specific row in the database.
     */
    private Uri insertProduct(Uri uri, ContentValues values) {
        // Sanity checking the attributes in ContentValues
        // Check that the product name is not null
        String productName = values.getAsString(COLUMN_PROD_NAME);
        if (productName == null) {
            throw new IllegalArgumentException("Product requires a name");
        }
        // Check that the price is not null and valid
        Integer productPrice = values.getAsInteger(COLUMN_PROD_PRICE);
        if (productPrice == null || productPrice < 0) {
            throw new IllegalArgumentException("Product requires a valid price");
        }
        // Check that the quantity is not null and valid
        Integer productQuantity = values.getAsInteger(COLUMN_PROD_QUANTITY);
        if (productQuantity == null || productQuantity < 0) {
            throw new IllegalArgumentException("Product requires a valid quantity");
        }
        // Check that the supplier name is not null
        String supplierName = values.getAsString(COLUMN_SUPPLIER_NAME);
        if (supplierName == null) {
            throw new IllegalArgumentException("Supplier requires a name");
        }
        // Check that the supplier phone number is not null
        String supplierPhone = values.getAsString(COLUMN_SUPPLIER_NAME);
        if (supplierPhone == null) {
            throw new IllegalArgumentException("Supplier requires a phone number");
        }

        // Get writable database
        SQLiteDatabase database = productDbHelper.getWritableDatabase();

        // Insert the new row, returning the primary key value (_id in this case) of the new row
        long newProductId = database.insert(TABLE_NAME, null, values);
        Log.d(LOG_TAG, "TEST: The new product's row ID is " + String.valueOf(newProductId));

        // If the ID is -1, then the insertion failed. Log an error and return null.
        if (newProductId == -1) {
            Log.e(LOG_TAG, "Failed to insert row for " + uri);
            return null;
        }
        // Notify all listeners that the data has changed for the product content URI
        getContext().getContentResolver().notifyChange(uri, null);
        // Return the new product Uri
        return ContentUris.withAppendedId(uri, newProductId);
    }

    /**
     * Delete the data at the given selection and selection arguments.
     */
    @Override
    public int delete(@NonNull Uri uri, @Nullable String selection, @Nullable String[] selectionArgs) {
        SQLiteDatabase database = productDbHelper.getWritableDatabase();
        // Track the number of rows that were deleted
        int rowsDeleted;

        final int match = uriMatcher.match(uri);
        switch (match){
            case PRODUCTS:
                rowsDeleted = database.delete(TABLE_NAME, selection, selectionArgs);
                break;
            case PRODUCT_ID:
                // Specify rows to delete and delete
                selection = COLUMN_PROD_ID + "=?";
                selectionArgs = new String[] {String.valueOf(ContentUris.parseId(uri))};
                rowsDeleted = database.delete(TABLE_NAME, selection, selectionArgs);
                break;
            default:
                throw new IllegalArgumentException("Deletion is not supported for " + uri);
        }
        Log.d(LOG_TAG, "TEST: The number of rows deleted is " + rowsDeleted);
        // If 1 or more rows were deleted, then notify all listeners that the data at the given URI has changed
        if (rowsDeleted > 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }
        // Return the number of deleted rows
        return rowsDeleted;
    }

    /**
     * Updates the data at the given selection and selection arguments, with the new ContentValues.
     */
    @Override
    public int update(@NonNull Uri uri, @Nullable ContentValues values, @Nullable String selection, @Nullable String[] selectionArgs) {
        final int match = uriMatcher.match(uri);
        switch (match){
            case PRODUCTS:
                return updateProduct(uri, values, selection, selectionArgs);
            case PRODUCT_ID:
                // Specify what product needs update
                selection = COLUMN_PROD_ID + "=?";
                selectionArgs = new String[] {String.valueOf(ContentUris.parseId(uri))};
                return updateProduct(uri, values, selection, selectionArgs);
            default:
                throw new IllegalArgumentException("Update is not supported for " + uri);
        }
    }
    /**
     * Helper method to update products in the database with the given content values. Apply the changes
     * to the rows specified in the selection and selection arguments (which could be 0 or 1 or more products).
     * Return the number of rows that were successfully updated.
     */
    private int updateProduct(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        // Sanity checking the attributes in ContentValues
        // Check that the product name is not null if it's going to be updated
        if (values.containsKey(COLUMN_PROD_NAME)){
            String productName = values.getAsString(COLUMN_PROD_NAME);
            if (productName == null) {
                throw new IllegalArgumentException("Product requires a name");
            }
        }
        // Check that the price is not null and valid if it's going to be updated
        if (values.containsKey(COLUMN_PROD_PRICE)) {
            Integer productPrice = values.getAsInteger(COLUMN_PROD_PRICE);
            if (productPrice == null || productPrice < 0) {
                throw new IllegalArgumentException("Product requires a valid price");
            }
        }
        // Check that the quantity is not null and valid if it's going to be updated
        if (values.containsKey(COLUMN_PROD_QUANTITY)){
            Integer productQuantity = values.getAsInteger(COLUMN_PROD_QUANTITY);
            if (productQuantity == null || productQuantity < 0) {
                throw new IllegalArgumentException("Product requires a valid quantity");
            }
        }
        // Check that the supplier name is not null if it's going to be updated
        if (values.containsKey(COLUMN_SUPPLIER_NAME)){
            String supplierName = values.getAsString(COLUMN_SUPPLIER_NAME);
            if (supplierName == null) {
                throw new IllegalArgumentException("Supplier requires a name");
            }
        }
        // Check that the supplier phone number is not null if it's going to be updated
        if (values.containsKey(COLUMN_SUPPLIER_PHONE)){
            String supplierPhone = values.getAsString(COLUMN_SUPPLIER_PHONE);
            if (supplierPhone == null) {
                throw new IllegalArgumentException("Supplier requires a phone number");
            }
        }

        // Get writable database
        SQLiteDatabase database = productDbHelper.getWritableDatabase();

        // Perform the update on the database and get the number of rows updated
        int rowsUpdated = database.update(TABLE_NAME, values, selection, selectionArgs);
        Log.d(LOG_TAG, "TEST: The number of updated rows is " + rowsUpdated);

        // If 1 or more rows were updated, then notify all listeners that the data at the
        // given URI has changed
        if (rowsUpdated != 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }
        // Return the number of rows updated
        return rowsUpdated;
    }
}
