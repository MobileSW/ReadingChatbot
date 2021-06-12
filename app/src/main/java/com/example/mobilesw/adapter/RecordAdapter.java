package com.example.mobilesw.adapter;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.model.Model;
import com.example.mobilesw.R;
import com.example.mobilesw.activity.RecordListActivity;
import com.example.mobilesw.activity.RecordActivity;
import com.example.mobilesw.info.RecordInfo;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;

public class RecordAdapter extends RecyclerView.Adapter<RecordAdapter.MyViewHolder> {
    private RecordListActivity activity;
    private List<RecordInfo> mList;
    private FirebaseFirestore db = FirebaseFirestore.getInstance();

    public RecordAdapter(RecordListActivity activity , List<RecordInfo> mList){
        this.activity = activity;
        this.mList = mList;
    }

    public void updateData(int position){
        RecordInfo item = mList.get(position);
        Bundle bundle = new Bundle();
        bundle.putString("title" , item.getTitle());
        bundle.putString("description" , item.getDescription());
        bundle.putString("readtime" , item.getReadtime());
        bundle.putStringArrayList("contents" , item.getContents());
        bundle.putString("publisher" , item.getPublisher());
        Intent intent = new Intent(activity , RecordListActivity.class);
        intent.putExtras(bundle);
        activity.startActivity(intent);
    }

    public void deleteData(int position){
        RecordInfo item = mList.get(position);
        db.collection("records").document(item.getPublisher()).delete()
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()){
                            notifyRemoved(position);
                            Toast.makeText(activity, "Data Deleted !!", Toast.LENGTH_SHORT).show();
                        }else{
                            Toast.makeText(activity, "Error" + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    private void notifyRemoved(int position){
        mList.remove(position);
        notifyItemRemoved(position);
        activity.showData();
    }
    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_record ,parent ,false);
        return new MyViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        ArrayList<String> content = mList.get(position).getContents();
        holder.readtime.setText(mList.get(position).getReadtime());
        holder.title.setText(mList.get(position).getTitle());
        holder.description.setText(mList.get(position).getDescription());
        if(!content.equals("")){
            Glide.with(holder.itemView).load(content).into(holder.contents);
        }

    }

    @Override
    public int getItemCount() {
        return mList.size();
    }

    public static class MyViewHolder extends RecyclerView.ViewHolder{
        TextView title;
        TextView readtime;
        TextView publisher;
        TextView description;
        ImageView contents;
        public MyViewHolder(@NonNull View itemView) {
            super(itemView);

            title = (TextView)itemView.findViewById(R.id.title_list);
            readtime = (TextView)itemView.findViewById(R.id.readtime_list);
            contents= (ImageView)itemView.findViewById(R.id.contents_list);
            description = (TextView)itemView.findViewById(R.id.description_list);
        }
    }
}