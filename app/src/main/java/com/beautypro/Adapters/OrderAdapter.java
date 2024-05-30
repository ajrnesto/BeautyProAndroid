package com.beautypro.Adapters;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.beautypro.Dialogs.CheckoutDialog;
import com.beautypro.Dialogs.RateDialog;
import com.beautypro.Fragments.OrdersFragment;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.beautypro.Objects.Order;
import com.beautypro.Objects.Product;
import com.beautypro.R;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Objects;

public class OrderAdapter extends RecyclerView.Adapter<OrderAdapter.orderViewHolder> {

    private static final FirebaseFirestore DB = FirebaseFirestore.getInstance();
    private final StorageReference storageRef = FirebaseStorage.getInstance().getReference();

    Context context;
    ArrayList<Order> arrOrder;
    OrderItemAdapter orderItemsAdapter;
    private OnOrderListener mOnOrderListener;

    public OrderAdapter(Context context, ArrayList<Order> arrOrder, OnOrderListener onOrderListener) {
        this.context = context;
        this.arrOrder = arrOrder;
        this.mOnOrderListener = onOrderListener;
    }

    @NonNull
    @Override
    public orderViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.cardview_order, parent, false);
        return new orderViewHolder(view, mOnOrderListener);
    }

    @Override
    public void onBindViewHolder(@NonNull orderViewHolder holder, int position) {
        Order order = arrOrder.get(position);

        String id = order.getId();
        ArrayList<String> deliveryAddress = order.getDeliveryAddress();
        String deliveryOption = order.getDeliveryOption();
        String status = order.getStatus();
        long timestamp = order.getTimestamp();
        double total = order.getTotal();
        ArrayList<Product> arrOrderItems = order.getArrOrderItems();
        boolean isRated = order.isRated();

        holder.tvDeliveryAddress.setText("Delivery Address: "+String.join(", ", deliveryAddress));
        if (deliveryOption.equals("Delivery")) {
            holder.tvDeliveryAddress.setVisibility(View.VISIBLE);
        }
        else {
            holder.tvDeliveryAddress.setVisibility(View.GONE);
        }
        holder.tvDeliveryOption.setText("Delivery Option: "+deliveryOption);
        holder.tvStatus.setText(status);
        SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy, hh:mm aa");
        holder.tvTimestamp.setText(sdf.format(timestamp));

        DecimalFormat df = new DecimalFormat("0.00");
        holder.tvTotal.setText("₱"+df.format(total));

        holder.rvOrderItems.setHasFixedSize(true);
        holder.rvOrderItems.setLayoutManager(new LinearLayoutManager(holder.itemView.getContext()));

        orderItemsAdapter = new OrderItemAdapter(holder.itemView.getContext(), arrOrderItems);
        holder.rvOrderItems.setAdapter(orderItemsAdapter);

        if (Objects.equals(status, "Pending")) {
            holder.btnCancel.setVisibility(View.VISIBLE);
        }
        else {
            holder.btnCancel.setVisibility(View.GONE);
        }

        if (Objects.equals(status, "Delivered/Picked-up")) {
            if (!isRated) {
                holder.btnRate.setVisibility(View.VISIBLE);
                // holder.tvReviewSubmitted.setVisibility(View.GONE);
            }
            else {
                holder.btnRate.setVisibility(View.GONE);
                // holder.tvReviewSubmitted.setVisibility(View.VISIBLE);
            }
        }
        else {
            holder.btnRate.setVisibility(View.GONE);
            //
        }
    }

    @Override
    public int getItemCount() {
        return arrOrder.size();
    }

    public class orderViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{

        OnOrderListener onOrderListener;
        AppCompatImageView ivProduct;
        TextView tvTimestamp, tvStatus, tvDeliveryOption, tvDeliveryAddress, tvTotal, tvReviewSubmitted;
        RecyclerView rvOrderItems;
        MaterialButton btnCancel, btnRate;

        public orderViewHolder(@NonNull View itemView, OnOrderListener onOrderListener) {
            super(itemView);

            tvTimestamp = itemView.findViewById(R.id.tvTimestamp);
            tvStatus = itemView.findViewById(R.id.tvStatus);
            tvDeliveryOption = itemView.findViewById(R.id.tvDeliveryOption);
            tvDeliveryAddress = itemView.findViewById(R.id.tvDeliveryAddress);
            rvOrderItems = itemView.findViewById(R.id.rvOrderItems);
            tvTotal = itemView.findViewById(R.id.tvTotal);
            btnCancel = itemView.findViewById(R.id.btnCancel);
            btnRate = itemView.findViewById(R.id.btnRate);
            tvReviewSubmitted = itemView.findViewById(R.id.tvReviewSubmitted);

            this.onOrderListener = onOrderListener;
            itemView.setOnClickListener(this);

            btnCancel.setOnClickListener(view -> {
                MaterialAlertDialogBuilder dialogCancel = new MaterialAlertDialogBuilder(itemView.getContext());
                dialogCancel.setTitle("Cancel order?");
                dialogCancel.setMessage("Are you sure you want to cancel your order?");
                dialogCancel.setNegativeButton("Cancel Order", (dialogInterface, i) -> {
                    int position = getAdapterPosition();
                    Order currentOrder = arrOrder.get(position);
                    String incidentUid = currentOrder.getId();

                    for (Product orderItem : arrOrder.get(getAdapterPosition()).getArrOrderItems()) {
                        DB.collection("products").document(orderItem.getProductId())
                                .update("stock", FieldValue.increment(orderItem.getQuantity()));
                    }
                    DB.collection("orders").document(arrOrder.get(getAdapterPosition()).getId())
                            .delete()
                            .addOnSuccessListener(unused -> {
                                Toast.makeText(view.getContext(), "Successfully cancelled your order.", Toast.LENGTH_SHORT).show();
                            })
                            .addOnFailureListener(e -> {
                                Toast.makeText(view.getContext(), "Failed to cancel order: "+e, Toast.LENGTH_SHORT).show();
                            });
                });
                dialogCancel.setNeutralButton("Back", (dialogInterface, i) -> {

                });
                dialogCancel.show();
            });

            btnRate.setOnClickListener(view -> {
                String orderId = arrOrder.get(getAdapterPosition()).getId();
                ArrayList<Product> arrOrderItems = arrOrder.get(getAdapterPosition()).getArrOrderItems();

                RateDialog rateDialog = new RateDialog();
                rateDialog.setData(orderId, arrOrderItems);
                rateDialog.show(((FragmentActivity)context).getSupportFragmentManager(), "RATE_DIALOG");
            });
        }

        @Override
        public void onClick(View view) {
            onOrderListener.onOrderClick(getAdapterPosition());
        }
    }

    public interface OnOrderListener{
        void onOrderClick(int position);
    }
}
