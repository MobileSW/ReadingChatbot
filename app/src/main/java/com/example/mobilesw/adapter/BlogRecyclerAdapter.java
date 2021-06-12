package com.example.mobilesw.adapter;

import android.content.Context;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.mobilesw.R;
import com.example.mobilesw.info.BlogPost;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Date;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class BlogRecyclerAdapter extends RecyclerView.Adapter<BlogRecyclerAdapter.ViewHolder> {

    public List<BlogPost> blog_list;
    public Context context;

    private FirebaseAuth firebaseAuth;
    private FirebaseFirestore firebaseFirestore;

    public BlogRecyclerAdapter(List<BlogPost> blog_list){

        this.blog_list = blog_list;

    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.blog_list_item, parent, false);

        context = parent.getContext();
        firebaseFirestore = FirebaseFirestore.getInstance();
        firebaseAuth = firebaseAuth.getInstance();
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {


            String blogPostId = blog_list.get(position).BlogPostId;
            String desc_data = blog_list.get(position).getDesc();
            holder.setDescText(desc_data);

            String image_url = blog_list.get(position).getImage_url();
            holder.setBlogImage(image_url);

            String user_id = blog_list.get(position).getUser_id();
            final String currentUserId = firebaseAuth.getCurrentUser().getUid();


            firebaseFirestore.collection("users").document(user_id).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<DocumentSnapshot> task) {

                    if (task.isSuccessful()) {
                        String userName = task.getResult().getString("name");
                        //  String userImage

                        holder.setUserData(userName);
                    } else {

                    }
                }
            });
            //User data


            String dateString = blog_list.get(position).getDate();
            holder.setTime(dateString);
            if (user_id.equals(currentUserId)) {
                holder.blogDeleteBtn.setEnabled(true);
                holder.blogDeleteBtn.setVisibility(View.VISIBLE);
            }

            holder.blogDeleteBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    firebaseFirestore.collection("BookPosts").document(blogPostId).delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {

                            blog_list.remove(position);
                        }
                    });
                }
            });

    }


    @Override
    public int getItemCount() {

        return blog_list.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder{
        private View mView;

        private TextView descView;
        private ImageView blogImageView;
        private TextView blogDate;

        private TextView blogUserName;
       // private CircleImageView blogUserImage;

        private Button blogDeleteBtn;

        public ViewHolder(@NonNull View itemView) {

            super(itemView);
            mView = itemView;

            blogDeleteBtn = mView.findViewById(R.id.blog_delete_btn);
        }

        public void setDescText(String descText){

            descView = mView.findViewById(R.id.blog_desc);
            descView.setText(descText);
        }

        public void setBlogImage(String downloadUri){
            blogImageView = mView.findViewById(R.id.blog_image);
            Glide.with(context).load(downloadUri).into(blogImageView);

        }

        public void setTime(String date){
            blogDate = mView.findViewById(R.id.blog_date);
            blogDate.setText(date);
        }

        public void setUserData(String name){
            blogUserName = mView.findViewById(R.id.blog_user_name);

            blogUserName.setText(name);
        }

    }

}
