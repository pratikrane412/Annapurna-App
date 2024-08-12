package com.alok.myfirstapp.Admin;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.alok.myfirstapp.R;
import com.alok.myfirstapp.Reciver.RequestFoodAdapter;
import com.alok.myfirstapp.RequestFoodModel;

import java.util.ArrayList;

public class AdminShowFeedBackActivity extends AppCompatActivity {

    ImageButton backBtn;
    RecyclerView recycleview;
    ArrayList<RequestFoodModel> requestFoodModelArrayList;
    RequestFoodAdapter requestFoodAdapter;

    FirebaseDatabase database;
    DatabaseReference databaseReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_show_feed_back);

        database = FirebaseDatabase.getInstance();
        databaseReference = database.getReference("RequestedFoods");

        backBtn = findViewById(R.id.backBtn);
        recycleview = findViewById(R.id.recycleview);

        loadRequestedFoods();

        backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });


    }

    private void loadRequestedFoods() {

        requestFoodModelArrayList = new ArrayList<>();

        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                requestFoodModelArrayList.clear();
                if (snapshot.exists()){
                    for (DataSnapshot ds : snapshot.getChildren()){
                        String feedback = ""+ds.child("feedback").getValue();

                        if (!feedback.equals("pending")){
                            RequestFoodModel requestFoodModel = ds.getValue(RequestFoodModel.class);
                            requestFoodModelArrayList.add(requestFoodModel);
                        }
                    }
                    requestFoodAdapter = new RequestFoodAdapter(AdminShowFeedBackActivity.this,requestFoodModelArrayList,"admin");
                    recycleview.setAdapter(requestFoodAdapter);
                    requestFoodAdapter.notifyDataSetChanged();
                }
                else {
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
}