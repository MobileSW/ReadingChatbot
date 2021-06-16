package com.example.mobilesw.activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.app.AlertDialog;
import android.content.ClipData;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.TextView;

import com.example.mobilesw.R;
import com.example.mobilesw.fragment.FragCalendar;
import com.example.mobilesw.fragment.FragBoard;
import com.example.mobilesw.fragment.FragHome;
import com.example.mobilesw.fragment.FragMyLibrary;
import com.example.mobilesw.fragment.FragSearch;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import static com.example.mobilesw.info.Util.handleDialog;
import static com.example.mobilesw.info.Util.makeDialog;

import java.util.GregorianCalendar;

public class MainActivity extends AppCompatActivity {
    private Button profileBtn;
    private FirebaseAuth mAuth = FirebaseAuth.getInstance();
    FirebaseUser user = mAuth.getCurrentUser();
    private FirebaseFirestore db;

    private Toolbar toolbar;
    private ActionBar actionBar;
    private TextView toolbar_title;
    private BottomNavigationView bottomNavigationView;
    private FragmentManager fm;
    private FragmentTransaction ft;
    private Fragment fragment_ac;
    String userName;
    String userId;

    private boolean fr_check = false;
    private boolean isRandomChat,isBookReport;

    private int fragnum;

    private GregorianCalendar gc;
    private Boolean isPost;

