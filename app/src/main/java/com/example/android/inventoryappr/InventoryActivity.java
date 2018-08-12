package com.example.android.inventoryappr;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.inputmethod.InputMethodManager;

import com.example.android.inventoryappr.fragments.AddProductFragment;
import com.example.android.inventoryappr.fragments.CustomFragment;
import com.example.android.inventoryappr.fragments.EditProductFragment;
import com.example.android.inventoryappr.fragments.ProductDetailsFragment;
import com.example.android.inventoryappr.fragments.ProductListFragment;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class InventoryActivity extends AppCompatActivity
        implements
        ProductListFragment.OnProductListListener,
        ProductDetailsFragment.OnEditOptionSelectedListener,
        // For Up Navigation in fragments
        FragmentManager.OnBackStackChangedListener {

    // Need this view to handle hiding the keyboard when Up button is pressed,
    // so we can grab a window token from it.
    @BindView(R.id.activity_parent_view)
    View activityParentView;

    /* Tag for the log messages */
    private static final String LOG_TAG = InventoryActivity.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_inventory);
        ButterKnife.bind(this);

        /* Enable Up Navigation in fragments. https://stackoverflow.com/a/20314570 */
        // Listen for changes in the fragments back stack
        getSupportFragmentManager().addOnBackStackChangedListener(this);
        // Handle when activity is recreated like on orientation Change
        shouldDisplayHomeUp();

        // If the activity is restored from a previous state,
        // then return early or else we could end up with overlapping fragments
        if (savedInstanceState != null) {
            return;
        }

        // Get the reference to the FragmentManager and define FragmentTransaction
        // in order to manipulate fragments
        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        // Instantiate an instance of the ProductListFragment
        ProductListFragment productListFragment = new ProductListFragment();
        // fragment_container is essentially a placeholder for fragments that allows us to manage fragments on the go
        fragmentTransaction.add(R.id.fragment_container, productListFragment);
        // Insert default fragment(product_list) to the first screen
        fragmentTransaction.commit();
    }

    @Override
    public void onBackPressed() {
        // Get the list of all fragments in the fragment manager
        List<Fragment> fragmentList = getSupportFragmentManager().getFragments();

        boolean onBackPressHandled = false;
        for(Fragment fragment : fragmentList) {
            if(fragment instanceof CustomFragment) {
                // Call onBackPress() method from the fragment
                onBackPressHandled = ((CustomFragment)fragment).onBackPressed();
                if(onBackPressHandled) {
                    break;
                }
            }
        }
        // If none of the fragments handled this, proceed as usual
        if (!onBackPressHandled) {
            super.onBackPressed();
        }
    }

    @Override
    public void onBackStackChanged() {
        shouldDisplayHomeUp();
    }

    /**
     * This method is called when the Up button is pressed.
     */
    @Override
    public boolean onSupportNavigateUp() {
        // Hide the keyboard https://stackoverflow.com/a/17789187
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(activityParentView.getWindowToken(), 0);

        // We can just call onBackPressed() method because all the logic has been implemented
        // already there and we need the same behavior here for now
        onBackPressed();
        return true;
    }

    /**
     * Activate Up navigation button.
     */
    public void shouldDisplayHomeUp(){
        // Enable Up button only if there are fragmentTransition entries in the back stack
        boolean canback = getSupportFragmentManager().getBackStackEntryCount() > 0;
        getSupportActionBar().setDisplayHomeAsUpEnabled(canback);
    }

    /**
     * This method is called when the user selects the product from the list.
     *
     * @param productUri URI of the selected product in the database.
     */
    @Override
    public void onProductSelected(Uri productUri) {
        // Create the ProductDetailFragment and give it the URI of the selected product
        ProductDetailsFragment productDetailsFragment = new ProductDetailsFragment();
        Bundle args = new Bundle();
        args.putString("productUri", productUri.toString());
        productDetailsFragment.setArguments(args);
        // Show the ProductDetailFragment
        // Replace whatever is in the fragment_container view with this fragment,
        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        fragmentTransaction.replace(R.id.fragment_container, productDetailsFragment);
        // Add the transaction to the back stack so the user can navigate back
        fragmentTransaction.addToBackStack(null);
        fragmentTransaction.commit();
    }

    /**
     * This method is called when the user clicks on the AddProduct element (button or menu option).
     */
    @Override
    public void onAddProductClicked() {
        // Create the AddProductFragment
        AddProductFragment addProductFragment = new AddProductFragment();
        // Show the AddProductFragment
        // Replace whatever is in the fragment_container view with this fragment,
        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        fragmentTransaction.replace(R.id.fragment_container, addProductFragment);
        // Add the transaction to the back stack so the user can navigate back
        fragmentTransaction.addToBackStack(null);
        fragmentTransaction.commit();
    }

    /**
     * This method is called when the user clicks on the Edit option in the menu.
     *
     * @param productUri URI of the currently selected product.
     */
    @Override
    public void onEditOptionSelected(Uri productUri) {
        // Create the EditProductFragment and give it the URI of the current product
        EditProductFragment editProductFragment = new EditProductFragment();
        Bundle args = new Bundle();
        args.putString("productUri", productUri.toString());
        editProductFragment.setArguments(args);
        // Show the ProductDetailFragment
        // Replace whatever is in the fragment_container view with this fragment,
        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        fragmentTransaction.replace(R.id.fragment_container, editProductFragment);
        // Add the transaction to the back stack so the user can navigate back
        fragmentTransaction.addToBackStack(null);
        fragmentTransaction.commit();
    }
}

