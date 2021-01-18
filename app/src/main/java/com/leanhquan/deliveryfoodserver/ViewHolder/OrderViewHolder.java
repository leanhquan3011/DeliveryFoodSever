package com.leanhquan.deliveryfoodserver.ViewHolder;

import android.view.ContextMenu;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.leanhquan.deliveryfoodserver.Common.Common;
import com.leanhquan.deliveryfoodserver.Inteface.ItemClickListener;
import com.leanhquan.deliveryfoodserver.R;

public class OrderViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener, View.OnLongClickListener, View.OnCreateContextMenuListener {

    public TextView txtOderId, txtOderPhone, txtOderStatus, txtAddress;
    private ItemClickListener itemClickListener;

    public OrderViewHolder(@NonNull View itemView) {
        super(itemView);
        txtOderId = itemView.findViewById(R.id.order_id);
        txtOderPhone = itemView.findViewById(R.id.order_phone);
        txtOderStatus = itemView.findViewById(R.id.order_status);
        txtAddress = itemView.findViewById(R.id.order_address);
        itemView.setOnClickListener(this);
        itemView.setOnCreateContextMenuListener(this);
        itemView.setOnLongClickListener(this);
    }

    @Override
    public void onClick(View v) {
        itemClickListener.onClick(v,getAdapterPosition(),false);
    }

    public void setItemClickListener(ItemClickListener itemClickListener) {
        this.itemClickListener = itemClickListener;
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        menu.setHeaderTitle("select action");
        menu.add(0, 0, getAdapterPosition(), Common.UPDATE);
        menu.add(0, 1, getAdapterPosition(), Common.DELETE);
    }

    @Override
    public boolean onLongClick(View v) {
        itemClickListener.onClick(v,getAdapterPosition(),true);
        return true;
    }
}
