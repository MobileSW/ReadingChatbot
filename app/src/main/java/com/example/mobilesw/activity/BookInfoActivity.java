package com.example.mobilesw.activity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.example.mobilesw.R;
import com.example.mobilesw.fragment.FragHome;
import com.example.mobilesw.info.BookInfo;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.GregorianCalendar;
import java.util.HashMap;

public class BookInfoActivity extends AppCompatActivity {

    private BookInfo bookInfo;
    private Button btn_to_library,btn_read_now,btn_book_report,btn_select_book,btn_post_book;
    private HashMap<String,String> book;

    private String bTitle="",bAuthor="",bImg="",bPubdate="",bPublisher="",bDesc ="";

    final FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
    FirebaseFirestore db;
    DocumentReference docRef;

    private boolean isRandomChat,isBookReport,isLibrary,isPost;
    private GregorianCalendar gc;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_book_info);

        btn_to_library= findViewById(R.id.btn_to_library);
        btn_read_now = findViewById(R.id.btn_read_now);
        btn_book_report = findViewById(R.id.btn_book_report);
        btn_select_book = findViewById(R.id.btn_select_book);
        btn_post_book = findViewById(R.id.btn_post_book);

        Intent intent = getIntent();
        bookInfo = (BookInfo)intent.getSerializableExtra("bookInfo");
        isLibrary=intent.getBooleanExtra("isLibrary",false);
        isRandomChat = intent.getBooleanExtra("isRandomChat",false);
        isBookReport = intent.getBooleanExtra("isBookReport",false);
        isPost = intent.getBooleanExtra("isPost",false);
        gc = (GregorianCalendar)intent.getSerializableExtra("date");


        //만약 서재일 경우 -> 버튼 없애기
        LinearLayout ll_btn_library = findViewById(R.id.ll_btn_library);
        if(isLibrary){
            ll_btn_library.setVisibility(ll_btn_library.GONE);
        }
        //만약 랜덤채팅일 경우 -> '바로 읽기' 버튼으로 변경
        if(isRandomChat){
            btn_to_library.setVisibility(btn_to_library.GONE);
            btn_read_now.setVisibility(btn_read_now.VISIBLE);
        }
        // 만약 독후감일 경우 -> '독후감 작성하기' 버튼으로 변경
        if(isBookReport){
            btn_to_library.setVisibility(btn_to_library.GONE);
            btn_book_report.setVisibility(btn_book_report.VISIBLE);
        }
        // 만약 캘린더일 경우 -> 1. 검색 2. 내서재
        if(gc!=null){
            ll_btn_library.setVisibility(ll_btn_library.VISIBLE);
            btn_to_library.setVisibility(btn_to_library.GONE);
            btn_select_book.setVisibility(btn_select_book.VISIBLE);
        }
        // 만약 독후감 글쓰기인 경우
        if(isPost){
            ll_btn_library.setVisibility(ll_btn_library.VISIBLE);
            btn_to_library.setVisibility(btn_to_library.GONE);
            btn_post_book.setVisibility(btn_post_book.VISIBLE);
        }

        bTitle=bookInfo.getTitle();
        bAuthor=bookInfo.getAuthor();
        bImg = bookInfo.getImg();
        bPubdate=bookInfo.getPubdate();
        bPublisher=bookInfo.getPublisher();
        bDesc=bookInfo.getDescription();


        //책 제목 설정
        TextView title = findViewById(R.id.d_book_title);
        title.setText(bTitle);
        //책 작가 설정
        TextView author = findViewById(R.id.d_book_author);
        author.setText(bAuthor);
        //책 설명
        TextView description = findViewById(R.id.d_book_desc);
        bDesc = bDesc.replaceAll("<(/)?([a-zA-Z]*)(\\\\s[a-zA-Z]*=[^>]*)?(\\\\s)*(/)?>","");
        description.setText(bDesc);

        //책 이미지 설정
        ImageView imv = findViewById(R.id.d_book_image);
        String img_url = bImg;
        img_url = img_url.replace("type=m1","type=m140");
        System.out.println("url : "+img_url);
        Glide.with(this).load(img_url).override(200,300).into(imv);

        //내 서재 담기 버튼
        btn_to_library.setOnClickListener(onClickListener);
        btn_read_now.setOnClickListener(onClickListener);
        btn_book_report.setOnClickListener(onClickListener);
        btn_select_book.setOnClickListener(onClickListener);
        btn_post_book.setOnClickListener(onClickListener);
    }
    View.OnClickListener onClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            Intent intent;
            switch(v.getId()){
                case R.id.btn_to_library:
                    Toast.makeText(BookInfoActivity.this, "내 서재에 저장되었습니다.", Toast.LENGTH_SHORT).show();
                    db = FirebaseFirestore.getInstance();
                    docRef = db.collection("users").document(user.getUid());
                    docRef.update("mybook", FieldValue.arrayUnion());


                    docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                            DocumentSnapshot document = task.getResult();
//                        ArrayList<Map<String,String>> books = (ArrayList<Map<String,String>>) document.get("mybook");
                            ArrayList<BookInfo> books = (ArrayList<BookInfo>) document.get("mybook");
                            books.add(bookInfo);
                            System.out.println("책 배열 : "+books);
                            updateDB(books);
                        }
                    });
                    break;
                case R.id.btn_read_now:
                    intent = new Intent(getApplicationContext(), MainActivity.class);
                    intent.addFlags(intent.FLAG_ACTIVITY_CLEAR_TOP);
                    intent.putExtra("isRandomChat",isRandomChat);
                    intent.putExtra("book_title",bTitle);
                    intent.putExtra("book_image",bImg);
                    startActivity(intent);
                    break;
                case R.id.btn_book_report:
                    intent = new Intent(getApplicationContext(), MainActivity.class);
                    intent.addFlags(intent.FLAG_ACTIVITY_CLEAR_TOP);
                    intent.putExtra("isBookReport",isBookReport);
                    intent.putExtra("book_title",bTitle);
                    intent.putExtra("book_image",bImg);
                    startActivity(intent);
                    break;
                case R.id.btn_select_book:
                    intent = new Intent(getApplicationContext(),PostCalendarAvtivity.class);
                    intent.putExtra("date",gc);
                    intent.putExtra("book_info",bookInfo);
                    startActivity(intent);
                    break;
                case R.id.btn_post_book:
                    intent = new Intent(getApplicationContext(),PostActivity.class);
                    intent.putExtra("book_info",bookInfo);
                    startActivity(intent);
                    finish();
                    break;
            }
        }
    };
    private void updateDB(ArrayList<BookInfo> books){
        docRef.update("mybook",books);
    }
}
