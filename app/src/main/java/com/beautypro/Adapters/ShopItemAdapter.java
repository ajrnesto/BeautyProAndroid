package com.beautypro.Adapters;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.recyclerview.widget.RecyclerView;

import com.beautypro.Utils.Utils;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.beautypro.Objects.ShopItem;
import com.beautypro.R;
import com.makeramen.roundedimageview.RoundedImageView;
import com.squareup.picasso.Picasso;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;

public class ShopItemAdapter extends RecyclerView.Adapter<ShopItemAdapter.shopItemViewHolder> {

    private final StorageReference storageRef = FirebaseStorage.getInstance().getReference();

    Context context;
    ArrayList<ShopItem> arrShopItem;
    private OnShopItemListener mOnShopItemListener;

    public ShopItemAdapter(Context context, ArrayList<ShopItem> arrShopItem, OnShopItemListener onShopItemListener) {
        this.context = context;
        this.arrShopItem = arrShopItem;
        this.mOnShopItemListener = onShopItemListener;
    }

    @NonNull
    @Override
    public shopItemViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.cardview_shop_item, parent, false);
        return new shopItemViewHolder(view, mOnShopItemListener);
    }

    @Override
    public void onBindViewHolder(@NonNull shopItemViewHolder holder, int position) {
        ShopItem shopItem = arrShopItem.get(position);

        String categoryId = shopItem.getCategoryId();
        double price = shopItem.getPrice();
        String productDetails = shopItem.getProductDetails();
        String productName = shopItem.getProductName();
        int stock = shopItem.getStock();
        Long thumbnail = shopItem.getThumbnail();
        HashMap<String, Object> rating = shopItem.getRating();

        DecimalFormat df = new DecimalFormat("0.00");
        holder.tvPrice.setText("₱"+df.format(price));
        holder.tvName.setText(productName);
        holder.tvDetails.setText(productDetails);
        holder.tvStock.setText("Items left: " + stock);

        // rating
        Double calculatedRating = Utils.calculateRating(rating);
        Log.d("TAG", "Product ["+productName+"] 1 stars: "+rating.get("oneStars"));
        Log.d("TAG", "Product ["+productName+"] 2 stars: "+rating.get("twoStars"));
        Log.d("TAG", "Product ["+productName+"] 3 stars: "+rating.get("threeStars"));
        Log.d("TAG", "Product ["+productName+"] 4 stars: "+rating.get("fourStars"));
        Log.d("TAG", "Product ["+productName+"] 5 stars: "+rating.get("fiveStars"));
        Log.d("TAG", "Product ["+productName+"] calculated rating: "+calculatedRating);
        if (calculatedRating.isNaN()) {
            holder.ivStar.setVisibility(View.GONE);
            holder.tvRating.setVisibility(View.GONE);
        }
        else {
            holder.ivStar.setVisibility(View.VISIBLE);
            holder.tvRating.setVisibility(View.VISIBLE);
        }
        holder.tvRating.setText(new DecimalFormat("0.0").format(calculatedRating));

        storageRef.child("products/"+thumbnail).getDownloadUrl()
                .addOnSuccessListener(uri -> Picasso.get().load(uri).resize(240,200).centerInside().into(holder.ivProduct))
                .addOnFailureListener(e -> Picasso.get().load("https://via.placeholder.com/150?text=No+Image").fit().into(holder.ivProduct));
    }

    @Override
    public int getItemCount() {
        return arrShopItem.size();
    }

    public class shopItemViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{

        OnShopItemListener onShopItemListener;
        RoundedImageView ivProduct;
        AppCompatImageView ivStar;
        TextView tvName, tvDetails, tvStock, tvPrice, tvRating;

        public shopItemViewHolder(@NonNull View itemView, OnShopItemListener onShopItemListener) {
            super(itemView);

            ivProduct = itemView.findViewById(R.id.ivProduct);
            tvName = itemView.findViewById(R.id.tvName);
            tvDetails = itemView.findViewById(R.id.tvDetails);
            tvStock = itemView.findViewById(R.id.tvStock);
            tvPrice = itemView.findViewById(R.id.tvPrice);
            ivStar = itemView.findViewById(R.id.ivStar);
            tvRating = itemView.findViewById(R.id.tvRating);

            this.onShopItemListener = onShopItemListener;
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            onShopItemListener.onShopItemClick(getAdapterPosition());
        }
    }

    public interface OnShopItemListener{
        void onShopItemClick(int position);
    }
}
