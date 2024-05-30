package com.beautypro.Fragments;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.beautypro.Activities.AuthenticationActivity;
import com.beautypro.Dialogs.ProfileDialog;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.beautypro.Adapters.ShopItemAdapter;
import com.beautypro.Dialogs.ShopItemDialog;
import com.beautypro.Objects.ShopItem;
import com.beautypro.R;
import com.beautypro.Utils.Utils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Objects;

public class ShopFragment extends Fragment implements ShopItemAdapter.OnShopItemListener {

    FirebaseFirestore DB;
    FirebaseAuth AUTH;
    FirebaseUser USER;
    Query qryShop;
    ListenerRegistration velShop;

    private void initializeFirebase() {
        DB = FirebaseFirestore.getInstance();
        AUTH = FirebaseAuth.getInstance();
        USER = AUTH.getCurrentUser();
    }

    TextInputLayout tilSearch;
    TextInputEditText etSearch;
    MaterialButton btnCart, btnOrders, btnProfile;
    RecyclerView rvShop;
    AutoCompleteTextView menuCategories;
    ArrayList<String> itemsCategories;
    ArrayList<String> itemsCategoriesId;
    ArrayAdapter<String> adapterCategories;

    ArrayList<ShopItem> arrShopItem;
    ShopItemAdapter shopItemAdapter;
    ShopItemAdapter.OnShopItemListener onShopItemListener = this;
    String categorySelectedId = "-1";

    View view;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        view = inflater.inflate(R.layout.fragment_shop, container, false);

        initializeFirebase();
        initializeViews();
        initializeSpinners();
        handleButtonClicks();
        loadRecyclerView(null, categorySelectedId);

