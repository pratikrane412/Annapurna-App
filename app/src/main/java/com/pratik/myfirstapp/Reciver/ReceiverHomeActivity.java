package com.alok.myfirstapp.Reciver;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.messaging.FirebaseMessaging;
import com.alok.myfirstapp.Constants;
import com.alok.myfirstapp.LoginActivity;
import com.alok.myfirstapp.R;
import com.alok.myfirstapp.RequestFoodModel;
import com.alok.myfirstapp.SplashActivity;

import org.jetbrains.annotations.NotNull;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class ReceiverHomeActivity extends AppCompatActivity {

    EditText title, message, token;
    Button alldeviceid, btnaddress;
    FirebaseAuth firebaseAuth;
    SharedPreferences user_session;
    ImageView locationimg;
    TextView friendsId;
    FirebaseAuth mAuth;
    FirebaseFirestore firestore;
    FirebaseDatabase database;
    DatabaseReference databaseReference;
    RecyclerView recycleview;
    TextView titleTv;
    FusedLocationProviderClient fusedLocationProviderClient;

    ArrayList<RequestFoodModel> requestFoodModelArrayList;
    RequestFoodAdapter requestFoodAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_receiver_home);

        FirebaseMessaging.getInstance().subscribeToTopic("all");
        firestore = FirebaseFirestore.getInstance();
        firebaseAuth = FirebaseAuth.getInstance();
        database = FirebaseDatabase.getInstance();
        databaseReference = database.getReference("RequestedFoods");

        title = findViewById(R.id.titileId);
        message = findViewById(R.id.messageId);
        recycleview = findViewById(R.id.recycleview);
        titleTv = findViewById(R.id.titleTv);
        //  token = findViewById(R.id.tokenId);
        alldeviceid = findViewById(R.id.alldeviceId);
        // btnaddress = findViewById(R.id.btnaddress);
        locationimg = findViewById(R.id.locationimg);

        checkStatus();
        loadRequestedFoods();


        //  friendsId = findViewById(R.id.friendsId);

        ///Initialize fusedLocationProviderClient

        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);

        locationimg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //check permission

                if (ActivityCompat.checkSelfPermission(ReceiverHomeActivity.this
                        , Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {

                    //when permission granted
                    getLocation();

                } else {
                    //when permission denied
                    ActivityCompat.requestPermissions(ReceiverHomeActivity.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 44);
                }
            }

        });


        alldeviceid.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                checkStatus();

                if (status.equals("accept")){

                    if (!title.getText().toString().isEmpty() && !message.getText().toString().isEmpty()) {

                        String titles = title.getText().toString();
                        String address = message.getText().toString();
                        String uid = firebaseAuth.getUid();

                        prepareNotificationMessage(titles, address, uid);

//                    FcmNotificationsSender notificationsSender = new FcmNotificationsSender("/topics/all", title.getText().toString(),
//                            message.getText().toString(), getApplicationContext(), ReceiverHomeActivity.this);
//                    notificationsSender.SendNotifications();


                    } else {
                        Toast.makeText(getApplicationContext(), "Write Your Address", Toast.LENGTH_SHORT).show();
                    }
                }
                else {
                    FirebaseAuth.getInstance().signOut();
                    startActivity(new Intent(ReceiverHomeActivity.this, SplashActivity.class));
                    finish();
                }

            }
        });

    }

    private void loadRequestedFoods() {

        requestFoodModelArrayList = new ArrayList<>();

        databaseReference.orderByChild("uid").equalTo(firebaseAuth.getUid()).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                requestFoodModelArrayList.clear();
                if (snapshot.exists()){
                    titleTv.setVisibility(View.VISIBLE);
                    for (DataSnapshot ds : snapshot.getChildren()){
                        RequestFoodModel requestFoodModel = ds.getValue(RequestFoodModel.class);
                        requestFoodModelArrayList.add(requestFoodModel);
                    }

                    requestFoodAdapter = new RequestFoodAdapter(ReceiverHomeActivity.this,requestFoodModelArrayList,"receiver");
                    recycleview.setAdapter(requestFoodAdapter);
                    requestFoodAdapter.notifyDataSetChanged();
                }
                else {
                    titleTv.setVisibility(View.GONE);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void prepareNotificationMessage(String titles, String address, String uid) {
        String NOTIFICATION_TOPIC = "/topics/" + Constants.FCM_TOPIC;
        String NOTIFICATION_TITLE = fName+"- "+titles;
        String NOTIFICATION_MESSAGE = address;
        String NOTIFICATION_TYPE = "requestingFood";

        //prepare json(what to send where to send)
        JSONObject notificationJo = new JSONObject();
        JSONObject notificationBodyJo = new JSONObject();

        try {
            notificationBodyJo.put("notificationType", NOTIFICATION_TYPE);
            notificationBodyJo.put("userId", uid);
            notificationBodyJo.put("notificationTitle", NOTIFICATION_TITLE);
            notificationBodyJo.put("notificationMessage", NOTIFICATION_MESSAGE);

            //where to send
            notificationJo.put("to", NOTIFICATION_TOPIC); //to all who subscribed to this topic
            notificationJo.put("data", notificationBodyJo);

        } catch (Exception e) {
            Toast.makeText(ReceiverHomeActivity.this, "" + e.getMessage(), Toast.LENGTH_SHORT).show();
        }

        sendFcmNotification(notificationJo, uid);
        savingRequest();

    }

    private void savingRequest() {
        String timetamp = ""+System.currentTimeMillis();
        String titles = title.getText().toString();
        String address = message.getText().toString();
        String uid = firebaseAuth.getUid();

        HashMap<String,Object> hashMap = new HashMap<>();
        hashMap.put("Title",titles);
        hashMap.put("address",address);
        hashMap.put("uid",uid);
        hashMap.put("status","pending");
        hashMap.put("id",timetamp);
        hashMap.put("feedback","pending");
        hashMap.put("contactNo","pending");

        databaseReference.child(timetamp).setValue(hashMap).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void unused) {
                Toast.makeText(ReceiverHomeActivity.this, "done", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void sendFcmNotification(JSONObject notificationJo, String uid) {


        //send volley request
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest("https://fcm.googleapis.com/fcm/send", notificationJo, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                //after sending fcm start  activity

            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {

            }
        }) {
            @Override
            public java.util.Map<String, String> getHeaders() throws AuthFailureError {

                //put required headers
                Map<String, String> headers = new HashMap<>();
                headers.put("Content-Type", "application/json");
                headers.put("Authorization", "key=" + Constants.FCM_KEY);

                return headers;

            }
        };

        //enque the volley request
        Volley.newRequestQueue(ReceiverHomeActivity.this).add(jsonObjectRequest);
    }

    private void getLocation() {

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        fusedLocationProviderClient.getLastLocation().addOnCompleteListener(new OnCompleteListener<Location>() {
            @Override
            public void onComplete(@NonNull @NotNull Task<Location> task) {
                //Intialize Location

                Location location = task.getResult();
                if (location != null) {
                    try {
                        //Intialize geoCoder
                        Geocoder geocoder = new Geocoder(ReceiverHomeActivity.this
                                , Locale.getDefault());

                        //Initialize address list
                        List<Address> address = geocoder.getFromLocation(location.getLatitude(), location.getLongitude(), 1);

                        //set address in messageId
                        message.setText(address.get(0).getAddressLine(0));

                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        });

    }


    public void logout(View view) {
        firebaseAuth.signOut();

        user_session = this.getSharedPreferences("user_details", MODE_PRIVATE);


        //sessions clear when user log out
        SharedPreferences.Editor editor = user_session.edit();
        editor.clear();
        editor.putBoolean("isLoggedIn",false);
        editor.putString("cu_type", "");
        editor.commit();

        Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
        startActivity(intent);
        finish();
    }

    @Override
    protected void onStart() {
        super.onStart();
        checkStatus();
    }

    private String status,fName;
    private void checkStatus() {
        DocumentReference df = firestore.collection("users").document(firebaseAuth.getUid());
        df.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                 status = documentSnapshot.getString("status");
                 fName = documentSnapshot.getString("fName");

                if (status.equals("decline")){
                    FirebaseAuth.getInstance().signOut();
                    startActivity(new Intent(ReceiverHomeActivity.this, SplashActivity.class));
                    finish();
                }
            }
        });
    }
}