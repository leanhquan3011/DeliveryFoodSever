package com.leanhquan.deliveryfoodserver;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.jaredrummler.materialspinner.MaterialSpinner;
import com.leanhquan.deliveryfoodserver.Common.Common;
import com.leanhquan.deliveryfoodserver.Inteface.ItemClickListener;
import com.leanhquan.deliveryfoodserver.Model.Request;
import com.leanhquan.deliveryfoodserver.ViewHolder.OrderViewHolder;

public class OrderActivity extends AppCompatActivity {

    private RecyclerView                  recyclerView;
    private LinearLayout                  root;
    private RecyclerView.LayoutManager    layoutManager;
    private FirebaseDatabase              database;
    private DatabaseReference             requests;
    private MaterialSpinner               materialSpinner;

    private FirebaseRecyclerAdapter<Request, OrderViewHolder> adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order);

        database = FirebaseDatabase.getInstance();
        requests = database.getReference("requests");

        recyclerView = findViewById(R.id.listOrders);
        layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);

        loadHistory();
    }

    @Override
    public boolean onContextItemSelected(@NonNull MenuItem item) {
        if (item.getTitle().equals(Common.UPDATE)){
            showDialogUpdateOrder(adapter.getRef(item.getOrder()).getKey(), adapter.getItem(item.getOrder()));
        } else if (item.getTitle().equals(Common.DELETE)){
            showDialogDeleteOder(adapter.getRef(item.getOrder()).getKey());
        }
        return super.onContextItemSelected(item);
    }

    private void showDialogDeleteOder(String key) {
        requests.child(key).removeValue();
        Toast.makeText(this, "Deleted" + key, Toast.LENGTH_SHORT).show();
    }

    private void showDialogUpdateOrder(String key, final Request item) {
        AlertDialog.Builder update = new AlertDialog.Builder(this);
        update.setTitle("Update order");
        update.setMessage("Please choose status");

        LayoutInflater layoutInflater = this.getLayoutInflater();
        final View addOrderlayout = layoutInflater.inflate(R.layout.layout_update_order, null, false);

        materialSpinner = addOrderlayout.findViewById(R.id.StatusSpinner);
        materialSpinner.setItems("Đã đặt", "Đang giao", "Đã hoàn thành");

        update.setView(addOrderlayout);

        final String localkey = key;

        update.setPositiveButton("YES", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                item.setStatus(String.valueOf(materialSpinner.getSelectedIndex()));
                requests.child(localkey).setValue(item);
            }
        });

        update.setNegativeButton("NO", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        update.show();
    }

    @Override
    public void onStart() {
        super.onStart();
        if (adapter != null){
            adapter.startListening();
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        if (adapter != null){
            adapter.stopListening();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if (adapter != null){
            adapter.startListening();
        }
    }

    private void loadHistory() {
        Query query = FirebaseDatabase.getInstance().getReference().child("requests");
        FirebaseRecyclerOptions<Request> options = new FirebaseRecyclerOptions.Builder<Request>()
                .setQuery(query, Request.class)
                .build();
        adapter = new FirebaseRecyclerAdapter<Request, OrderViewHolder>(options) {
            @Override
            protected void onBindViewHolder(@NonNull OrderViewHolder holder, int position, @NonNull final Request model) {
                holder.txtOderId.setText("Code order #"+adapter.getRef(position).getKey());
                holder.txtOderStatus.setText(Common.convertCodeToStatus(model.getStatus()));
                holder.txtAddress.setText(model.getAddress());
                holder.txtOderPhone.setText(model.getPhone());
                Log.d("TAG", "onBindViewHolder: "+model.getPhone());
                holder.setItemClickListener(new ItemClickListener() {
                    @Override
                    public void onClick(View view, int position, boolean longClick) {
                       if (!longClick){
                           Intent tracking = new Intent(OrderActivity.this, TrackingOrderActivity.class);
                           Common.currentRequest = model;
                           startActivity(tracking);
                           Toast.makeText(OrderActivity.this, "goto tracking", Toast.LENGTH_SHORT).show();
                       } else {
                           Intent orderDetails = new Intent(OrderActivity.this, OrderDetailsActivity.class);
                           Common.currentRequest = model;
                           orderDetails.putExtra("orderID", adapter.getRef(position).getKey());
                           startActivity(orderDetails);
                           Toast.makeText(OrderActivity.this, "goto Details", Toast.LENGTH_SHORT).show();
                       }
                    }
                });
            }

            @NonNull
            @Override
            public OrderViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.layout_listorder,parent, false);
                OrderViewHolder viewHolder = new OrderViewHolder(view);
                return viewHolder;
            }
        };
        recyclerView.setAdapter(adapter);
    }

}
