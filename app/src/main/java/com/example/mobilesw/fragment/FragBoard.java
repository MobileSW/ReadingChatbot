package com.example.mobilesw.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.mobilesw.R;
import com.example.mobilesw.activity.BlogPost;
import com.example.mobilesw.adapter.BlogRecyclerAdapter;
import com.example.mobilesw.activity.PostActivity;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;


public class FragBoard extends Fragment {
    private RecyclerView  blog_list_view;
    private List<BlogPost> blog_list;

    private ImageButton add_new_post_button;
    private FirebaseFirestore firebaseFirestore;
    private BlogRecyclerAdapter blogRecyclerAdapter;

    public FragBoard(){

    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.frag_board, container, false);

        blog_list = new ArrayList<>();
        blog_list_view = view.findViewById(R.id.blog_list_view);
        add_new_post_button = view.findViewById(R.id.add_new_post_button);

        blogRecyclerAdapter = new BlogRecyclerAdapter(blog_list);

        blog_list_view.setAdapter(blogRecyclerAdapter);
        blog_list_view.setLayoutManager(new LinearLayoutManager(getActivity()));

        firebaseFirestore = FirebaseFirestore.getInstance();



        add_new_post_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(getActivity(), PostActivity.class));
            }
        });

        firebaseFirestore.collection("BookPosts").addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot documentSnapshots, @Nullable FirebaseFirestoreException error) {

                for(DocumentChange doc: documentSnapshots.getDocumentChanges()){

                    if(doc.getType() == DocumentChange.Type.ADDED){

                        BlogPost blogPost =  doc.getDocument().toObject(BlogPost.class);
                        blog_list.add(blogPost);

                        blogRecyclerAdapter.notifyDataSetChanged();
                    }
                }
            }
        });


        return view;
    }

}
