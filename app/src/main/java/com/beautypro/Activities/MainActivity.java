package com.beautypro.Activities;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.beautypro.Dialogs.ProfileDialog;
import com.beautypro.Fragments.CartFragment;
import com.beautypro.Fragments.OrdersFragment;
import com.beautypro.Fragments.ShopFragment;
import com.beautypro.Fragments.ShopItemFragment;
import com.beautypro.R;
import com.beautypro.Utils.Utils;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.Objects;

public class MainActivity extends AppCompatActivity {

    FirebaseFirestore DB;
    FirebaseAuth AUTH;
    FirebaseUser USER;

    private void initializeFirebase() {
        DB = FirebaseFirestore.getInstance();
        AUTH = FirebaseAuth.getInstance();
        USER = AUTH.getCurrentUser();
    }

    BottomNavigationView bottom_navbar;
    MaterialButton btnProfile;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initializeFirebase();
        initializeViews();
        backstackListener();
        // countCartItems();

        if (USER != null) {
            listenForReadyForPickupOrders();
            listenForInTransitOrders();
            listenForDelinquency();

        }
        btnProfile.setOnClickListener(view -> {
            if (USER != null) {
                ProfileDialog profileDialog = new ProfileDialog();
                profileDialog.show(getSupportFragmentManager(), "PROFILE_DIALOG");
            }
            else {
                startActivity(new Intent(MainActivity.this, AuthenticationActivity.class));
            }
        });

