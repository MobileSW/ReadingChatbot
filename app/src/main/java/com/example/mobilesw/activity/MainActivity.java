package com.example.mobilesw.activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.Toast;

import com.example.mobilesw.R;
import com.example.mobilesw.fragment.FragCalendar;
import com.example.mobilesw.fragment.FragBoard;
import com.example.mobilesw.fragment.FragHome;
import com.example.mobilesw.fragment.FragMyLibrary;
import com.example.mobilesw.fragment.FragSearch;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

public class MainActivity extends AppCompatActivity {
    private Button profileBtn;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    private BottomNavigationView bottomNavigationView;
    private FragmentManager fm;
    private FragmentTransaction ft;
    private Fragment fragment_ac;
    String userName;
    String userId;

    private boolean fr_check = false;
    private boolean isRandomChat,isBookReport;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.frag_default);

        Intent intent = getIntent();
        isRandomChat = intent.getBooleanExtra("isRandomChat",false);
        isBookReport = intent.getBooleanExtra("isBookReport",false);

        // Fragment 전환
        FragHome fragHome = new FragHome();

        if(isRandomChat){
            String bookT = intent.getStringExtra("book_title");
            Bundle bundle = new Bundle();
            bundle.putBoolean("isRandomChat",true);
            bundle.putString("book_title",bookT);
            fragHome.setArguments(bundle);
        }
        if(isBookReport){
            String bookT = intent.getStringExtra("book_title");
            Bundle bundle = new Bundle();
            bundle.putBoolean("isBookReport",true);
            bundle.putString("book_title",bookT);
            fragHome.setArguments(bundle);
        }

        getSupportFragmentManager().beginTransaction()
                .replace(R.id.main_frame, fragHome)
                .commit();
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
        bottomNavigationView = findViewById(R.id.bottomNavi);
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
                        return true;
                    case R.id.menu_search:
                        FragSearch fragSearch = new FragSearch();
                        getSupportFragmentManager().beginTransaction()
                                .replace(R.id.main_frame, fragSearch)
                                .commit(); 
                        return true;
                    case R.id.menu_board:
                        FragBoard fragBoard = new FragBoard();
                        getSupportFragmentManager().beginTransaction()
                                .replace(R.id.main_frame, fragBoard)
                                .commit();
                        return true;

                    case R.id.menu_calendar:
                        FragCalendar fragCalendar = new FragCalendar();
                        getSupportFragmentManager().beginTransaction()
                                .replace(R.id.main_frame, fragCalendar)
                                .commit();
                        return true;
                    case R.id.menu_library:
                        FragMyLibrary fragMyLibrary = new FragMyLibrary();
                        getSupportFragmentManager().beginTransaction()
                                .replace(R.id.main_frame, fragMyLibrary)
                                .commit();
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
                signOut();
                finish();
                return true;

            case R.id.memberInfo:
                myStartActivity(ProfileActivity.class);
                return true;
        }

        return super.onOptionsItemSelected(item);
    }


    // 로그아웃 함수
    private void signOut() {
        FirebaseAuth.getInstance().signOut();
    }

    // 회원 탈퇴 함수
    private void revokeAccess() {
        db = FirebaseFirestore.getInstance();

        // 인증제거
        mAuth.getCurrentUser().delete();

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

        Toast.makeText(MainActivity.this, "회원탈퇴를 완료했습니다.", Toast.LENGTH_SHORT).show();
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