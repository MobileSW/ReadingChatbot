package com.example.mobilesw.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.mobilesw.R;
import com.example.mobilesw.info.BookInfo;

import java.util.ArrayList;

public class BookAdapter extends RecyclerView.Adapter<BookAdapter.MyViewHolder> {

    private Context context;
    private ArrayList<BookInfo> booklist;

    //getItemCount, onCreateViewHolder, MyViewHolder, onBindViewholder 순으로 들어오게 된다.
    // 뷰홀더에서 초기세팅해주고 바인드뷰홀더에서 셋텍스트해주는 값이 최종적으로 화면에 출력되는 값

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_book,parent,false);//뷰 생성(아이템 레이아웃을 기반으로)
        MyViewHolder viewHolder = new MyViewHolder(view);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        BookInfo data = booklist.get(position); //위치에 따라서 그에 맞는 데이터를 얻어오게 한다.
        String image = data.getImg();
        String title = data.getTitle();
        if(title.length()>25){
            title = title.substring(0,25);
            title+="...";
        }
        holder.title.setText(title);
        holder.author.setText(data.getAuthor());
        holder.publisher.setText(data.getPublisher());
        holder.pubdate.setText(data.getPubdate());

        if(!image.equals("")){
            //Picasso.get().load(image).into(holder.img);
            Glide.with(context).load(image).into(holder.img);
        }
    }

    @Override
    public int getItemCount() {
        return booklist.size();
    }

    public BookAdapter(Context context, ArrayList<BookInfo> booklist){
        this.context= context;//보여지는 액티비티
        this.booklist = booklist;//내가 처리하고자 하는 아이템들의 리스트
    }

    public interface OnItemClickListener{
        void onItemClick(View v,int pos);
    }
    private OnItemClickListener mListener = null;
    public void setOnItemClickListener(OnItemClickListener listener){
        this.mListener = listener;
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {
        TextView title;
        TextView author;
        TextView publisher;
        TextView pubdate;
        ImageView img;
        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
            title = (TextView)itemView.findViewById(R.id.book_title);
            author = (TextView)itemView.findViewById(R.id.book_author);
            img = (ImageView)itemView.findViewById(R.id.book_image);
            publisher = (TextView)itemView.findViewById(R.id.book_publisher);
            pubdate = (TextView)itemView.findViewById(R.id.book_pubdate);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int pos = getBindingAdapterPosition();
                    if(pos!= RecyclerView.NO_POSITION){
                        if(mListener!=null){
                            mListener.onItemClick(v,pos);
                        }
                    }
                }
            });        }
    }
}