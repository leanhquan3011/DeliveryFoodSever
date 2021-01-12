package com.leanhquan.deliveryfoodserver;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.Menu;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.andremion.counterfab.CounterFab;
import com.bumptech.glide.Glide;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.leanhquan.deliveryfoodserver.Common.Common;
import com.leanhquan.deliveryfoodserver.Inteface.ItemClickListener;
import com.leanhquan.deliveryfoodserver.Model.Category;
import com.leanhquan.deliveryfoodserver.Service.OrderListenerService;
import com.leanhquan.deliveryfoodserver.ViewHolder.MenuViewHolder;
import com.rengwuxian.materialedittext.MaterialEditText;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AlertDialog;
import androidx.core.view.GravityCompat;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.UUID;

public class HomeActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    private final int PICK_IMAGE_REQUEST = 71;
    private final static String TAG = "TAG";
    private DrawerLayout drawer;
    private NavigationView navigationView;
    private Toolbar toolbar;
    private RecyclerView recyclerMenu;
    private TextView txtFullname;
    private CounterFab fapCart;
    private FirebaseStorage storage;
    private FirebaseDatabase database;
    private StorageReference storageReference;
    private DatabaseReference categories;
    private MaterialEditText edtNamenewCate;
    private Button btnSelect, btnUpload;
    private Category newCategory;
    private Uri saveUri;

    private FirebaseRecyclerAdapter<Category, MenuViewHolder> adapterCategorylist;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        init();

        storage = FirebaseStorage.getInstance();
        storageReference = storage.getReference();

        database = FirebaseDatabase.getInstance();
        categories = database.getReference("categories");

        Intent service = new Intent(this, OrderListenerService.class);
        startService(service);

        recyclerMenu = findViewById(R.id.recycler_menu);
        RecyclerView.LayoutManager layoutManagerListCategory = new LinearLayoutManager(this);
        recyclerMenu.setLayoutManager(layoutManagerListCategory);

        loadListCategory();


        fapCart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDialogCart();
            }
        });


        ActionBarDrawerToggle actionBarDrawerToggle = new ActionBarDrawerToggle(
                this, drawer, toolbar,
                R.string.navigation_drawer_opne,
                R.string.navigation_drawer_close);
        drawer.setDrawerListener(actionBarDrawerToggle);
        actionBarDrawerToggle.syncState();
        navigationView.setNavigationItemSelectedListener(this);


        View HeaderView = navigationView.getHeaderView(0);
        txtFullname = HeaderView.findViewById(R.id.txtFullName);
        txtFullname.setText(Common.currentUser.getName());


    }

    @Override
    public boolean onContextItemSelected(@NonNull MenuItem item) {
        if (item.getTitle().equals(Common.UPDATE)) {
            showUpdateDialog(adapterCategorylist.getRef(item.getOrder()).getKey(), adapterCategorylist.getItem(item.getOrder()));
        } else if (item.getTitle().equals(Common.DELETE)) {
            showDeleteDialog(adapterCategorylist.getRef(item.getOrder()).getKey());
        }
        return super.onContextItemSelected(item);
    }

    private void showDeleteDialog(String key) {
        DatabaseReference food = database.getReference("foods");
        Query foodinCategory = food.orderByChild("menuId").equalTo(key);
        foodinCategory.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                snapshot.getRef().removeValue();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        categories.child(key).removeValue();
        Toast.makeText(this, "Item Deleted", Toast.LENGTH_SHORT).show();
    }

    private void showUpdateDialog(final String key, final Category item) {
        final AlertDialog.Builder alrtAddCate = new AlertDialog.Builder(HomeActivity.this);
        alrtAddCate.setTitle("Update category");
        alrtAddCate.setMessage("Please fill full information");
        LayoutInflater inflater = LayoutInflater.from(this);
        View addMenuLayout = inflater.inflate(R.layout.layout_add_new_category, null, false);
        edtNamenewCate = addMenuLayout.findViewById(R.id.edtNamenewCate);
        btnSelect = addMenuLayout.findViewById(R.id.btnSelect);
        btnUpload = addMenuLayout.findViewById(R.id.btnUpload);

        edtNamenewCate.setText(item.getName());

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
                item.setName(edtNamenewCate.getText().toString());
                categories.child(key).setValue(item);
                Toast.makeText(HomeActivity.this, "Item has been updated", Toast.LENGTH_SHORT).show();
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

    private void changeImage(final Category item) {
        if (saveUri != null) {
            final ProgressDialog progressDialog = new ProgressDialog(this);
            progressDialog.setMessage("Uploading....");
            progressDialog.show();

            String imgName = UUID.randomUUID().toString();
            final StorageReference imgFolder = storageReference.child("images/" + imgName);
            imgFolder.putFile(saveUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    progressDialog.dismiss();
                    Toast.makeText(HomeActivity.this, "Uploaded", Toast.LENGTH_SHORT).show();
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
                            Toast.makeText(HomeActivity.this, "" + e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    })
                    .addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onProgress(@NonNull UploadTask.TaskSnapshot snapshot) {
                            double progess = (100. * snapshot.getBytesTransferred() / snapshot.getTotalByteCount());
                            progressDialog.setMessage("Uploaded " + progess);
                        }
                    });
        }
    }

    private void showDialogCart() {
        final AlertDialog.Builder alrtAddCate = new AlertDialog.Builder(HomeActivity.this);
        alrtAddCate.setTitle("Add new category");
        alrtAddCate.setMessage("Please fill full information");
        LayoutInflater inflater = LayoutInflater.from(this);
        View addMenuLayout = inflater.inflate(R.layout.layout_add_new_category, null, false);
        edtNamenewCate = addMenuLayout.findViewById(R.id.edtNamenewCate);
        btnSelect = addMenuLayout.findViewById(R.id.btnSelect);
        btnUpload = addMenuLayout.findViewById(R.id.btnUpload);

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

                if (newCategory != null) {
                    categories.push().setValue(newCategory);
                    Snackbar.make(drawer, "New category " + newCategory.getName() + " was added", Snackbar.LENGTH_SHORT).show();
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

    private void uploadImage() {
        if (saveUri != null) {
            final ProgressDialog progressDialog = new ProgressDialog(this);
            progressDialog.setMessage("Uploading....");
            progressDialog.show();

            String imgName = UUID.randomUUID().toString();
            final StorageReference imgFolder = storageReference.child("images/" + imgName);
            imgFolder.putFile(saveUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    progressDialog.dismiss();
                    Toast.makeText(HomeActivity.this, "Uploaded", Toast.LENGTH_SHORT).show();
                    imgFolder.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                        @Override
                        public void onSuccess(Uri uri) {
                            newCategory = new Category(edtNamenewCate.getText().toString(), uri.toString());
                        }
                    });
                }
            })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            progressDialog.dismiss();
                            Toast.makeText(HomeActivity.this, "" + e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    })
                    .addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onProgress(@NonNull UploadTask.TaskSnapshot snapshot) {
                            double progess = (100. * snapshot.getBytesTransferred() / snapshot.getTotalByteCount());
                            progressDialog.setMessage("Uploaded " + progess);
                        }
                    });
        }
    }

    private void chooseImage() {
        Intent i = new Intent();
        i.setType("image/*");
        i.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(i, "Select picture"), PICK_IMAGE_REQUEST);
    }

    public void onBackPressed() {
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            saveUri = data.getData();
            btnSelect.setText("Image selected!!");
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (adapterCategorylist != null) {
            adapterCategorylist.stopListening();
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (adapterCategorylist != null) {
            adapterCategorylist.startListening();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (adapterCategorylist != null) {
            adapterCategorylist.startListening();
        }
    }

    private void loadListCategory() {
        Query query = FirebaseDatabase.getInstance().getReference().child("categories");
        FirebaseRecyclerOptions<Category> options = new FirebaseRecyclerOptions.Builder<Category>()
                .setQuery(query, Category.class)
                .build();
        adapterCategorylist = new FirebaseRecyclerAdapter<Category, MenuViewHolder>(options) {
            @Override
            protected void onBindViewHolder(@NonNull MenuViewHolder holder, int position, @NonNull Category model) {
                Log.d(TAG, "onBindViewHolder: " + model.getName() + model.getImage());
                Glide
                        .with(HomeActivity.this)
                        .load(model.getImage())
                        .centerCrop()
                        .into(holder.imgCate);
                holder.nameCate.setText(model.getName());
                final Category clickItem = model;
                holder.setItemClickListener(new ItemClickListener() {
                    @Override
                    public void onClick(View view, int position, boolean longClick) {
                        Toast.makeText(HomeActivity.this, "Go to deltais of" + clickItem.getName(), Toast.LENGTH_SHORT).show();
                        String id = adapterCategorylist.getRef(position).getKey();
                        Intent idCategory = new Intent(HomeActivity.this, FoodListActivity.class);
                        idCategory.putExtra("IdCategory", id);
                        startActivity(idCategory);
                    }
                });
            }

            @NonNull
            @Override
            public MenuViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(HomeActivity.this).inflate(R.layout.layout_list_category, parent, false);
                return new MenuViewHolder(view);
            }
        };
        recyclerMenu.setAdapter(adapterCategorylist);
    }

    private void showSignOutDialog() {
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(HomeActivity.this);
        alertDialog.setTitle("Đăng xuất");
        alertDialog.setIcon(getDrawable(R.drawable.ic_log_out));
        alertDialog.setMessage("Bạn có chắc chắn muốn đăng xuất ?");
        LayoutInflater inflater = LayoutInflater.from(this);
        alertDialog.setNegativeButton("Hủy bỏ", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
            }
        });
        alertDialog.setPositiveButton("Đồng ý", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                FirebaseAuth.getInstance().signOut();
                Intent signIn = new Intent(HomeActivity.this, LoginActivity.class);
                signIn.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(signIn);
            }
        });
        alertDialog.show();
    }

    private void init() {
        drawer = findViewById(R.id.drawer_layout);
        navigationView = findViewById(R.id.nav_view);
        toolbar = findViewById(R.id.toolbar);
        fapCart = findViewById(R.id.fab);
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.page_menu) {
            // Handle the camera action
            Toast.makeText(this, "Go to menu", Toast.LENGTH_SHORT).show();
        } else if (id == R.id.page_historycart) {
            Toast.makeText(this, "Go to history", Toast.LENGTH_SHORT).show();
            Intent OrderIntent = new Intent(HomeActivity.this, OrderActivity.class);
            startActivity(OrderIntent);
        } else if (id == R.id.page_logout) {
            showSignOutDialog();
        }
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }
}
