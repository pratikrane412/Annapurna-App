package com.alok.myfirstapp.Reciver;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.alok.myfirstapp.R;
import com.alok.myfirstapp.RequestFoodModel;

import java.util.ArrayList;
import java.util.HashMap;

public class RequestFoodAdapter extends RecyclerView.Adapter<RequestFoodAdapter.ViewHolder> {

    private Context context;
    ArrayList<RequestFoodModel> requestFoodModelArrayList;
    String activityStatus;

    public RequestFoodAdapter(Context context, ArrayList<RequestFoodModel> requestFoodModelArrayList, String activityStatus) {
        this.context = context;
        this.requestFoodModelArrayList = requestFoodModelArrayList;
        this.activityStatus = activityStatus;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(context).inflate(R.layout.raw_requeted_foods, parent, false);
        return new RequestFoodAdapter.ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {

        RequestFoodModel requestFoodModel = requestFoodModelArrayList.get(position);

        holder.requestFoodId.setText(requestFoodModel.getId());
        holder.titleTv.setText(requestFoodModel.getTitle());
        holder.uidTv.setText(requestFoodModel.getUid());
        holder.statusTv.setText(requestFoodModel.getStatus());
        holder.contacNotv.setText(requestFoodModel.getContactNo());

        if (activityStatus.equals("admin")) {
            holder.feedbackLL.setVisibility(View.VISIBLE);
            holder.feedbackBtn.setVisibility(View.GONE);
            holder.feedbackTv.setText(requestFoodModel.getFeedback());

        } else if (activityStatus.equals("receiver")) {

            holder.feedbackLL.setVisibility(View.GONE);
            if (requestFoodModel.getStatus().equals("pending")) {
                holder.feedbackBtn.setVisibility(View.VISIBLE);
            } else {
                holder.feedbackBtn.setVisibility(View.GONE);
            }

            holder.feedbackBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    LayoutInflater inflater = LayoutInflater.from(context);
                    View dialogView = inflater.inflate(R.layout.dialog_feedbacks, null);

                    AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(context);
                    dialogBuilder.setView(dialogView);

                    EditText editText = dialogView.findViewById(R.id.edit_text);
                    EditText mobileEt = dialogView.findViewById(R.id.mobileEt);

                    dialogBuilder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            String enteredText = editText.getText().toString();
                            String enteredMobile = mobileEt.getText().toString();

                            if (TextUtils.isEmpty(enteredText)) {
                                editText.setError("feedback required");
                            }
                            else if (TextUtils.isEmpty(enteredMobile)){
                                mobileEt.setError("Mobile required");
                            }
                            else {
                                HashMap<String, Object> hashMap = new HashMap<>();
                                hashMap.put("feedback", enteredText);
                                hashMap.put("status", "completed");
                                hashMap.put("contactNo",enteredMobile);
                                DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("RequestedFoods");
                                databaseReference.child(requestFoodModel.getId()).updateChildren(hashMap).addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void unused) {
                                        Toast.makeText(context, "done", Toast.LENGTH_SHORT).show();
                                        dialog.dismiss();
                                    }
                                });
                            }
                        }
                    });

                    dialogBuilder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    });

                    // Create and show the dialog
                    AlertDialog dialog = dialogBuilder.create();
                    dialog.show();

                }
            });
        }


    }

    @Override
    public int getItemCount() {
        return requestFoodModelArrayList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        TextView requestFoodId, titleTv, uidTv, statusTv,contacNotv,feedbackTv;
        Button feedbackBtn;
        LinearLayout feedbackLL;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            requestFoodId = itemView.findViewById(R.id.requestFoodId);
            titleTv = itemView.findViewById(R.id.titleTv);
            uidTv = itemView.findViewById(R.id.uidTv);
            statusTv = itemView.findViewById(R.id.statusTv);
            feedbackBtn = itemView.findViewById(R.id.feedbackBtn);
            feedbackLL = itemView.findViewById(R.id.feedbackLL);
            feedbackTv = itemView.findViewById(R.id.feedbackTv);
            contacNotv = itemView.findViewById(R.id.contacNotv);
        }
    }
}