        return view;
    }

    private void initializeViews() {
        btnCart = view.findViewById(R.id.btnCart);
        btnOrders = view.findViewById(R.id.btnOrders);
        btnProfile = view.findViewById(R.id.btnProfile);
        tilSearch = view.findViewById(R.id.tilSearch);
        etSearch = view.findViewById(R.id.etSearch);
        rvShop = view.findViewById(R.id.rvShop);
        menuCategories = view.findViewById(R.id.menuCategories);

        Utils.Cache.setBoolean(requireContext(), "shop_item_dialog_is_visible", false);
    }

    private void initializeSpinners() {
        itemsCategories = new ArrayList<>();
        itemsCategoriesId = new ArrayList<>();

        itemsCategories.add("All");
        itemsCategoriesId.add("All");

        itemsCategories.add("Uncategorized");
        itemsCategoriesId.add("-1");
        DB.collection("categories").whereNotEqualTo("categoryName", "Uncategorized").get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
            @Override
            public void onSuccess(QuerySnapshot snapshots) {
                for (QueryDocumentSnapshot snapshot : snapshots) {
                    String id = snapshot.getId();
                    String categoryName = snapshot.get("categoryName").toString();

                    itemsCategoriesId.add(id);
                    itemsCategories.add(categoryName);
                }

                adapterCategories = new ArrayAdapter<>(requireContext(), R.layout.list_item, itemsCategories);
                menuCategories.setAdapter(adapterCategories);

                menuCategories.setOnItemClickListener((adapterView, view, position, id) -> {
                    if (position == 0) {
                        categorySelectedId = "-1";
                        loadRecyclerView(Objects.requireNonNull(etSearch.getText()).toString().toUpperCase(), categorySelectedId);
                    }
                    else {
                        categorySelectedId = itemsCategoriesId.get(position);
                        loadRecyclerView(Objects.requireNonNull(etSearch.getText()).toString().toUpperCase(), categorySelectedId);
                    }
                });
            }
        });
    }

    private void loadRecyclerView(String searchKey, String categoryId) {
        arrShopItem = new ArrayList<>();
        rvShop = view.findViewById(R.id.rvShop);
        rvShop.setHasFixedSize(true);
        /*LinearLayoutManager linearLayoutManager = new LinearLayoutManager(requireContext());
        linearLayoutManager.setStackFromEnd(true);
        linearLayoutManager.setReverseLayout(true);
        rvShop.setLayoutManager(linearLayoutManager);*/

        GridLayoutManager gridLayoutManager = new GridLayoutManager(requireContext(), 2);
        rvShop.setLayoutManager(gridLayoutManager);

        if (categoryId == "-1") {
            if (searchKey == null || searchKey.isEmpty()) {
                qryShop = DB.collection("products")
                        .whereNotEqualTo("stock", 0);
            }
            else {
                qryShop = DB.collection("products")
                        .orderBy("productNameAllCaps")
                        .whereNotEqualTo("stock", 0)
                        .startAt(searchKey)
                        .endAt(searchKey+'\uf8ff');
            }
        }
        else {
            if (searchKey == null || searchKey.isEmpty()) {
                qryShop = DB.collection("products")
                        .whereNotEqualTo("stock", 0)
                        .whereEqualTo("categoryId", categoryId);
            }
            else {
                qryShop = DB.collection("products")
                        .orderBy("productNameAllCaps")
                        .startAt(searchKey)
                        .endAt(searchKey+'\uf8ff')
                        .whereNotEqualTo("stock", 0)
                        .whereEqualTo("categoryId", categoryId);
            }
        }

        qryShop.addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException error) {
                if (error != null) {
                    Log.d("DEBUG", "Listen failed.", error);
                    return;
                }

                arrShopItem.clear();

                for (QueryDocumentSnapshot doc : value) {
                    /*arrShopItem.add(doc.toObject(ShopItem.class));*/


                    String id = (String) doc.get("id");
                    String categoryId = (String) doc.get("id");
                    double price = Double.parseDouble(doc.get("price").toString());
                    String productDetails = (String) doc.get("productDetails");
                    String productName = (String) doc.get("productName");
                    int stock = Integer.parseInt(doc.get("stock").toString());
                    Long thumbnail = null;
                    if (doc.getLong("thumbnail") != null) {
                        thumbnail = Long.parseLong(doc.get("thumbnail").toString());
                    }
                    HashMap<String, Object> rating = (HashMap<String, Object>) doc.get("rating");

                    arrShopItem.add(new ShopItem(
                            id,
                            categoryId,
                            price,
                            productDetails,
                            productName,
                            stock,
                            thumbnail,
                            rating
                    ));

                    shopItemAdapter.notifyDataSetChanged();
                }
            }
        });

        shopItemAdapter = new ShopItemAdapter(requireContext(), arrShopItem, onShopItemListener);
        rvShop.setAdapter(shopItemAdapter);
        shopItemAdapter.notifyDataSetChanged();
    }

    private void handleButtonClicks() {
        BottomNavigationView bottom_navbar = requireActivity().findViewById(R.id.bottom_navbar);
        FragmentManager fragmentManager = requireActivity().getSupportFragmentManager();
        FragmentTransaction fragmentTransaction= fragmentManager.beginTransaction();

        CartFragment cartFragment = (CartFragment) requireActivity().getSupportFragmentManager().findFragmentByTag("CART_FRAGMENT");

        btnCart.setOnClickListener(view -> {
            Utils.hideKeyboard(requireActivity());
            if (USER != null) {
                Fragment cartFragment1 = new CartFragment();
                fragmentTransaction.replace(R.id.fragmentHolder, cartFragment1, "CART_FRAGMENT");
                fragmentTransaction.addToBackStack("CART_FRAGMENT");
                fragmentTransaction.commit();
            }
            else {
                Utils.loginRequiredDialog(requireContext(), bottom_navbar, "You need to sign in to set up orders.");
            }
        });

        btnOrders.setOnClickListener(view -> {
            Utils.hideKeyboard(requireActivity());
            if (USER != null) {
                if (bottom_navbar.getSelectedItemId() != R.id.miOrders) {
                    Fragment ordersFragment = new OrdersFragment();
                    fragmentTransaction.replace(R.id.fragmentHolder, ordersFragment, "ORDERS_FRAGMENT");
                    fragmentTransaction.addToBackStack("ORDERS_FRAGMENT");
                    fragmentTransaction.commit();
                }
            }
            else {
                Utils.loginRequiredDialog(requireContext(), bottom_navbar, "You need to sign in to view your order history.");
            }
        });

        btnProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (USER != null) {
                    ProfileDialog profileDialog = new ProfileDialog();
                    profileDialog.show(requireActivity().getSupportFragmentManager(), "PROFILE_DIALOG");
                }
                else {
                    startActivity(new Intent(requireContext(), AuthenticationActivity.class));
                }
            }
        });

        tilSearch.setEndIconOnClickListener(view -> {
            Utils.hideKeyboard(requireActivity());
            loadRecyclerView(Objects.requireNonNull(etSearch.getText()).toString().toUpperCase(), categorySelectedId);
        });

        etSearch.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                Utils.hideKeyboard(requireActivity());
                loadRecyclerView(Objects.requireNonNull(etSearch.getText()).toString().toUpperCase(), categorySelectedId);
                return true;
            }
            return false;
        });

        etSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if (charSequence.toString().isEmpty()) {
                    loadRecyclerView(Objects.requireNonNull(etSearch.getText()).toString().toUpperCase(), categorySelectedId);
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });
    }

    @Override
    public void onShopItemClick(int position) {
        if (!Utils.Cache.getBoolean(requireContext(), "shop_item_dialog_is_visible")) {
            ShopItemDialog shopItemDialog = new ShopItemDialog();

            ShopItem shopItem = arrShopItem.get(position);
            Bundle args = new Bundle();
            args.putString("id", shopItem.getId());
            args.putDouble("price", shopItem.getPrice());
            args.putString("productDetails", shopItem.getProductDetails());
            args.putString("productName", shopItem.getProductName());
            args.putInt("stock", shopItem.getStock());
            args.putDouble("ratingCount", Integer.parseInt(shopItem.getRating().get("oneStars").toString()) +
                    Integer.parseInt(shopItem.getRating().get("twoStars").toString()) +
                    Integer.parseInt(shopItem.getRating().get("threeStars").toString()) +
                    Integer.parseInt(shopItem.getRating().get("fourStars").toString()) +
                    Integer.parseInt(shopItem.getRating().get("fiveStars").toString()));
            args.putDouble("rating", Utils.calculateRating(shopItem.getRating()));
            if (shopItem.getThumbnail() != null) {
                args.putLong("thumbnail", shopItem.getThumbnail());
            }
            /*shopItemDialog.setArguments(args);
            shopItemDialog.show(requireActivity().getSupportFragmentManager(), "SHOP_ITEM_DIALOG");

            Utils.Cache.setBoolean(requireContext(), "shop_item_dialog_is_visible", true);*/

            FragmentManager fragmentManager = requireActivity().getSupportFragmentManager();
            FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
            Fragment shopItemFragment = new ShopItemFragment();
            shopItemFragment.setArguments(args);
            fragmentTransaction.replace(R.id.fragmentHolder, shopItemFragment, "SHOP_ITEM_FRAGMENT");
            fragmentTransaction.addToBackStack("SHOP_ITEM_FRAGMENT");
            fragmentTransaction.commit();
        }
    }
}