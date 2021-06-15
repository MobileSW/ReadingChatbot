package com.example.mobilesw.activity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.bumptech.glide.Glide;
import com.example.mobilesw.R;
import com.example.mobilesw.info.BookInfo;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.HashMap;

public class ShowCalendarPost extends AppCompatActivity {
    private HashMap<String,String> info;

    private LinearLayout layout_cbook;
    private TextView tv_select_date,c_book_title,c_book_author,tv_des_user,tv_des;
    private ImageView c_book_image;
    private int pos;
    private String image;
    SharedPreferences sp;
    FirebaseFirestore db;
    DocumentReference docRef;

    private ArrayList<HashMap<String, String>> booklist;

    private Toolbar toolbar;
    private ActionBar actionBar;
    private TextView toolbar_title;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_cpost);

        final FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        db = FirebaseFirestore.getInstance();
        docRef = db.collection("users").document(user.getUid());

        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        actionBar = getSupportActionBar();
        actionBar.setDisplayShowCustomEnabled(true);
        actionBar.setDisplayShowTitleEnabled(false);


        sp = getApplicationContext().getSharedPreferences("sp", Context.MODE_PRIVATE);
        String str = sp.getString("name", "");
        System.out.println("calendar 에서 user의 이름 : "+str);
        str+="님의 한줄평!";

        Intent intent = getIntent();
        booklist = (ArrayList<HashMap<String, String>>)intent.getSerializableExtra("calbook_list");
        pos = intent.getIntExtra("pos",0);

        info = booklist.get(pos);
        c_book_title = findViewById(R.id.c_book_title);
        c_book_author = findViewById(R.id.c_book_author);
        c_book_image  = findViewById(R.id.c_book_image);
        //tv_select_date = findViewById(R.id.tv_select_date);
        tv_des_user = findViewById(R.id.tv_des_user);
        tv_des = findViewById(R.id.tv_des);

        toolbar_title = findViewById(R.id.toolbar_title);
        toolbar_title.setText(info.get("date"));

        c_book_title.setText(info.get("title"));
        c_book_author.setText(info.get("author"));
        image = info.get("image");
        if(!image.equals("")){
            //Picasso.get().load(image).into(holder.img);
            Glide.with(this).load(image).into(c_book_image);
        }
        //tv_select_date.setText(info.get("date"));
        tv_des.setText(info.get("comment"));
        tv_des_user.setText(str);

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.calendar_menu,menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch(item.getItemId()){
            case R.id.c_modify:
                Intent pIntent= new Intent(getApplicationContext(),PostCalendarAvtivity.class);
                pIntent.putExtra("cBookInfo",info);
                pIntent.putExtra("calbook_list",booklist);
                pIntent.putExtra("pos",pos);
                startActivity(pIntent);
                break;
            case R.id.c_delete:
                Toast.makeText(this,"삭제되었습니다.", Toast.LENGTH_SHORT).show();
                booklist.remove(pos);
                docRef.update("bookCalendar",booklist);
                Intent intent= new Intent(getApplicationContext(),MainActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                //intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
                //intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                intent.putExtra("fragnum",2);
                startActivity(intent);
                break;
        }
        return super.onOptionsItemSelected(item);
    }

}
