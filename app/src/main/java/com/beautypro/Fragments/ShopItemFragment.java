package com.beautypro.Fragments;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.widget.AppCompatImageView;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.beautypro.Utils.Utils;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.beautypro.Activities.AuthenticationActivity;
import com.beautypro.R;
import com.makeramen.roundedimageview.RoundedImageView;
import com.squareup.picasso.Picasso;

import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class ShopItemFragment extends Fragment {

    private final StorageReference storageRef = FirebaseStorage.getInstance().getReference();

    FirebaseFirestore DB;
    FirebaseAuth AUTH;
    FirebaseUser USER;

    private void initializeFirebase() {
        DB = FirebaseFirestore.getInstance();
        AUTH = FirebaseAuth.getInstance();
        USER = AUTH.getCurrentUser();
    }

    View view;

    String id;
    double price;
    Double rating, ratingCount;
    String productDetails;
    String productName;
    int stock;
    Long thumbnail;

    RoundedImageView ivProduct;
    AppCompatImageView ivStar1, ivStar2, ivStar3, ivStar4, ivStar5;
    TextView tvName, tvDetails, tvPrice, tvRating, tvStock, tvTotal;
    MaterialButton btnBack, btnDecrement, btnIncrement, btnAddToCart;
    TextInputEditText etQuantity;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        view = inflater.inflate(R.layout.fragment_shop_item, container, false);

        initializeFirebase();
        initiate(view);
        loadItem();
        buttonHandler();

        etQuantity.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                calculateTotal();

                if (Objects.requireNonNull(etQuantity.getText()).toString().isEmpty()) {
                    return;
                }

                float quantity = Float.parseFloat(etQuantity.getText().toString());

                if (quantity > stock) {
                    etQuantity.setText(""+stock);
                    etQuantity.setSelection(etQuantity.length());
                }

                if (quantity < 1) {
                    etQuantity.setText(""+1);
                    etQuantity.setSelection(etQuantity.length());
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });

        return view;
    }

    private void initiate(View view) {
        btnBack = view.findViewById(R.id.btnBack);
        ivProduct = view.findViewById(R.id.ivProduct);
        tvName = view.findViewById(R.id.tvName);
        tvDetails = view.findViewById(R.id.tvDetails);
        tvPrice = view.findViewById(R.id.tvPrice);
        ivStar1 = view.findViewById(R.id.ivStar1);
        ivStar2 = view.findViewById(R.id.ivStar2);
        ivStar3 = view.findViewById(R.id.ivStar3);
        ivStar4 = view.findViewById(R.id.ivStar4);
        ivStar5 = view.findViewById(R.id.ivStar5);
        tvRating = view.findViewById(R.id.tvRating);
        tvStock = view.findViewById(R.id.tvStock);
        tvTotal = view.findViewById(R.id.tvTotal);
        btnDecrement = view.findViewById(R.id.btnDecrement);
        btnIncrement = view.findViewById(R.id.btnIncrement);
        btnAddToCart = view.findViewById(R.id.btnRemove);
        etQuantity = view.findViewById(R.id.etQuantity);
    }

    private void loadItem() {
        assert getArguments() != null;
        id = getArguments().getString("id");
        Log.d("DEBUG", "RECEIVED ID: "+id);
        price = getArguments().getDouble("price");
        rating = getArguments().getDouble("rating");
        ratingCount = getArguments().getDouble("ratingCount");
        productDetails = getArguments().getString("productDetails");
        productName = getArguments().getString("productName");
        stock = getArguments().getInt("stock");
        thumbnail = getArguments().getLong("thumbnail");

        if (stock < 1) {
            btnIncrement.setEnabled(false);
            btnDecrement.setEnabled(false);
            etQuantity.setEnabled(false);
            btnAddToCart.setEnabled(false);
            tvStock.setTextColor(getResources().getColor(R.color.red_danger));
            etQuantity.setText("0");
        }

        tvName.setText(productName);
        tvDetails.setText(productDetails);
        DecimalFormat df = new DecimalFormat("0.00");
        tvPrice.setText("₱"+df.format(price));
        tvStock.setText("Items left: "+stock);
        storageRef.child("products/"+thumbnail).getDownloadUrl()
                .addOnSuccessListener(uri -> Picasso.get().load(uri).resize(360,360).centerInside().into(ivProduct))
                .addOnFailureListener(e -> Picasso.get().load("https://via.placeholder.com/150?text=No+Image").fit().into(ivProduct));

        // rating
        if (rating.isNaN()) {
            ivStar1.setImageResource(R.drawable.baseline_star_rate_24);
            ivStar1.setColorFilter(ContextCompat.getColor(requireContext(), R.color.gray), android.graphics.PorterDuff.Mode.SRC_IN);
            ivStar2.setImageResource(R.drawable.baseline_star_rate_24);
            ivStar2.setColorFilter(ContextCompat.getColor(requireContext(), R.color.gray), android.graphics.PorterDuff.Mode.SRC_IN);
            ivStar3.setImageResource(R.drawable.baseline_star_rate_24);
            ivStar3.setColorFilter(ContextCompat.getColor(requireContext(), R.color.gray), android.graphics.PorterDuff.Mode.SRC_IN);
            ivStar4.setImageResource(R.drawable.baseline_star_rate_24);
            ivStar4.setColorFilter(ContextCompat.getColor(requireContext(), R.color.gray), android.graphics.PorterDuff.Mode.SRC_IN);
            ivStar5.setImageResource(R.drawable.baseline_star_rate_24);
            ivStar5.setColorFilter(ContextCompat.getColor(requireContext(), R.color.gray), android.graphics.PorterDuff.Mode.SRC_IN);

            tvRating.setText(ratingCount + " Ratings");
        }
        else {
            if (rating < 5d) {
                ivStar5.setImageResource(R.drawable.outline_star_half_24);
                ivStar5.setColorFilter(ContextCompat.getColor(requireContext(), R.color.yellow_amber), android.graphics.PorterDuff.Mode.SRC_IN);
            }
            if (rating <= 4d) {
                ivStar5.setImageResource(R.drawable.baseline_star_rate_24);
                ivStar5.setColorFilter(ContextCompat.getColor(requireContext(), R.color.gray), android.graphics.PorterDuff.Mode.SRC_IN);
                ivStar4.setImageResource(R.drawable.outline_star_half_24);
                ivStar4.setColorFilter(ContextCompat.getColor(requireContext(), R.color.yellow_amber), android.graphics.PorterDuff.Mode.SRC_IN);
            }
            if (rating <= 3d) {
                ivStar4.setImageResource(R.drawable.baseline_star_rate_24);
                ivStar4.setColorFilter(ContextCompat.getColor(requireContext(), R.color.gray), android.graphics.PorterDuff.Mode.SRC_IN);
                ivStar3.setImageResource(R.drawable.outline_star_half_24);
                ivStar3.setColorFilter(ContextCompat.getColor(requireContext(), R.color.yellow_amber), android.graphics.PorterDuff.Mode.SRC_IN);
            }
            if (rating <= 2d) {
                ivStar3.setImageResource(R.drawable.baseline_star_rate_24);
                ivStar3.setColorFilter(ContextCompat.getColor(requireContext(), R.color.gray), android.graphics.PorterDuff.Mode.SRC_IN);
                ivStar2.setImageResource(R.drawable.outline_star_half_24);
                ivStar2.setColorFilter(ContextCompat.getColor(requireContext(), R.color.yellow_amber), android.graphics.PorterDuff.Mode.SRC_IN);
            }
            if (rating == 1d) {
                ivStar2.setImageResource(R.drawable.baseline_star_rate_24);
                ivStar2.setColorFilter(ContextCompat.getColor(requireContext(), R.color.gray), android.graphics.PorterDuff.Mode.SRC_IN);
                ivStar1.setImageResource(R.drawable.baseline_star_rate_24);
                ivStar1.setColorFilter(ContextCompat.getColor(requireContext(), R.color.yellow_amber), android.graphics.PorterDuff.Mode.SRC_IN);
            }
            tvRating.setText(new DecimalFormat("0.0").format(rating) + "  |  " + new DecimalFormat("#").format(ratingCount) + " ratings");
        }
        
        calculateTotal();
    }

    private void calculateTotal() {
        if (Objects.requireNonNull(etQuantity.getText()).toString().isEmpty()) {
            return;
        }

        float quantity = Float.parseFloat(etQuantity.getText().toString());
        float total = (float) (quantity * price);

        DecimalFormat df = new DecimalFormat("0.00");
        tvTotal.setText("₱"+df.format(total));
    }

    private void buttonHandler() {
        btnBack.setOnClickListener(view -> requireActivity().onBackPressed());

        btnDecrement.setOnClickListener(view -> {
            int quantity;

            if (Objects.requireNonNull(etQuantity.getText()).toString().isEmpty()) {
                quantity = 1;
            }
            else {
                quantity = Integer.parseInt(etQuantity.getText().toString());
            }

            if (quantity > 1) {
                quantity--;
                etQuantity.setText(""+quantity);
                etQuantity.setSelection(etQuantity.length());
                calculateTotal();
            }
        });

        btnIncrement.setOnClickListener(view -> {
            int quantity;

            if (Objects.requireNonNull(etQuantity.getText()).toString().isEmpty()) {
                quantity = 1;
            }
            else {
                quantity = Integer.parseInt(etQuantity.getText().toString());
            }

            if (quantity < stock) {
                quantity++;
                etQuantity.setText(""+quantity);
                etQuantity.setSelection(etQuantity.length());
                calculateTotal();
            }
        });

        btnAddToCart.setOnClickListener(view -> {
            if (AUTH.getCurrentUser() != null) {
                btnAddToCart.setEnabled(false);
                Map<String, Object> cartItem = new HashMap<>();
                int quantity = Integer.parseInt(Objects.requireNonNull(etQuantity.getText()).toString());
                cartItem.put("productId", id);
                cartItem.put("quantity", quantity);

                // check if item exists
                DocumentReference refItem = DB.collection("carts").document(Objects.requireNonNull(AUTH.getCurrentUser()).getUid())
                        .collection("items").document(id);

                refItem.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                        if (documentSnapshot.exists()) {
                            int snapshotQuantity = Integer.parseInt(Objects.requireNonNull(documentSnapshot.get("quantity")).toString());

                            int newQuantity = snapshotQuantity + quantity;
                            cartItem.put("quantity", newQuantity);

                            DB.collection("carts").document(Objects.requireNonNull(AUTH.getCurrentUser()).getUid())
                                    .collection("items").document(id)
                                    .set(cartItem, SetOptions.merge())
                                    .addOnSuccessListener(unused -> {
                                        Toast.makeText(requireContext(), "Item added to cart", Toast.LENGTH_SHORT).show();
                                        /*requireDialog().dismiss();*/

                                        requireActivity().getSupportFragmentManager().popBackStack();
                                    })
                                    .addOnFailureListener(e -> {
                                        Toast.makeText(requireContext(), "Something went wrong...", Toast.LENGTH_SHORT).show();
                                        Toast.makeText(requireContext(), "Failed to add item to cart", Toast.LENGTH_SHORT).show();
                                    });

                        }
                        else {
                            DB.collection("carts").document(Objects.requireNonNull(AUTH.getCurrentUser()).getUid())
                                    .collection("items").document(id)
                                    .set(cartItem, SetOptions.merge())
                                    .addOnSuccessListener(unused -> {
                                        Toast.makeText(requireActivity().getApplicationContext(), "Item added to cart", Toast.LENGTH_SHORT).show();
                                        /*requireDialog().dismiss();*/

                                        requireActivity().getSupportFragmentManager().popBackStack();
                                    })
                                    .addOnFailureListener(e -> {
                                        Toast.makeText(requireContext().getApplicationContext(), "Something went wrong...", Toast.LENGTH_SHORT).show();
                                        Toast.makeText(requireContext().getApplicationContext(), "Failed to add item to cart", Toast.LENGTH_SHORT).show();
                                    });
                        }
                    }
                });

                /**/
            }
            else {
                MaterialAlertDialogBuilder dialogLoginRequired = new MaterialAlertDialogBuilder(requireContext());
                dialogLoginRequired.setTitle("Sign in required");
                dialogLoginRequired.setMessage("You need to sign in to place orders.");
                dialogLoginRequired.setPositiveButton("Log in", (dialogInterface, i) -> {
                    requireContext().startActivity(new Intent(requireContext(), AuthenticationActivity.class));
                    ((Activity)requireContext()).finish();
                });
                dialogLoginRequired.setNeutralButton("Back", (dialogInterface, i) -> { });
                dialogLoginRequired.show();
            }
        });
    }
}