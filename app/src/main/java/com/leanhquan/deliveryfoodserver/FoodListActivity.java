package com.leanhquan.deliveryfoodserver;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import com.andremion.counterfab.CounterFab;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.leanhquan.deliveryfoodserver.Common.Common;
import com.leanhquan.deliveryfoodserver.Inteface.ItemClickListener;
import com.leanhquan.deliveryfoodserver.Model.Food;
import com.leanhquan.deliveryfoodserver.ViewHolder.FoodViewHolder;
import com.rengwuxian.materialedittext.MaterialEditText;
import com.squareup.picasso.Picasso;

import java.util.UUID;

public class FoodListActivity extends AppCompatActivity {
    private final int                                       PICK_IMAGE_REQUEST = 71;
    private String                                          menuId = "";
    private RecyclerView                                    recyclerViewListFood;
    private RecyclerView.LayoutManager                      layoutManagerListFood;
    private FirebaseDatabase                                database;
    private DatabaseReference                               foodList;
    private FirebaseStorage                                 storage;
    private StorageReference                                storageReference;
    private MaterialEditText                                edtNamenewFood, edtDescriptionFood, edtPricenewFood;
    private Button                                          btnSelect, btnUpload;
    private Food                                            newFood;
    private Uri                                             saveUri;
    private CounterFab                                      fapCart;
    private CoordinatorLayout                               root;

