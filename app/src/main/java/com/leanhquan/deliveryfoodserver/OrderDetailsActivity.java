package com.leanhquan.deliveryfoodserver;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.widget.TextView;

import com.leanhquan.deliveryfoodserver.Common.Common;
import com.leanhquan.deliveryfoodserver.ViewHolder.OrderDetailsAdapter;

public class OrderDetailsActivity extends AppCompatActivity {

    private TextView orderID, orderPhone, orderAddress, orderTotal;
    private String orderID_value = "";
    private RecyclerView listFoodsOrder;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order_details);
        init();

        listFoodsOrder = findViewById(R.id.listFoodsOrder);
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(this);
        listFoodsOrder.setLayoutManager(layoutManager);

        if (getIntent() != null){
            orderID_value = getIntent().getStringExtra("orderID");
        }

        orderID.setText("#"+orderID_value);
        orderPhone.setText(Common.currentRequest.getPhone());
        orderAddress.setText(Common.currentRequest.getAddress());
        orderTotal.setText(Common.currentRequest.getTotal());

        OrderDetailsAdapter adapter = new OrderDetailsAdapter(Common.currentRequest.getFoods());
        adapter.notifyDataSetChanged();
        listFoodsOrder.setAdapter(adapter);
    }

    private void init() {
        orderID = findViewById(R.id.order_id);
        orderPhone = findViewById(R.id.order_phone);
        orderAddress = findViewById(R.id.order_address);
        orderTotal = findViewById(R.id.order_total);
    }
}
