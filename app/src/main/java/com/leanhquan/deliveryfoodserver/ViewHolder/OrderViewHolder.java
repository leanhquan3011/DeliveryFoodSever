package com.leanhquan.deliveryfoodserver.ViewHolder;

import android.view.ContextMenu;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.leanhquan.deliveryfoodserver.Common.Common;
import com.leanhquan.deliveryfoodserver.Inteface.ItemClickListener;
import com.leanhquan.deliveryfoodserver.R;

public class OrderViewHolder extends RecyclerView.ViewHolder {

    public TextView txtOderId,
            txtOderPhone,
            txtOderStatus,
            txtAddress,
            txtEdit,
            txtRemove,
            txtDetails,
            txtDirection;

    public OrderViewHolder(@NonNull View itemView) {
        super(itemView);
        txtOderId = itemView.findViewById(R.id.order_id);
        txtOderPhone = itemView.findViewById(R.id.order_phone);
        txtOderStatus = itemView.findViewById(R.id.order_status);
        txtAddress = itemView.findViewById(R.id.order_address);
        txtEdit = itemView.findViewById(R.id.txtEditOrder);
        txtRemove = itemView.findViewById(R.id.txtRemoveOrder);
        txtDetails = itemView.findViewById(R.id.txtDetailsOrder);
        txtDirection = itemView.findViewById(R.id.txtDirectionOrder);

    }

}
