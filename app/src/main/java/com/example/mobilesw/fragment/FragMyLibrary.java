package com.example.mobilesw.fragment;

import android.app.ActionBar;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.Toolbar;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.mobilesw.R;
import com.example.mobilesw.activity.BookInfoActivity;
import com.example.mobilesw.adapter.LibraryAdapter;
import com.example.mobilesw.info.BookInfo;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.GregorianCalendar;
import java.util.HashMap;

public class FragMyLibrary extends Fragment {
    private ArrayList<BookInfo> books;
    RecyclerView rcv;
    GridLayoutManager glm;

    LibraryAdapter libraryAdapter;

    final FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
    FirebaseFirestore db;
    DocumentReference docRef;

    TextView toolbar_title;

    private GregorianCalendar gc;
    private Boolean isPost;

    SharedPreferences sp;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.frag_mylibrary, container, false);

        sp = getContext().getSharedPreferences("sp", Context.MODE_PRIVATE);
        String name = sp.getString("name", "");
        System.out.println("이름 : "+name);

        if(getArguments()!=null){
            gc = (GregorianCalendar)getArguments().getSerializable("date");
            isPost = getArguments().getBoolean("isPost",false);
        }

        toolbar_title = view.findViewById(R.id.toolbar_title);
        toolbar_title.setText(name+"님의 서재");

        rcv = view.findViewById(R.id.book_list);
        glm = new GridLayoutManager(getContext(),3);
        rcv.setHasFixedSize(true);//각 아이템이 보여지는 것을 일정하게
        rcv.setLayoutManager(glm);//앞서 선언한 리싸이클러뷰를 레이아웃 매니저에 붙힌다.


        db = FirebaseFirestore.getInstance();
        docRef = db.collection("users").document(user.getUid());
        docRef.update("mybook", FieldValue.arrayUnion());


        docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                books = new ArrayList<BookInfo>();
                DocumentSnapshot document = task.getResult();
                ArrayList<HashMap<String,String>> booklist = (ArrayList<HashMap<String, String>>) document.get("mybook");
                for(int i=0;i<booklist.size();i++){
                    HashMap<String,String> hm = booklist.get(i);
                    String title = hm.get("title");
                    String author = hm.get("author");
                    String image = hm.get("img");
                    String publisher = hm.get("publisher");
                    String pubdate = hm.get("pubdate");
                    String description = hm.get("description");
                    description = description.replaceAll("<(/)?([a-zA-Z]*)(\\\\s[a-zA-Z]*=[^>]*)?(\\\\s)*(/)?>","");
                    if(pubdate.length()==8){
                        pubdate = pubdate.substring(0,4)+"-"+pubdate.substring(4,6)+"-"+pubdate.substring(6,8);
                    }
                    BookInfo bookInfo=new BookInfo(title,author,image,publisher,pubdate,description);
                    books.add(bookInfo);
                }
                //books = (ArrayList<BookInfo>) document.get("mybook");
                System.out.println("책 배열 : "+books);
                libraryAdapter=new LibraryAdapter(getContext(),books); //앞서 만든 리스트를 어댑터에 적용시켜 객체를 만든다.
                libraryAdapter.setOnItemClickListener(
                        new LibraryAdapter.OnItemClickListener() {
                            @Override
                            public void onItemClick(View v, int pos) {
                                BookInfo obj = books.get(pos);
                                Intent intent = new Intent(getContext(), BookInfoActivity.class);
                                //목록에서 클릭한 게시물에 대한 내용을 전달
                                intent.putExtra("bookInfo", (Serializable) obj);
                                //내 서재임을 나타낸다.
                                intent.putExtra("isLibrary",true);
                                intent.putExtra("date",gc);
                                intent.putExtra("isPost",isPost);
                                startActivity(intent);
                            }
                        }
                );
                libraryAdapter.setOnItemLongClickListener(new LibraryAdapter.OnItemLongClickListener() {
                    @Override
                    public void onItemLongClick(View v, int pos) {
                        PopupMenu popupMenu = new PopupMenu(getContext(),v);
                        getActivity().getMenuInflater().inflate(R.menu.library_book_menu,popupMenu.getMenu());
                        popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                            @Override
                            public boolean onMenuItemClick(MenuItem item) {
                                switch (item.getItemId()){
                                    case R.id.book_delete:
                                        Toast.makeText(getContext(),"삭제되었습니다.",Toast.LENGTH_SHORT).show();
                                        booklist.remove(pos);
                                        docRef.update("mybook",booklist);
                                        return true;
                                }
                                return false;
                            }
                        });
                        popupMenu.show();
                    }
                });
                rcv.setAdapter(libraryAdapter);
            }
        });

        return view;
    }
}
