package com.beautypro.Dialogs;

import android.app.AlertDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.RadioGroup;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatDialogFragment;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.beautypro.Adapters.CartItemAdapter;
import com.beautypro.Adapters.OrderAdapter;
import com.beautypro.Adapters.ShopItemForRatingAdapter;
import com.beautypro.Fragments.ShopFragment;
import com.beautypro.Objects.CartItem;
import com.beautypro.Objects.Order;
import com.beautypro.Objects.Product;
import com.beautypro.Objects.ShopItem;
import com.beautypro.R;
import com.beautypro.Utils.Utils;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.radiobutton.MaterialRadioButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class RateDialog extends AppCompatDialogFragment {

    private String orderId;
    private ArrayList<Product> arrOrderItems;

    public void setData(String orderId, ArrayList<Product> arrOrderItems) {
        this.orderId = orderId;
        this.arrOrderItems = arrOrderItems;
    }

    private final StorageReference storageRef = FirebaseStorage.getInstance().getReference();

    FirebaseFirestore DB;
    FirebaseAuth AUTH;
    FirebaseUser USER;

    private void initializeFirebase() {
        DB = FirebaseFirestore.getInstance();
        AUTH = FirebaseAuth.getInstance();
        USER = AUTH.getCurrentUser();
    }

    ShopItemForRatingAdapter adapterShopItemForRating;
    RecyclerView rvProducts;
    MaterialButton btnSubmitRatings;

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.dialog_rate, null);

        initializeFirebase();
        initializeViews(view);
        loadRecyclerView();
        handleUserInteraction();

        builder.setView(view);
        return builder.create();
    }

    private void initializeViews(View view) {
        rvProducts = view.findViewById(R.id.rvProducts);
        btnSubmitRatings = view.findViewById(R.id.btnSubmitRatings);

        rvProducts.setHasFixedSize(true);
        rvProducts.setLayoutManager(new LinearLayoutManager(requireContext()));
    }

    private void loadRecyclerView() {
        adapterShopItemForRating = new ShopItemForRatingAdapter(requireContext(), arrOrderItems);
        rvProducts.setAdapter(adapterShopItemForRating);

        if (arrOrderItems.isEmpty()) {
            rvProducts.setVisibility(View.INVISIBLE);
        }
        else {
            rvProducts.setVisibility(View.VISIBLE);
        }
    }

    private void handleUserInteraction() {
        btnSubmitRatings.setOnClickListener(view -> {
            ArrayList<Double> arrRatings = new ArrayList<>();
            arrRatings.clear();

            btnSubmitRatings.setEnabled(false);
            for (int i = 0; i < rvProducts.getAdapter().getItemCount(); i++) {
                View itemView = rvProducts.getChildAt(i);
                RatingBar ratingBar = itemView.findViewById(R.id.ratingBar);

                double rating = ratingBar.getRating();

                if (rating == 0) {
                    Toast.makeText(requireContext(), "Please make sure to leave a rating for each item", Toast.LENGTH_SHORT).show();
                    btnSubmitRatings.setEnabled(true);
                    return;
                }

                arrRatings.add(rating);
            }

            // update product ratings
            for (int i = 0; i < arrOrderItems.size(); i++) {
                String productId = arrOrderItems.get(i).getProductId();
                int rating = arrRatings.get(i).intValue();

                if (rating == 1) {
                    DB.collection("products").document(productId)
                            .update("rating.oneStars", FieldValue.increment(1));
                }
                else if (rating == 2) {
                    DB.collection("products").document(productId)
                            .update("rating.twoStars", FieldValue.increment(1));
                }
                else if (rating == 3) {
                    DB.collection("products").document(productId)
                            .update("rating.threeStars", FieldValue.increment(1));
                }
                else if (rating == 4) {
                    DB.collection("products").document(productId)
                            .update("rating.fourStars", FieldValue.increment(1));
                }
                else if (rating == 5) {
                    DB.collection("products").document(productId)
                            .update("rating.fiveStars", FieldValue.increment(1));
                }
            }

            // update order isRated value
            DB.collection("orders").document(orderId)
                    .update("isRated", true)
                    .addOnCompleteListener(task -> {
                        Handler handler = new Handler();
                        handler.postDelayed(new Runnable() {
                            public void run() {
                                requireDialog().dismiss();
                            }
                        }, 1000);
                    });
        });
    }
}
