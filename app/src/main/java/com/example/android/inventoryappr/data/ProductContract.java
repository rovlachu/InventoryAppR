package com.example.android.inventoryappr.data;

import android.content.ContentResolver;
import android.net.Uri;
import android.provider.BaseColumns;

/**
 * A container for constants that define names for URIs, tables, and columns.
 */
public final class ProductContract {
    // To prevent someone from accidentally instantiating the contract class,
    // make the constructor private.
    private ProductContract () {}

    /**
     * The "Content authority" is a name for the entire content provider, similar to the
     * relationship between a domain name and its website.  A convenient string to use for the
     * content authority is the package name for the app, which is guaranteed to be unique on the
     * device.
     */
    public static final String CONTENT_AUTHORITY = "com.example.android.inventoryappr.productprovider";

    /**
     * Use CONTENT_AUTHORITY to create the base of all URIs which apps will use to contact
     * the content provider.
     */
    public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY);

    /**
     * Possible path (appended to base content URI for possible URI's)
     * For instance, content://com.example.android.inventoryappr.ProductProvider/products/ is a valid path for
     * looking at product data. content://com.example.android.inventoryappr.ProductProvider/stuff/ will fail,
     * as the ContentProvider hasn't been given any information on what to do with "staff".
     */
    public static final String PATH_PRODUCTS = ProductEntry.TABLE_NAME; // "products"

    /**
     * Inner class that defines the table contents.
     */
    public static abstract class ProductEntry implements BaseColumns {

        /** The content URI to access the product data in the provider */
        public static final Uri CONTENT_URI = Uri.withAppendedPath(BASE_CONTENT_URI, PATH_PRODUCTS);

        /**
         * The MIME type of the {@link #CONTENT_URI} for a list of products.
         */
        public static final String CONTENT_LIST_TYPE =
                ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_PRODUCTS;

        /**
         * The MIME type of the {@link #CONTENT_URI} for a single product.
         */
        public static final String CONTENT_ITEM_TYPE =
                ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_PRODUCTS;

        // Constants for table name and headings (column names)
        /* Name of database table for products */
        public static final String TABLE_NAME = "products";

        /**
         * The product ID. <P>Type: INTEGER</P>
         * The Cursor must include a column named "_id" or the CursorAdapter will not work.
         */
        public static final String COLUMN_PROD_ID = "_id";

        /* The name of the product. <P>Type: TEXT</P> */
        public static final String COLUMN_PROD_NAME = "prod_name";

        /* The price of the product in the smallest currency unit. <P>Type: INTEGER</P> */
        public static final String COLUMN_PROD_PRICE = "prod_price";

        /* The quantity of the product. <P>Type: INTEGER</P> */
        public static final String COLUMN_PROD_QUANTITY = "prod_quantity";

        /* The name of the supplier. <P>Type: TEXT</P> */
        public static final String COLUMN_SUPPLIER_NAME = "supplier_name";

        /* The phone number of the supplier. <P>Type: TEXT</P> */
        public static final String COLUMN_SUPPLIER_PHONE = "supplier_phone";
    }
}