    SharedPreferences sp;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.frag_default);

        sp = getSharedPreferences("sp", Context.MODE_PRIVATE);
        String name = sp.getString("name", "");

        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        actionBar = getSupportActionBar();
        actionBar.setDisplayShowCustomEnabled(true);
        actionBar.setDisplayShowTitleEnabled(false);

        toolbar_title = findViewById(R.id.toolbar_title);
        bottomNavigationView = findViewById(R.id.bottomNavi);

        Intent intent = getIntent();
        isRandomChat = intent.getBooleanExtra("isRandomChat",false);
        isBookReport = intent.getBooleanExtra("isBookReport",false);
        isPost = intent.getBooleanExtra("isPost", false);
        //isCalendar = intent.getBooleanExtra("isCalendar",false);
        gc = (GregorianCalendar)intent.getSerializableExtra("date");

        fragnum = intent.getIntExtra("fragnum",0);

        switch (fragnum){
            case 0:
                FragHome fragHome = new FragHome();
                if(isRandomChat){
                    String bookT = intent.getStringExtra("book_title");
                    String bookI = intent.getStringExtra("book_image");
                    Bundle bundle = new Bundle();
                    bundle.putBoolean("isRandomChat",true);
                    bundle.putString("book_title",bookT);
                    bundle.putString("book_image",bookI);
                    fragHome.setArguments(bundle);
                }
                if(isBookReport){
                    String bookT = intent.getStringExtra("book_title");
                    String bookI = intent.getStringExtra("book_image");
                    Bundle bundle = new Bundle();
                    bundle.putBoolean("isBookReport",true);
                    bundle.putString("book_title",bookT);
                    bundle.putString("book_image",bookI);
                    fragHome.setArguments(bundle);
                }

                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.main_frame, fragHome)
                        .commit();
                toolbar_title.setText("채팅봇");
                break;
            case 1: //독서달력에서 책 검색으로 넘어가야하는 경우
                FragSearch fragSearch = new FragSearch();
                if(gc!=null){
                    System.out.println("gc!=null");
                    Bundle bundle = new Bundle();
                    bundle.putSerializable("date",gc);
                    fragSearch.setArguments(bundle);
                }
                if(isPost) {
                    Bundle bundle = new Bundle();
                    bundle.putBoolean("isPost", true);
                    fragSearch.setArguments(bundle);
                }
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.main_frame, fragSearch)
                        .commit();
                toolbar_title.setText("책 검색");
                break;
            case 2:
                FragCalendar fragCalendar = new FragCalendar();
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.main_frame, fragCalendar)
                        .commit();
                toolbar_title.setText("독서 달력");
                break;
            case 3:
                FragBoard fragBoard = new FragBoard();
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.main_frame, fragBoard)
                        .commit();
                toolbar_title.setText("독후감 갤러리");
                bottomNavigationView.setSelectedItemId(R.id.menu_board);
                break;
            case 4: // 독서달력에서 내서로 넘어가야하는 경우
                FragMyLibrary fragMyLibrary = new FragMyLibrary();
                if(gc!=null){
                    Bundle bundle = new Bundle();
                    bundle.putSerializable("date",gc);
                    fragMyLibrary.setArguments(bundle);
                }
                if(isPost) {
                    Bundle bundle = new Bundle();
                    bundle.putBoolean("isPost", true);
                    fragMyLibrary.setArguments(bundle);
                }
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.main_frame, fragMyLibrary)
                        .commit();
                toolbar_title.setText(name+"님의 서재");
                break;

        }

        fragment_ac = new Fragment();


        /*
        // 유저 이름 찾기, 각 Fragment에 전달하기 위함 (매번 DB에서 검색하면 낭비니까)
        db = FirebaseFirestore.getInstance();
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        userId = user.getUid();

        db.collection("users").document(userId).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {
                        userName = document.get("name").toString();
                        bundle.putString("userId", userId);
                        bundle.putString("userName",userName);
                    }
                }
            }
        });

         */

        // 메뉴 바 아이콘을 눌렀을 때의 화면 동작
        // 각 화면 코드는 fragment 폴더에 있음
        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch (item.getItemId()) {
                    // 홈 화면(약속 목록)으로 이동
                    case R.id.menu_home:
                        FragHome fragHome = new FragHome();
                        getSupportFragmentManager().beginTransaction()
                                .replace(R.id.main_frame, fragHome)
                                .commit();
                        toolbar_title.setText("채팅봇");
                        return true;
                    case R.id.menu_search:
                        FragSearch fragSearch = new FragSearch();
                        getSupportFragmentManager().beginTransaction()
                                .replace(R.id.main_frame, fragSearch)
                                .commit();
                        toolbar_title.setText("책 검색");
                        return true;
                    case R.id.menu_board:
                        FragBoard fragBoard = new FragBoard();
                        getSupportFragmentManager().beginTransaction()
                                .replace(R.id.main_frame, fragBoard)
                                .commit();
                        toolbar_title.setText("독후감 갤러리");
                        return true;

                    case R.id.menu_calendar:
                        FragCalendar fragCalendar = new FragCalendar();
                        getSupportFragmentManager().beginTransaction()
                                .replace(R.id.main_frame, fragCalendar)
                                .commit();
                        toolbar_title.setText("독서 달력");
                        return true;
                    case R.id.menu_library:
                        FragMyLibrary fragMyLibrary = new FragMyLibrary();
                        getSupportFragmentManager().beginTransaction()
                                .replace(R.id.main_frame, fragMyLibrary)
                                .commit();
                        toolbar_title.setText(name+"님의 서재");
                        return true;

                }
                return false;
            }
        });

    }
    //
    //메뉴바 코드
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.user_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.revoke:
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setTitle("회원 탈퇴")        // 제목
                        .setMessage("독서 비서 앱을 정말로 탈퇴하시겠습니까?")
                        .setPositiveButton("확인", new DialogInterface.OnClickListener() {
                            // 확인 버튼 클릭시 설정, 오른쪽 버튼입니다.
                            public void onClick(DialogInterface dialog, int whichButton) {
                                revokeAccess();
                                finish();
                            }
                        })
                        .setNegativeButton("취소", new DialogInterface.OnClickListener() {// 취소 버튼 클릭시
                            public void onClick(DialogInterface dialog, int whichButton) {//취소 이벤트...
                            }
                        });
                AlertDialog dialog = builder.create();    // 알림창 객체 생성
                dialog.show();    // 알림창 띄우기

                return true;

            case R.id.logout:
                mAuth.signOut();
                myStartActivity(LoginActivity.class);
                return true;

            case R.id.memberInfo:
                myStartActivity(ProfileActivity.class);
                return true;
        }

        return super.onOptionsItemSelected(item);
    }


    // 회원 탈퇴 함수
    private void revokeAccess() {
        db = FirebaseFirestore.getInstance();
        String uid = user.getUid();

        // 저장하고 있던 정보들 모두 삭제
        SharedPreferences pref = getSharedPreferences("sp", MODE_PRIVATE);
        SharedPreferences.Editor editor = pref.edit();
        editor.clear();
        editor.commit();

        // DB에 저장된 정보 삭제
        userDelete(uid);
        postDelete("posts","user_id",uid);
        postDelete("records","publisher",uid);
        chatDelete(uid);
        storageDelete(uid);

        mAuth.getCurrentUser().delete();
        mAuth.signOut();

        makeDialog("회원 탈퇴", "탈퇴 처리가 완료 되었습니다." , MainActivity.this);
    }

    private void userDelete(String uid) {
        // user 테이블에서 현재 user uid로 저장된 문서 삭제
        db.collection("users").document(mAuth.getUid())
                .delete()
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.d("revoke User", "DocumentSnapshot successfully deleted!");
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.w("revoke User", "Error deleting document", e);
                    }
                });
    }

    private void postDelete(String collection, String field, String uid) {

        //      현재 user가 만든 독후감 모두 제거
        db.collection(collection).get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            //모든 document 확인
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                // 현재 uid를 가진 문서
                                Object doc = document.getData().get(field);
                                if (doc != null) {
                                    if (doc.toString().contains(uid)){
                                        // db에서 현재 유저 uid 삭제
                                        DocumentReference userdel = db.collection(collection).document(document.getId());
                                        userdel.delete();
                                        finish();
                                        return;
                                    }
                                }
                            }
                        }
                    }
                });
    }

    private void chatDelete(String uid){
        FirebaseDatabase firebaseDatabase= FirebaseDatabase.getInstance();
        DatabaseReference chatRef= firebaseDatabase.getReference("chat").child(uid);
        chatRef.removeValue();
    }

    private void storageDelete(String uid) {
        FirebaseStorage storage = FirebaseStorage.getInstance();
        StorageReference storageRef = storage.getReference();
        final StorageReference mountainImagesRef = storageRef.child("users/" + uid + "/profileImage.jpg");
        mountainImagesRef.delete();
    }

    private void myStartActivity(Class c) {
        Intent intent = new Intent(this, c);
        intent.addFlags(intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
    }

    public void replaceFragment(Fragment fragment){
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.main_frame,fragment).commit();
    }

   /* private void replaceFragment(Fragment fragment){

        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        fragmentTransaction.replace(R.id.main_container, fragment);
        fragmentTransaction.commit();
    } */
}