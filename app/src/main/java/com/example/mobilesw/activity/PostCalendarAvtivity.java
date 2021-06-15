package com.example.mobilesw.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.example.mobilesw.R;
import com.example.mobilesw.info.BookInfo;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.HashMap;

public class PostCalendarAvtivity extends AppCompatActivity {

    private GregorianCalendar gc;

    private TextView tv_select_date,c_book_title,c_book_author;
    private ImageView c_book_image;
    private Button btn_my_library,btn_search_book,btn_post_calendar;
    private EditText edit_post_calendar;
    private BookInfo bookInfo;

    private String selectDate="",bookTitle="",bookAuthor="",bookImage="",bookComment="";

    private LinearLayout layout_buttons, layout_cbook;

    final FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
    FirebaseFirestore db;
    DocumentReference docRef;

    private HashMap<String,String> info;
    private int pos;
    private ArrayList<HashMap<String, String>> booklist;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post_calendar);

        Intent intent = getIntent();
        //select_date = intent.getStringExtra("select_date");
        bookInfo = (BookInfo)intent.getSerializableExtra("book_info");
        gc = (GregorianCalendar)intent.getSerializableExtra("date");

        booklist = (ArrayList<HashMap<String, String>>)intent.getSerializableExtra("calbook_list");
        pos = intent.getIntExtra("pos",0);

        if(booklist!=null){
            info = booklist.get(pos);
        }

        tv_select_date = findViewById(R.id.tv_select_date);
        btn_my_library = findViewById(R.id.btn_my_library);
        btn_search_book = findViewById(R.id.btn_search_book);
        btn_post_calendar = findViewById(R.id.btn_post_calendar);
        edit_post_calendar = findViewById(R.id.edit_post_calendar);
        layout_buttons = findViewById(R.id.layout_twobuttons);
        layout_cbook = findViewById(R.id.layout_cbook);
        c_book_title = findViewById(R.id.c_book_title);
        c_book_author = findViewById(R.id.c_book_author);
        c_book_image  = findViewById(R.id.c_book_image);

        //수정하는 경우
        if(info!=null){
            System.out.println("수정");
            layout_buttons.setVisibility(layout_buttons.GONE);
            layout_cbook.setVisibility(layout_cbook.VISIBLE);
            selectDate = info.get("date");
            bookTitle = info.get("title");
            bookAuthor = info.get("author");
            bookImage = info.get("image");
            bookComment =info.get("comment");
            edit_post_calendar.setText(bookComment);
        }else{
            System.out.println("수정아님");
            SimpleDateFormat fmt = new SimpleDateFormat("yyyy-MM-dd");
            fmt.setCalendar(gc);
            selectDate = fmt.format(gc.getTime());
            if(bookInfo!=null){
                System.out.println("수정아니고 새로 등록");
                layout_buttons.setVisibility(layout_buttons.GONE);
                layout_cbook.setVisibility(layout_cbook.VISIBLE);

                bookTitle = bookInfo.getTitle();
                bookAuthor = bookInfo.getAuthor();
                bookImage = bookInfo.getImg();
            }
        }

        //sd = ""+gc.get(Calendar.YEAR)+"-"+(gc.get(Calendar.MONTH)+1)+"-"+gc.get(Calendar.DAY_OF_MONTH);



        c_book_title.setText(bookTitle);
        c_book_author.setText(bookAuthor);
        tv_select_date.setText(selectDate);
        if(!bookImage.equals("")){
            //Picasso.get().load(image).into(holder.img);
            Glide.with(this).load(bookImage).into(c_book_image);
        }

        btn_my_library.setOnClickListener(onClickListener);
        btn_search_book.setOnClickListener(onClickListener);
        btn_post_calendar.setOnClickListener(onClickListener);

    }

    View.OnClickListener onClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            Intent intent= new Intent(getApplicationContext(),MainActivity.class);;
            switch(v.getId()){
                case R.id.btn_my_library:
                    intent.putExtra("fragnum",4);
                    intent.putExtra("date",gc);
                    startActivity(intent);
                    break;
                case R.id.btn_search_book:
                    intent.putExtra("fragnum",1);
                    intent.putExtra("date",gc);
                    startActivity(intent);
                    break;
                case R.id.btn_post_calendar:
                    if(info==null&&bookInfo==null) {
                        Toast.makeText(PostCalendarAvtivity.this, "책 정보를 가져오세요.", Toast.LENGTH_SHORT).show();
                    }else{
                        db = FirebaseFirestore.getInstance();
                        docRef = db.collection("users").document(user.getUid());
                        docRef.update("bookCalendar", FieldValue.arrayUnion());

                        docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                            @Override
                            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                DocumentSnapshot document = task.getResult();
                                HashMap<String,String> post = new HashMap<String,String>();
                                post.put("title",bookTitle);
                                post.put("author",bookAuthor);
                                post.put("image",bookImage);
                                post.put("comment",edit_post_calendar.getText().toString());
                                post.put("date",selectDate);

                                if(info!=null){
                                    booklist.set(pos,post);
                                    docRef.update("bookCalendar",booklist);
                                }else{
                                    ArrayList<HashMap<String,String>> cPost = (ArrayList<HashMap<String,String>>) document.get("bookCalendar");
                                    cPost.add(post);
                                    updateDB(cPost);
                                }
                                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
                                //intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                intent.putExtra("fragnum",2);
                                startActivity(intent);
                            }
                        });
                    }
                    break;
            }
        }
    };
    private void updateDB(ArrayList<HashMap<String,String>> cPost){
        docRef.update("bookCalendar",cPost);
    }
}
