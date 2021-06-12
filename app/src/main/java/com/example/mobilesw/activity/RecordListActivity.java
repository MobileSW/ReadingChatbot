package com.example.mobilesw.activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.bumptech.glide.load.model.Model;
import com.example.mobilesw.R;
import com.example.mobilesw.adapter.RecordAdapter;
import com.example.mobilesw.info.RecordInfo;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;

import java.util.ArrayList;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

public class RecordListActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private RecyclerView.Adapter adapter;
    private RecyclerView.LayoutManager layoutManager;
    private ArrayList<RecordInfo> arrayList;
    private FirebaseFirestore database;
    private DatabaseReference databaseReference;
    RecordAdapter recordAdapter;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_record_list);

        recyclerView = findViewById(R.id.recyclerView_list);
        recyclerView.setHasFixedSize((true));
        layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);

        arrayList = new ArrayList<>(); //RecordInfo 객체를 담을 어레이 리스트
        database = FirebaseFirestore.getInstance(); //파이어베이스 데이터베이스 연동
        adapter = new RecordAdapter(this, arrayList);
        recyclerView.setAdapter(adapter); //리사이클러뷰에 어댑터 연결

        showData();
    }

    public void showData(){

        database.collection("records").get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        arrayList.clear();
                        for (DocumentSnapshot snapshot : task.getResult()){
                            RecordInfo recordInfo = new RecordInfo(snapshot.getString("title") , snapshot.getString("readtime") ,
                                    snapshot.getString("description"), (ArrayList<String>) snapshot.get("contents"),
                                    snapshot.getString("publisher"), snapshot.getDate("createdAt"), snapshot.getString("dates"));
                            arrayList.add(recordInfo);
                        }
                        adapter.notifyDataSetChanged();
                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(RecordListActivity.this, "Oops ... something went wrong", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
