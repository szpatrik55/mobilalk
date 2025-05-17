package com.example.shop;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.MenuItemCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.job.JobInfo;
import android.app.job.JobScheduler;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.content.res.TypedArray;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.SearchView;
import android.widget.TextView;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.Firebase;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.WriteBatch;

import java.util.ArrayList;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;

import android.content.pm.ArchivedActivityInfo;
import android.widget.Toast;

import androidx.appcompat.widget.Toolbar;


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
    private int cartItems = 0;
    private int itemLimit = 15;
    private boolean viewRow = true;
    private FirebaseFirestore mFirestore;
    private CollectionReference mItems;
    private SharedPreferences preferences;
    private NotificationHandler mNotificationHandler;
    private AlarmManager mAlarmManager;
    private JobScheduler mJobScheduler;


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
            Log.d(LOG_TAG, "Nem hitelesített felhasználó!");
            finish();
        }

        int orientation = getResources().getConfiguration().orientation;

        if (orientation == Configuration.ORIENTATION_LANDSCAPE) {
            gridNumber = 2;  // Tájkép módban grid nézetben legyen
        } else {
            gridNumber = 1;  // Portré módban lista nézetben
        }

        mRecycleView = findViewById(R.id.recyclerView);
        mRecycleView.setLayoutManager(new GridLayoutManager(this, gridNumber));
        mItemList = new ArrayList<>();
        mAdapter = new ShoppingItemAdapter(this, mItemList);
        mRecycleView.setAdapter(mAdapter);

        mFirestore = FirebaseFirestore.getInstance();
        mItems = mFirestore.collection("Items");

        queryData();

        IntentFilter filter = new IntentFilter();
        filter.addAction(Intent.ACTION_POWER_CONNECTED);
        filter.addAction(Intent.ACTION_POWER_DISCONNECTED);
        this.registerReceiver(powerReceiver, filter);

        mNotificationHandler = new NotificationHandler(this);
        mAlarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
        mJobScheduler = (JobScheduler) getSystemService(JOB_SCHEDULER_SERVICE);

        setAlarmManager();
        setJobScheduler();
    }

    BroadcastReceiver powerReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();

            if (action == null){
                return;
            }

            switch (action){
                case Intent.ACTION_POWER_CONNECTED:
                    itemLimit = 20;
                    break;
                case Intent.ACTION_POWER_DISCONNECTED:
                    itemLimit = 15;
                    break;
            }
            queryData();
        }
    };

    private void initializeData() {
        String[] itemsList = getResources().getStringArray(R.array.shopping_item_names);
        String[] itemsInfo = getResources().getStringArray(R.array.shopping_item_desc);
        String[] itemsPrice = getResources().getStringArray(R.array.shopping_item_price);
        TypedArray itemsImageResources = getResources().obtainTypedArray(R.array.shopping_item_images);
        TypedArray itemRate = getResources().obtainTypedArray(R.array.shopping_item_rates);

        for (int i = 0; i < itemsList.length; i++) {
            mItems.add(new ShoppingItem(
                    itemsList[i],
                    itemsInfo[i],
                    itemsPrice[i],
                    itemRate.getFloat(i, 0),
                    itemsImageResources.getResourceId(i, 0),
                    0));
        }

        itemsImageResources.recycle();
    }
    private void queryData() {
        mItemList.clear();
        mItems.orderBy("name", Query.Direction.DESCENDING).limit(itemLimit).get().addOnSuccessListener(queryDocumentSnapshots -> {
            for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                ShoppingItem item = document.toObject(ShoppingItem.class);
                item.setId(document.getId());
                mItemList.add(item);
            }

            if (mItemList.isEmpty()) {
                initializeData();
                queryData();
            }

            mAdapter.notifyDataSetChanged();
        });
    }

    public void deleteItem(ShoppingItem item){
        DocumentReference ref = mItems.document(item._getId());
        ref.delete().addOnSuccessListener(succes -> {
            Log.d(LOG_TAG, "Termék törölve: " + item._getId());
        })
                .addOnFailureListener(failure ->{
                    Toast.makeText(this, "Termék " + item._getId() + " nem került törlésre.", Toast.LENGTH_LONG).show();
                });

        mNotificationHandler.cancel();
        queryData();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);

        if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            gridNumber = 2;
        } else {
            gridNumber = 1;
        }

        mRecycleView.setLayoutManager(new GridLayoutManager(this, gridNumber));
        invalidateOptionsMenu();
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.shop_list_menu, menu);
        MenuItem menuItem = menu.findItem(R.id.search_bar);
        SearchView searchView = (SearchView) MenuItemCompat.getActionView(menuItem);
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String s) {
                return false;
            }
            @Override
            public boolean onQueryTextChange(String s) {
                Log.d(LOG_TAG, s);
                mAdapter.getFilter().filter(s);
                return false;
            }
        });
        return true;
    }

    @Override
    public boolean onOptionsItemSelected( MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.log_out_button) {
            Log.d(LOG_TAG, "Kijelentkezés gomb megnyomva!");
            FirebaseAuth.getInstance().signOut();
            finish();
            return true;

        } else if (id == R.id.setting_button) {
            Log.d(LOG_TAG, "Beállítások gomb megnyomva!");
            return true;

        } else if (id == R.id.cart) {
            Log.d(LOG_TAG, "Kosár gomb megnyomva!");
            return true;

        } else if (id == R.id.view_selector) {
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

    private void changeSpanCount(MenuItem item, int drawableId, int spanCount) {
        viewRow = !viewRow;
        item.setIcon(drawableId);
        GridLayoutManager layoutManager = (GridLayoutManager) mRecycleView.getLayoutManager();
        layoutManager.setSpanCount(spanCount);
    }


    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        final MenuItem alertMenuItem = menu.findItem(R.id.cart);
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

    public void updateAlertIcon(ShoppingItem item){
        cartItems = (cartItems+1);
        if (0 < cartItems){
            contentTextView.setText(String.valueOf(cartItems));
        } else {
            contentTextView.setText("");
        }

        redCircle.setVisibility((cartItems > 0) ? VISIBLE : GONE);

        mItems.document(item._getId()).update("cartedCount", item.getCartedCount()+1)
                .addOnFailureListener(failure ->{
                    Toast.makeText(this, "Termék " + item._getId() + " nem került változtatásra.", Toast.LENGTH_LONG).show();
                });

        mNotificationHandler.send(item.getName());
        queryData();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(powerReceiver);
    }

    private void setAlarmManager(){
        long reapeatInterval = AlarmManager.INTERVAL_FIFTEEN_MINUTES * 96; //24 óránként
        long triggerTime = SystemClock.elapsedRealtime() + reapeatInterval;

        Intent intent = new Intent(this, AlarmReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(this, 1, intent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
        mAlarmManager.setInexactRepeating(
                AlarmManager.ELAPSED_REALTIME_WAKEUP,
                triggerTime,
                reapeatInterval,
                pendingIntent
        );

    }

    public void setJobScheduler(){
        if (mJobScheduler == null) {
            Log.e(LOG_TAG, "JobScheduler is null!");
            return;
        }

        int networkType = JobInfo.NETWORK_TYPE_UNMETERED;
        int hardDeadline = 80000;

        ComponentName name = new ComponentName(getPackageName(), NotificationJobService.class.getName());
        JobInfo.Builder builder = new JobInfo.Builder(0, name)
                .setRequiredNetworkType(networkType)
                .setRequiresCharging(true)
                .setOverrideDeadline(hardDeadline);

        mJobScheduler.schedule(builder.build());
    }

}