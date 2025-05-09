package com.example.shop;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.MenuItemCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.SharedPreferences;
import android.content.res.TypedArray;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.SearchView;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.ArrayList;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;

import android.content.pm.ArchivedActivityInfo;


import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;

import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class ShopListActivity extends AppCompatActivity {
    private static final String LOG_TAG = ShopListActivity.class.getName();
    private FirebaseUser user;
    private RecyclerView mRecycleView;
    private ArrayList<ShoppingItem> mItemList;
    private ShoppingItemAdapter mAdapter;

    public FrameLayout redCircle;
    private TextView contentTextView;
    private int gridNumber = 1;
    private int cartIems = 0;
    private boolean viewRow = true;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_shop_list);

        EdgeToEdge.enable(this);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null){
            Log.d(LOG_TAG, "Hitelesített felhasználó!");
        } else {
            Log.d(LOG_TAG, "Nem hitelesített felhasnáló!");
            finish();
        }

        mRecycleView = findViewById(R.id.recyclerView);
        mRecycleView.setLayoutManager(new GridLayoutManager(this, gridNumber));
        mItemList = new ArrayList<>();
        mAdapter = new ShoppingItemAdapter(this, mItemList);
        mRecycleView.setAdapter(mAdapter);
        initializeData();
    }

    private void initializeData(){
        String[] itemsList = getResources().getStringArray(R.array.shopping_item_names);
        String[] itemsInfo = getResources().getStringArray(R.array.shopping_item_desc);
        String[] itemsPrice = getResources().getStringArray(R.array.shopping_item_price);
        TypedArray itemsImageResource = getResources().obtainTypedArray(R.array.shopping_item_images);
        TypedArray itemsRate = getResources().obtainTypedArray(R.array.shopping_item_rates);
        mItemList.clear();
        for (int i = 0; i < itemsList.length; i++) {
            mItemList.add(new ShoppingItem(itemsImageResource.getResourceId(i, 0), itemsInfo[i], itemsList[i], itemsPrice[i], itemsRate.getFloat(i, 0)));
        }

        itemsImageResource.recycle();
        mAdapter.notifyDataSetChanged();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.shop_list_menu, menu);
        MenuItem menuItem = menu.findItem(R.id.search_bar);
        SearchView searchView = (SearchView) MenuItemCompat.getActionView(menuItem);
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) { return false; }
            @Override
            public boolean onQueryTextChange(String newText) {
                Log.d(LOG_TAG, newText);
                mAdapter.getFilter().filter(newText);
                return false;
            }
        });
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.logout_button) {
            Log.d(LOG_TAG, "Kijelentkezés gomb megnyomva!");
            FirebaseAuth.getInstance().signOut();
            finish();
            return true;

        } else if (id == R.id.settings_button) {
            Log.d(LOG_TAG, "Beállítások gomb megnyomva!");
            return true;

        } else if (id == R.id.cart_button) {
            Log.d(LOG_TAG, "Kosár gomb megnyomva!");
            return true;

        } else if (id == R.id.view_button) {
            Log.d(LOG_TAG, "Nézet gomb megnyomva!");
            if (viewRow) {
                changeSpanCount(item, R.drawable.view_grid, 1);
            } else {
                changeSpanCount(item, R.drawable.view_row, 2);
            }
            return true;

        } else {
            return super.onOptionsItemSelected(item);
        }
    }


    private void changeSpanCount(MenuItem item, int drawableId, int spanCount){
        viewRow = !viewRow;
        item.setIcon(drawableId);
        GridLayoutManager layoutManager = (GridLayoutManager) mRecycleView.getLayoutManager();
        assert layoutManager != null;
        layoutManager.setSpanCount(spanCount);
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu){
        final MenuItem alertMenuItem = menu.findItem(R.id.cart_button);
        FrameLayout rootView = (FrameLayout) alertMenuItem.getActionView();
        redCircle = (FrameLayout) rootView.findViewById(R.id.view_alert_red_circle);
        contentTextView = (TextView) rootView.findViewById(R.id.view_alert_count_textview);
        rootView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onOptionsItemSelected(alertMenuItem);
            }
        });

        return super.onPrepareOptionsMenu(menu);
    }

    public void updateAlertIcon(){
        cartIems = (cartIems + 1);
        if (0 < cartIems){
            contentTextView.setText(String.valueOf(cartIems));
        } else {
            contentTextView.setText("");
        }
    }
}