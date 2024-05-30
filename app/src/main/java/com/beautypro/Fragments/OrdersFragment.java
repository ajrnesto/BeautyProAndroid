package com.beautypro.Fragments;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.media.Rating;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.beautypro.Activities.SplashActivity;
import com.google.android.material.tabs.TabLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.beautypro.Adapters.OrderAdapter;
import com.beautypro.Objects.Order;
import com.beautypro.Objects.Product;
import com.beautypro.R;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class OrdersFragment extends Fragment implements OrderAdapter.OnOrderListener {

    FirebaseFirestore DB;
    FirebaseAuth AUTH;
    FirebaseUser USER;

    private void initializeFirebase() {
        DB = FirebaseFirestore.getInstance();
        AUTH = FirebaseAuth.getInstance();
        USER = AUTH.getCurrentUser();
    }

    View view;

    TabLayout tabOrders;
    RecyclerView rvOrders;

    ArrayList<Order> arrOrders;
    ArrayList<Product> arrOrderItems;
    OrderAdapter orderAdapter;
    OrderAdapter.OnOrderListener onOrderListener = this;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        view = inflater.inflate(R.layout.fragment_orders, container, false);

        initializeFirebase();
        initializeViews();
        loadRecyclerView(tabOrders.getSelectedTabPosition());

        tabOrders.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                loadRecyclerView(tabOrders.getSelectedTabPosition());
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });

        return view;
    }

    private void loadRecyclerView(int tabIndex) {
        arrOrders = new ArrayList<>();
        arrOrderItems = new ArrayList<>();
        rvOrders = view.findViewById(R.id.rvOrders);
        rvOrders.setHasFixedSize(true);
        rvOrders.setLayoutManager(new LinearLayoutManager(requireContext()));

        String statusFilter = "";
        /*if (tabIndex == 0) {
            statusFilter = "Pending";
        }
        else if (tabIndex == 1) {
            statusFilter = "Preparing";
        }
        else if (tabIndex == 2) {
            statusFilter = "Ready for Pick-up";
        }
        else if (tabIndex == 3) {
            statusFilter = "In Transit";
        }
        else if (tabIndex == 4) {
            statusFilter = "Delivered/Picked-up";
        }
        else if (tabIndex == 5) {
            statusFilter = "Failed Delivery";
        }*/
        if (tabIndex == 0) {
            statusFilter = "Pending";
        }
        else if (tabIndex == 1) {
            statusFilter = "Preparing";
        }
        else if (tabIndex == 2) {
            statusFilter = "Ready for Pick-up";
        }
        else if (tabIndex == 3) {
            statusFilter = "In Transit";
        }
        else if (tabIndex == 4) {
            statusFilter = "Delivered/Picked-up";
        }
        else if (tabIndex == 5) {
            statusFilter = "Failed Delivery";
        }

        Query qryMyOrders = DB.collection("orders")
                        .whereEqualTo("customer", Objects.requireNonNull(AUTH.getCurrentUser()).getUid())
                        .whereEqualTo("status", statusFilter)
                        .orderBy("timestamp", Query.Direction.DESCENDING);

        qryMyOrders.addSnapshotListener((value, error) -> {
            arrOrders.clear();

            for (QueryDocumentSnapshot doc : value) {
                /*Order order = doc.toObject(Order.class);
                arrOrders.add(order);
                Log.d("TAG", "Order isRated?: "+order.isRated());*/
                String id = (String) doc.getData().get("id");
                String customer = (String) doc.getData().get("customer");
                ArrayList<String> deliveryAddress = (ArrayList<String>) doc.getData().get("deliveryAddress");
                String deliveryOption = (String) doc.getData().get("deliveryOption");
                String status = (String) doc.getData().get("status");
                long timestamp = Long.parseLong(doc.getData().get("timestamp").toString());
                double total = Double.parseDouble(doc.getData().get("total").toString());
                ArrayList<Product> arrOrderItems = new ArrayList<>();
                boolean isRated = Boolean.parseBoolean(doc.getData().get("isRated").toString());

                arrOrders.add(new Order(
                        id,
                        customer,
                        deliveryAddress,
                        deliveryOption,
                        status,
                        timestamp,
                        total,
                        arrOrderItems,
                        isRated
                ));

                DB.collection("orders").document(id)
                        .collection("orderItems")
                        .get()
                        .addOnCompleteListener(task -> {
                            if (task.isSuccessful()) {
                                for (QueryDocumentSnapshot item : task.getResult()) {

                                    String productId = (String) item.getData().get("productId");
                                    int quantity = Integer.parseInt(item.getData().get("quantity").toString());

                                    DB.collection("products").document(productId)
                                            .get()
                                            .addOnCompleteListener(task1 -> {
                                                if (task1.isSuccessful()) {
                                                    DocumentSnapshot productSnapshot = task1.getResult();

                                                    /*if (product == null) {
                                                        return;
                                                    }*/

                                                    if (productSnapshot.getData() == null) {
                                                        arrOrderItems.add(new Product(
                                                                "-1",
                                                                0,
                                                                "Deleted Item",
                                                                "",
                                                                0,
                                                                null,
                                                                new HashMap<String, Object>() {{
                                                                    put("oneStars", 0);
                                                                    put("twoStars", 0);
                                                                    put("threeStars", 0);
                                                                    put("fourStars", 0);
                                                                    put("fiveStars", 0);
                                                                }}
                                                        ));
                                                        orderAdapter.notifyDataSetChanged();

                                                        return;
                                                    }

                                                    Product product = productSnapshot.toObject(Product.class);

                                                    arrOrderItems.add(new Product(
                                                            productId,
                                                            quantity,
                                                            product.getProductName(),
                                                            product.getProductDetails(),
                                                            product.getPrice(),
                                                            product.getThumbnail()!=null?product.getThumbnail():null,
                                                            product.getRating()
                                                    ));

                                                    orderAdapter.notifyDataSetChanged();
                                                }
                                            });
                                }
                            }
                        });
            }

            if (arrOrders.isEmpty()) {
                rvOrders.setVisibility(View.INVISIBLE);
            }
            else {
                rvOrders.setVisibility(View.VISIBLE);
            }
        });

        orderAdapter = new OrderAdapter(requireContext(), arrOrders, onOrderListener);
        rvOrders.setAdapter(orderAdapter);
    }

    private void initializeViews() {
        tabOrders = view.findViewById(R.id.tabOrders);
        rvOrders = view.findViewById(R.id.rvOrders);
    }

    @Override
    public void onOrderClick(int position) {
        
    }
}