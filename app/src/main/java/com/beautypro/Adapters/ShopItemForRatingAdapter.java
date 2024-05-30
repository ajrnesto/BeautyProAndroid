package com.beautypro.Adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.beautypro.Objects.Product;
import com.beautypro.R;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.makeramen.roundedimageview.RoundedImageView;
import com.squareup.picasso.Picasso;

import java.text.DecimalFormat;
import java.util.ArrayList;

public class ShopItemForRatingAdapter extends RecyclerView.Adapter<ShopItemForRatingAdapter.orderItemViewHolder> {

    private final StorageReference storageRef = FirebaseStorage.getInstance().getReference();

    Context context;
    ArrayList<Product> arrOrderItems;

    public ShopItemForRatingAdapter(Context context, ArrayList<Product> arrOrderItems) {
        this.context = context;
        this.arrOrderItems = arrOrderItems;
    }

    @NonNull
    @Override
    public orderItemViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.cardview_shop_item_for_rating, parent, false);
        return new orderItemViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull orderItemViewHolder holder, int position) {
        Product orderItem = arrOrderItems.get(position);

        if (orderItem.getProductId() == "-1") {
            Picasso.get().load("https://via.placeholder.com/150?text=No+Image").fit().into(holder.ivProduct);
            holder.tvName.setText("Deleted Item");
            return;
        }

        String productName = orderItem.getProductName();
        String productDetails = orderItem.getProductDetails();
        Long thumbnail = orderItem.getThumbnail();

        holder.tvName.setText(productName);

        storageRef.child("products/"+thumbnail).getDownloadUrl()
                .addOnSuccessListener(uri -> Picasso.get().load(uri).resize(120,120).centerInside().into(holder.ivProduct))
                .addOnFailureListener(e -> Picasso.get().load("https://via.placeholder.com/150?text=No+Image").fit().into(holder.ivProduct));
    }

    @Override
    public int getItemCount() {
        return arrOrderItems.size();
    }

    public class orderItemViewHolder extends RecyclerView.ViewHolder {

        RoundedImageView ivProduct;
        TextView tvName;
        RatingBar ratingBar;

        public orderItemViewHolder(@NonNull View itemView) {
            super(itemView);

            ivProduct = itemView.findViewById(R.id.ivProduct);
            tvName = itemView.findViewById(R.id.tvName);
            ratingBar = itemView.findViewById(R.id.ratingBar);
        }
    }
}