    //list food
    private FirebaseRecyclerAdapter<Food, FoodViewHolder> adapterFoodList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_food_list);

        database = FirebaseDatabase.getInstance();
        foodList = database.getReference("foods");
        storage = FirebaseStorage.getInstance();
        storageReference = storage.getReference();

        root = findViewById(R.id.root);

        recyclerViewListFood = findViewById(R.id.recycler_food);
        layoutManagerListFood = new LinearLayoutManager(FoodListActivity.this);
        recyclerViewListFood.setLayoutManager(layoutManagerListFood);

        if (getIntent() != null){ menuId = getIntent().getStringExtra("IdCategory");}
        assert menuId != null;
        if (!menuId.isEmpty() && menuId != null){
            loadListFood(menuId);
        }

        fapCart = findViewById(R.id.fabFood);
        fapCart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDialogFood();
            }
        });
    }

    @Override
    public boolean onContextItemSelected(@NonNull MenuItem item) {
        if (item.getTitle().equals(Common.UPDATE)){
            showUpdateDialog(adapterFoodList.getRef(item.getOrder()).getKey(),adapterFoodList.getItem(item.getOrder()));
        } else  if (item.getTitle().equals(Common.DELETE)){
            showDeleteDialog(adapterFoodList.getRef(item.getOrder()).getKey());
        }
        return super.onContextItemSelected(item);
    }

    private void showDeleteDialog(String key) {
        foodList.child(key).removeValue();
        Toast.makeText(this, "Item Deleted", Toast.LENGTH_SHORT).show();
    }

    private void showUpdateDialog(final String key, final Food item) {
        final AlertDialog.Builder alrtAddCate = new AlertDialog.Builder(FoodListActivity.this);
        alrtAddCate.setTitle("Edit food");
        alrtAddCate.setMessage("Please fill full information");
        LayoutInflater inflater = LayoutInflater.from(this);
        View addMenuLayout = inflater.inflate(R.layout.layout_add_new_food,null, false);
        edtNamenewFood = addMenuLayout.findViewById(R.id.edtNamenewFood);
        edtDescriptionFood = addMenuLayout.findViewById(R.id.edtDescriptionnewFood);
        edtPricenewFood = addMenuLayout.findViewById(R.id.edtPricenewFood);
        btnSelect = addMenuLayout.findViewById(R.id.btnSelectFood);
        btnUpload = addMenuLayout.findViewById(R.id.btnUploadFood);

        edtNamenewFood.setText(item.getName());
        edtPricenewFood.setText(item.getPrice());
        edtDescriptionFood.setText(item.getDescription());

        alrtAddCate.setView(addMenuLayout);
        alrtAddCate.setIcon(R.drawable.ic_cart);

        btnUpload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                changeImage(item);
            }
        });

        btnSelect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                chooseImage();
            }
        });

        //setButton
        alrtAddCate.setPositiveButton("YES", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                    item.setName(edtNamenewFood.getText().toString());
                    item.setDescription(edtDescriptionFood.getText().toString());
                    item.setPrice(edtPricenewFood.getText().toString());
                    foodList.child(key).setValue(item);

                    Snackbar.make(root, "New Food "+ newFood.getName()+" was added",Snackbar.LENGTH_SHORT).show();
            }
        });

        alrtAddCate.setNegativeButton("NO", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        alrtAddCate.show();
    }

    private void changeImage(final Food item) {
        if (saveUri != null){
            final ProgressDialog progressDialog = new ProgressDialog(this);
            progressDialog.setMessage("Uploading....");
            progressDialog.show();

            String imgName = UUID.randomUUID().toString();
            final StorageReference imgFolder = storageReference.child("images/"+imgName);
            imgFolder.putFile(saveUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    progressDialog.dismiss();
                    Toast.makeText(FoodListActivity.this, "Uploaded", Toast.LENGTH_SHORT).show();
                    imgFolder.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                        @Override
                        public void onSuccess(Uri uri) {
                            item.setImage(uri.toString());
                        }
                    });
                }
            })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            progressDialog.dismiss();
                            Toast.makeText(FoodListActivity.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    })
                    .addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onProgress(@NonNull UploadTask.TaskSnapshot snapshot) {
                            double progess = (100 * snapshot.getBytesTransferred() / snapshot.getTotalByteCount());
                            progressDialog.setMessage("Uploaded "+ progess);
                        }
                    });
        }
    }


    private void showDialogFood() {
        final AlertDialog.Builder alrtAddCate = new AlertDialog.Builder(FoodListActivity.this);
        alrtAddCate.setTitle("Add new Food");
        alrtAddCate.setMessage("Please fill full information");
        LayoutInflater inflater = LayoutInflater.from(this);
        View addMenuLayout = inflater.inflate(R.layout.layout_add_new_food,null, false);
        edtNamenewFood = addMenuLayout.findViewById(R.id.edtNamenewFood);
        edtDescriptionFood = addMenuLayout.findViewById(R.id.edtDescriptionnewFood);
        edtPricenewFood = addMenuLayout.findViewById(R.id.edtPricenewFood);
        btnSelect = addMenuLayout.findViewById(R.id.btnSelectFood);
        btnUpload = addMenuLayout.findViewById(R.id.btnUploadFood);

        alrtAddCate.setView(addMenuLayout);
        alrtAddCate.setIcon(R.drawable.ic_cart);

        btnUpload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                uploadImage();
            }
        });

        btnSelect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                chooseImage();
            }
        });

        //setButton
        alrtAddCate.setPositiveButton("YES", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();

                if (newFood != null){
                    foodList.push().setValue(newFood);
                    Snackbar.make(root, "New Food "+ newFood.getName()+" was added",Snackbar.LENGTH_SHORT).show();
                }
            }
        });

        alrtAddCate.setNegativeButton("NO", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        alrtAddCate.show();
    }

    private void chooseImage() {
        Intent i = new Intent();
        i.setType("image/*");
        i.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(i,"Select picture"), PICK_IMAGE_REQUEST);
    }

    private void uploadImage() {
        if (saveUri != null){
            final ProgressDialog progressDialog = new ProgressDialog(this);
            progressDialog.setMessage("Uploading....");
            progressDialog.show();

            String imgName = UUID.randomUUID().toString();
            final StorageReference imgFolder = storageReference.child("images/"+imgName);
            imgFolder.putFile(saveUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    progressDialog.dismiss();
                    Toast.makeText(FoodListActivity.this, "Uploaded", Toast.LENGTH_SHORT).show();
                    imgFolder.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                        @Override
                        public void onSuccess(Uri uri) {
                            newFood = new Food();
                            newFood.setDescription(edtDescriptionFood.getText().toString());
                            newFood.setImage(uri.toString());
                            newFood.setMenuId(menuId);
                            newFood.setName(edtNamenewFood.getText().toString());
                            newFood.setPrice(edtPricenewFood.getText().toString());
                        }
                    });
                }
            })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            progressDialog.dismiss();
                            Toast.makeText(FoodListActivity.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    })
                    .addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onProgress(@NonNull UploadTask.TaskSnapshot snapshot) {
                            double progess = (100. * snapshot.getBytesTransferred() / snapshot.getTotalByteCount());
                            progressDialog.setMessage("Uploaded "+ progess);
                        }
                    });
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null){
            saveUri = data.getData();
            btnSelect.setText("Image selected!!");
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (adapterFoodList != null) {adapterFoodList.startListening();}
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (adapterFoodList != null) {adapterFoodList.startListening();}
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (adapterFoodList != null) {adapterFoodList.stopListening();}
    }

    private void loadListFood(String menuId) {
        Query query = FirebaseDatabase.getInstance().getReference().child("foods").orderByChild("menuId").equalTo(menuId);
        FirebaseRecyclerOptions<Food> options = new FirebaseRecyclerOptions.Builder<Food>()
                .setQuery(query, Food.class)
                .build();
        adapterFoodList = new FirebaseRecyclerAdapter<Food, FoodViewHolder>(options) {
            @NonNull
            @Override
            public FoodViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(FoodListActivity.this).inflate(R.layout.layout_list_food,parent, false);
                return new FoodViewHolder(view);
            }

            @Override
            protected void onBindViewHolder(@NonNull FoodViewHolder holder, int position, @NonNull Food model) {
                Picasso.with(FoodListActivity.this).load(model.getImage()).centerCrop().fit().into(holder.imgFood);
                holder.nameFood.setText(model.getName());
                final Food food = model;
                holder.setItemClickListener(new ItemClickListener() {
                    @Override
                    public void onClick(View view, int position, boolean longClick) {
                        String idFood = adapterFoodList.getRef(position).getKey();
                       // Intent foodDetails = new Intent(FoodListActivity.this, FoodDetailsActivity.class);
                       // foodDetails.putExtra("FoodId", idFood);
                       // startActivity(foodDetails);
                    }
                });
            }


        };
        recyclerViewListFood.setAdapter(adapterFoodList);
    }
}