        /*bottom_navbar.setOnItemSelectedListener(item -> {
            FragmentManager fragmentManager = getSupportFragmentManager();
            FragmentTransaction fragmentTransaction= fragmentManager.beginTransaction();
            switch (item.getItemId()) {
                case R.id.miShop:
                    Utils.hideKeyboard(this);
                    if (bottom_navbar.getSelectedItemId() != R.id.miShop) {
                        Fragment shopFragment = new ShopFragment();
                        fragmentTransaction.replace(R.id.fragmentHolder, shopFragment, "SHOP_FRAGMENT");
                        fragmentTransaction.addToBackStack("SHOP_FRAGMENT");
                        fragmentTransaction.commit();
                    }
                    break;
                case R.id.miCart:
                    Utils.hideKeyboard(this);
                    if (USER != null) {
                        if (bottom_navbar.getSelectedItemId() != R.id.miCart) {
                            Fragment cartFragment = new CartFragment();
                            fragmentTransaction.replace(R.id.fragmentHolder, cartFragment, "CART_FRAGMENT");
                            fragmentTransaction.addToBackStack("CART_FRAGMENT");
                            fragmentTransaction.commit();
                        }
                    }
                    else {
                        Utils.loginRequiredDialog(MainActivity.this, bottom_navbar, "You need to sign in to set up orders.");
                        bottom_navbar.getMenu().getItem(0).setChecked(true);
                    }
                    break;
                case R.id.miOrders:
                    Utils.hideKeyboard(this);
                    if (USER != null) {
                        if (bottom_navbar.getSelectedItemId() != R.id.miOrders) {
                            Fragment ordersFragment = new OrdersFragment();
                            fragmentTransaction.replace(R.id.fragmentHolder, ordersFragment, "ORDERS_FRAGMENT");
                            fragmentTransaction.addToBackStack("ORDERS_FRAGMENT");
                            fragmentTransaction.commit();
                        }
                    }
                    else {
                        Utils.loginRequiredDialog(MainActivity.this, bottom_navbar, "You need to sign in to view your order history.");
                        bottom_navbar.getMenu().getItem(0).setChecked(true);
                    }
                    break;
            }
            return true;
        });*/
    }

    private void countCartItems() {
        Utils.renderCartBadge(bottom_navbar);
    }

    @Override
    public void onBackPressed() {
        int backStackEntryCount = getSupportFragmentManager().getBackStackEntryCount();
        if (backStackEntryCount == 1) { // if navigation is at first backstack entry
            finish();
        } else {
            super.onBackPressed();
        }
    }

    private void initializeViews() {
        bottom_navbar = findViewById(R.id.bottom_navbar);
        btnProfile = findViewById(R.id.btnProfile);

        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction= fragmentManager.beginTransaction();
        Fragment shopFragment = new ShopFragment();
        fragmentTransaction.replace(R.id.fragmentHolder, shopFragment, "SHOP_FRAGMENT");
        fragmentTransaction.addToBackStack("SHOP_FRAGMENT");
        fragmentTransaction.commit();
        bottom_navbar.getMenu().getItem(0).setChecked(true);
    }

    private void backstackListener() {
        /*FragmentManager fragmentManager = getSupportFragmentManager();
        fragmentManager.addOnBackStackChangedListener(() -> {
            ShopFragment shopFragment = (ShopFragment) getSupportFragmentManager().findFragmentByTag("SHOP_FRAGMENT");
            CartFragment cartFragment = (CartFragment) getSupportFragmentManager().findFragmentByTag("CART_FRAGMENT");
            OrdersFragment ordersFragment = (OrdersFragment) getSupportFragmentManager().findFragmentByTag("ORDERS_FRAGMENT");
            ShopItemFragment shopItemFragment = (ShopItemFragment) getSupportFragmentManager().findFragmentByTag("SHOP_ITEM_FRAGMENT");

            if (shopFragment != null && shopFragment.isVisible() ||
                    shopItemFragment != null && shopItemFragment.isVisible() ) {
                bottom_navbar.getMenu().getItem(0).setChecked(true);
                softKeyboardListener();
            }
            else if (cartFragment != null && cartFragment.isVisible()) {
                bottom_navbar.getMenu().getItem(1).setChecked(true);
            }
            else if (ordersFragment != null && ordersFragment.isVisible()) {
                bottom_navbar.getMenu().getItem(2).setChecked(true);
            }
        });*/
    }

    private void softKeyboardListener() {
        /*final View activityRootView = findViewById(R.id.fragmentHolder);
        activityRootView.getViewTreeObserver().addOnGlobalLayoutListener(() -> {
            int heightDiff = activityRootView.getRootView().getHeight() - activityRootView.getHeight();
            if (heightDiff > Utils.dpToPx(MainActivity.this, 225)) {
                // if keyboard visible
                bottom_navbar.setVisibility(View.GONE);
            }
            else {
                bottom_navbar.setVisibility(View.VISIBLE);
            }
        });*/
    }

    private void buildReadyForPickupNotification() {
        String channelID = "Ready for Pick up Orders";
        NotificationCompat.Builder builder = new NotificationCompat.Builder(getApplicationContext(), channelID);
        builder.setContentTitle("BeautyPro Marketplace")
                .setSmallIcon(R.mipmap.ic_beautypro)
                .setContentText("Your order is now ready for pick-up.")
                .setAutoCancel(true)
                .setPriority(NotificationCompat.PRIORITY_MAX);

        Intent intent = new Intent(MainActivity.this, SplashActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

        PendingIntent pendingIntent = PendingIntent.getActivity(getApplicationContext(), 0, intent, PendingIntent.FLAG_MUTABLE);
        builder.setContentIntent(pendingIntent);

        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel notificationChannel = notificationManager.getNotificationChannel(channelID);

            if (notificationChannel == null) {
                int importance = NotificationManager.IMPORTANCE_HIGH;
                notificationChannel = new NotificationChannel(channelID, "Ready for Pick up Orders", importance);
                notificationChannel.enableVibration(true);
                notificationManager.createNotificationChannel(notificationChannel);
            }
        }

        notificationManager.notify(0, builder.build());
    }

    private void listenForReadyForPickupOrders() {
        Query qryReadyRequests = DB.collection("orders")
                .whereEqualTo("customer", Objects.requireNonNull(AUTH.getCurrentUser()).getUid())
                .whereEqualTo("status", "Ready for Pick-up")
                .orderBy("timestamp", Query.Direction.DESCENDING);
        qryReadyRequests.addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots, @Nullable FirebaseFirestoreException e) {
                //If something went wrong
                if (e != null)
                    Log.w("TAG", "ERROR : ", e);

                if (queryDocumentSnapshots != null && !queryDocumentSnapshots.isEmpty()) {
                    //Instead of simply using the entire query snapshot
                    //See the actual changes to query results between query snapshots (added, removed, and modified)
                    for (DocumentChange docChange : queryDocumentSnapshots.getDocumentChanges()) {
                        if (docChange.getType() == DocumentChange.Type.ADDED) {
                            buildReadyForPickupNotification();
                        }
                    }
                }
            }
        });
    }

    private void listenForInTransitOrders() {
        Query qryReadyRequests = DB.collection("orders")
                .whereEqualTo("customer", Objects.requireNonNull(AUTH.getCurrentUser()).getUid())
                .whereEqualTo("status", "In Transit")
                .orderBy("timestamp", Query.Direction.DESCENDING);
        qryReadyRequests.addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots, @Nullable FirebaseFirestoreException e) {
                //If something went wrong
                if (e != null)
                    Log.w("TAG", "ERROR : ", e);

                if (queryDocumentSnapshots != null && !queryDocumentSnapshots.isEmpty()) {
                    //Instead of simply using the entire query snapshot
                    //See the actual changes to query results between query snapshots (added, removed, and modified)
                    for (DocumentChange docChange : queryDocumentSnapshots.getDocumentChanges()) {
                        if (docChange.getType() == DocumentChange.Type.ADDED) {
                            buildInTransitNotification();
                        }
                    }
                }
            }
        });
    }

    private void buildInTransitNotification() {
        String channelID = "In Transit Orders";
        NotificationCompat.Builder builder = new NotificationCompat.Builder(getApplicationContext().getApplicationContext(), channelID);
        builder.setContentTitle("BeautyPro Marketplace")
                .setSmallIcon(R.mipmap.ic_beautypro)
                .setContentText("Your parcel is out for delivery.")
                .setAutoCancel(true)
                .setPriority(NotificationCompat.PRIORITY_MAX);

        Intent intent = new Intent(MainActivity.this, SplashActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

        PendingIntent pendingIntent = PendingIntent.getActivity(MainActivity.this, 0, intent, PendingIntent.FLAG_MUTABLE);
        builder.setContentIntent(pendingIntent);

        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel notificationChannel = notificationManager.getNotificationChannel(channelID);

            if (notificationChannel == null) {
                int importance = NotificationManager.IMPORTANCE_HIGH;
                notificationChannel = new NotificationChannel(channelID, "In Transit Orders", importance);
                notificationChannel.enableVibration(true);
                notificationManager.createNotificationChannel(notificationChannel);
            }
        }

        notificationManager.notify(1, builder.build());
    }

    private void listenForDelinquency() {
        Query qryUser = DB.collection("users")
                .whereEqualTo("uid", USER.getUid());

        qryUser.addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots, @Nullable FirebaseFirestoreException e) {
                DocumentSnapshot documentSnapshot = queryDocumentSnapshots.getDocuments().get(0);

                if (documentSnapshot.contains("isDelinquent") && documentSnapshot.contains("wasNotifiedForDelinquency")) {
                    boolean isDelinquent = Boolean.parseBoolean(documentSnapshot.get("isDelinquent").toString());
                    boolean wasNotifiedForDelinquency = Boolean.parseBoolean(documentSnapshot.get("wasNotifiedForDelinquency").toString());

                    if (isDelinquent && !wasNotifiedForDelinquency) {
                        buildDelinquencyNotification();
                        MaterialAlertDialogBuilder dialogFlaggedAsDelinquent = new MaterialAlertDialogBuilder(MainActivity.this);
                        dialogFlaggedAsDelinquent.setTitle("Behavior Warning");
                        dialogFlaggedAsDelinquent.setMessage("You have been flagged as a delinquent user for bad user behavior!");
                        dialogFlaggedAsDelinquent.setPositiveButton("Okay", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                updateWasNotifiedValue();
                            }
                        });
                        dialogFlaggedAsDelinquent.setOnDismissListener(new DialogInterface.OnDismissListener() {
                            @Override
                            public void onDismiss(DialogInterface dialogInterface) {
                                updateWasNotifiedValue();
                            }
                        });

                        buildDelinquencyNotification();
                    }
                }
            }

            private void updateWasNotifiedValue() {
                DB.collection("users").document(USER.getUid())
                        .update("wasNotifiedForDelinquency", true);
            }
        });
    }

    private void buildDelinquencyNotification() {
        String channelID = "Delinquency";
        NotificationCompat.Builder builder = new NotificationCompat.Builder(getApplicationContext().getApplicationContext(), channelID);
        builder.setContentTitle("BeautyPro Marketplace")
                .setSmallIcon(R.mipmap.ic_beautypro)
                .setContentText("NOTICE: You have been flagged as a delinquent user for bad user behavior!")
                .setAutoCancel(true)
                .setPriority(NotificationCompat.PRIORITY_MAX);

        Intent intent = new Intent(MainActivity.this, SplashActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

        PendingIntent pendingIntent = PendingIntent.getActivity(MainActivity.this, 0, intent, PendingIntent.FLAG_MUTABLE);
        builder.setContentIntent(pendingIntent);

        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel notificationChannel = notificationManager.getNotificationChannel(channelID);

            if (notificationChannel == null) {
                int importance = NotificationManager.IMPORTANCE_HIGH;
                notificationChannel = new NotificationChannel(channelID, "Delinquency", importance);
                notificationChannel.enableVibration(true);
                notificationManager.createNotificationChannel(notificationChannel);
            }
        }

        notificationManager.notify(1, builder.build());
    }
}