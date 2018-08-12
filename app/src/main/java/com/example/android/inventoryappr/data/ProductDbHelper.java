package com.example.android.inventoryappr.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import static com.example.android.inventoryappr.data.ProductContract.ProductEntry.*;

public class ProductDbHelper extends SQLiteOpenHelper {

    // If you change the database schema, you must increment the database version
    public static final int DATABASE_VERSION = 1;
    public static final String DATABASE_NAME = "inventory.db";

    /**
     * Constructs a new instance of {@link ProductDbHelper}.
     *
     * @param context of the app
     */
    public ProductDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    /**
     * This method is called when the database is created for the first time.
     */
    @Override
    public void onCreate(SQLiteDatabase db) {
        // Create a String that contains the SQL statement to create the products table
        String SQL_CREATE_PRODUCTS_TABLE = "CREATE TABLE " + TABLE_NAME + " ("
                + COLUMN_PROD_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + COLUMN_PROD_NAME + " TEXT NOT NULL, "
                + COLUMN_PROD_PRICE + " INTEGER NOT NULL, "
                + COLUMN_PROD_QUANTITY + " INTEGER NOT NULL, "
                + COLUMN_SUPPLIER_NAME + " TEXT NOT NULL, "
                + COLUMN_SUPPLIER_PHONE + " TEXT NOT NULL);";
        // Execute the SQL statement
        db.execSQL(SQL_CREATE_PRODUCTS_TABLE);
    }

    /**
     * This is called when the database needs to be upgraded.
     */
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // The database is still at version 1, so there's nothing to be done here.
    }
}
