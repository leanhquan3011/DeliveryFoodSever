package com.leanhquan.deliveryfoodserver.ViewHolder;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.leanhquan.deliveryfoodserver.Model.Order;
import com.leanhquan.deliveryfoodserver.R;

import java.util.List;

class OrderDetailsViewHolder extends RecyclerView.ViewHolder {

    public TextView txtName, txtQuantity, txtPrice;

    public OrderDetailsViewHolder(@NonNull View itemView) {
        super(itemView);

        txtName = itemView.findViewById(R.id.product_name);
        txtPrice = itemView.findViewById(R.id.product_price);
        txtQuantity = itemView.findViewById(R.id.product_quantity);
    }
}

public class OrderDetailsAdapter extends RecyclerView.Adapter<OrderDetailsViewHolder> {

    private List<Order> orders;

    public OrderDetailsAdapter(List<Order> orders) {
        this.orders = orders;
    }

    @NonNull
    @Override
    public OrderDetailsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.layout_details_order,parent, false);
        return new OrderDetailsViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull OrderDetailsViewHolder holder, int position) {
        Order myOrder = orders.get(position);
        holder.txtName.setText(myOrder.getProductName());
        holder.txtQuantity.setText(myOrder.getProductCount());
        holder.txtPrice.setText(myOrder.getProductPrice());
    }

    @Override
    public int getItemCount() {
        return orders.size();
    }
}
