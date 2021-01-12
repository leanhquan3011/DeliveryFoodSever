package com.leanhquan.deliveryfoodserver;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.leanhquan.deliveryfoodserver.Common.Common;
import com.leanhquan.deliveryfoodserver.Model.User;

public class LoginActivity extends AppCompatActivity {

    private DatabaseReference          userDB;
    private EditText                   edtPhonenumber, edtPassword;
    private Button                     btnLogin;
    private TextView                   txtFogotPass;
    private FirebaseDatabase           database;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        init();
        
        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String phone = edtPhonenumber.getText().toString();
                String pass = edtPassword.getText().toString();
                login(phone, pass);
            }
        });
    }

    private void login(String phone, final String pass) {
        database = FirebaseDatabase.getInstance();
        userDB = database.getReference("user");

        final ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Please waiting ...");
        progressDialog.show();

        final String localPhone = phone;
        final String localPass = pass;

        userDB.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.child(localPhone).exists()){
                    progressDialog.dismiss();
                    User user = snapshot.child(localPhone).getValue(User.class);
                    assert user != null;
                    user.setPhone(localPhone);
                    if (Boolean.parseBoolean(user.getIsStaff())) {
                        if (user.getPassword().equals(pass)) {
                            edtPhonenumber.setError(null);
                            Intent intent = new Intent(LoginActivity.this, HomeActivity.class);
                            Common.currentUser = user;
                            startActivity(intent);
                            finish();
                        }else {
                            progressDialog.dismiss();
                            edtPassword.setError("Wrong Password");
                            edtPassword.requestFocus();
                        }
                    } else {
                        progressDialog.dismiss();
                        Toast.makeText(LoginActivity.this, "Please login with staff account", Toast.LENGTH_SHORT).show();
                    }
                }else {
                    progressDialog.dismiss();
                    Toast.makeText(LoginActivity.this, "User does not exits", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }


    private void init() {
        edtPassword = findViewById(R.id.edtPassword);
        edtPhonenumber = findViewById(R.id.edtPhonenumber);
        btnLogin = findViewById(R.id.btnLogin);
        txtFogotPass = findViewById(R.id.txtFogotPass);
    }
}
