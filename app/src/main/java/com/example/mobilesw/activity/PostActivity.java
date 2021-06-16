package com.example.mobilesw.activity;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.bumptech.glide.Glide;
import com.example.mobilesw.fragment.FragSearch;
import com.example.mobilesw.info.BookInfo;
import com.example.mobilesw.info.PostInfo;
import com.example.mobilesw.R;
import com.example.mobilesw.view.ContentsItemView;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageMetadata;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Date;

import static com.example.mobilesw.info.Util.GALLERY_IMAGE;
import static com.example.mobilesw.info.Util.INTENT_MEDIA;
import static com.example.mobilesw.info.Util.INTENT_PATH;
import static com.example.mobilesw.info.Util.isStorageUrl;
import static com.example.mobilesw.info.Util.showToast;
import static com.example.mobilesw.info.Util.storageUrlToName;

public class PostActivity extends AppCompatActivity {
    private static final String TAG = "WritePostActivity";
    private FirebaseUser user;
    private FirebaseFirestore firebaseFirestore = FirebaseFirestore.getInstance();
    private StorageReference storageRef;
    private ArrayList<String> pathList = new ArrayList<>();
    private ArrayList<String> showList = new ArrayList<>();
    private RelativeLayout buttonsBackgroundLayout, loaderLayout;
    private ImageView selectedImageVIew, c_book_image;
    private EditText selectedEditText, descriptionText, titleEditText;
    private TextView c_book_title,c_book_author;
    private LinearLayout parent, layout_buttons, layout_cbook;
    private PostInfo postInfo;
    private BookInfo bookInfo;
    private int pathCount, successCount;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_write_post);
        ActionBar actionBar = getSupportActionBar();
        if(actionBar != null){
            actionBar.setTitle("독후감 작성");
        }

        // 책 정보 받아오기
        layout_buttons = findViewById(R.id.layout_twobuttons);
        layout_cbook = findViewById(R.id.layout_cbook);
        c_book_title = findViewById(R.id.c_book_title);
        c_book_author = findViewById(R.id.c_book_author);
        c_book_image  = findViewById(R.id.c_book_image);

        Intent intent = getIntent();
        bookInfo = (BookInfo)intent.getSerializableExtra("book_info");

        if(bookInfo!=null){
            showBook(bookInfo.getImg());
        }

        parent = findViewById(R.id.contentsLayout);
        buttonsBackgroundLayout = findViewById(R.id.buttonsBackgroundLayout);
        loaderLayout = findViewById(R.id.loaderLayout);
        descriptionText = findViewById(R.id.contentsEditText);
        titleEditText = findViewById(R.id.titleEditText);

        findViewById(R.id.check).setOnClickListener(onClickListener);
        findViewById(R.id.image).setOnClickListener(onClickListener);
        findViewById(R.id.delete).setOnClickListener(onClickListener);
        findViewById(R.id.btn_my_library).setOnClickListener(onClickListener);
        findViewById(R.id.btn_search_book).setOnClickListener(onClickListener);

        buttonsBackgroundLayout.setOnClickListener(onClickListener);
        titleEditText.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    selectedEditText = null;
                }
            }
        });

        // 저장소
        FirebaseStorage storage = FirebaseStorage.getInstance();
        storageRef = storage.getReference();

        // 수정하는 경우
        if (intent.getSerializableExtra("postInfo")!=null) {
            postInfo = (PostInfo) getIntent().getSerializableExtra("postInfo");
            if(postInfo.getContents() != null){
                for(String str: postInfo.getContents()){
                    if(str.contains("bookthumb"))
                        showBook(str);
                }
            }
        }

        if(intent.getSerializableExtra("modify")!=null){

        }
        postInit();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            String path = data.getStringExtra(INTENT_PATH);
            pathList.add(path);

            ContentsItemView contentsItemView = new ContentsItemView(this);

            if (selectedEditText == null) {
                parent.addView(contentsItemView);
            } else {
                for (int i = 0; i < parent.getChildCount(); i++) {
                    if (parent.getChildAt(i) == selectedEditText.getParent()) {
                        parent.addView(contentsItemView, i + 1);
                        break;
                    }
                }
            }

            contentsItemView.setImage(path);
            contentsItemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    buttonsBackgroundLayout.setVisibility(View.VISIBLE);
                    selectedImageVIew = (ImageView) v;
                }
            });
            contentsItemView.setOnFocusChangeListener(onFocusChangeListener);
        }
    }

    View.OnClickListener onClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            Intent intent= new Intent(getApplicationContext(),MainActivity.class);;
            switch (v.getId()) {
                case R.id.check:
                    if(postInfo!=null){
                        edit();
                    }else {
                        storageUpload();
                    }
                    break;
                case R.id.image:
                    myStartActivity(GalleryActivity.class, GALLERY_IMAGE, 0);
                    break;

                case R.id.buttonsBackgroundLayout:
                    if (buttonsBackgroundLayout.getVisibility() == View.VISIBLE) {
                        buttonsBackgroundLayout.setVisibility(View.GONE);
                    }
                    break;

                // 책 찾기
                case R.id.btn_my_library:
                    intent.putExtra("fragnum",4);
                    intent.putExtra("isPost",true);
                    startActivity(intent);
                    finish();
                    break;
                case R.id.btn_search_book:
                    intent.putExtra("fragnum", 1);
                    intent.putExtra("isPost",true);
                    startActivity(intent);
                    finish();
                    break;

                // 개별 사진 삭제
                case R.id.delete:
                    final View selectedView = (View) selectedImageVIew.getParent();
                    String path;
                    int contSize;
                    int now = parent.indexOfChild(selectedView) - 1;
                    if(postInfo == null){
                        contSize = 0;
                        path = pathList.get(now);
                    }else{
                        contSize = postInfo.getContents().size();
                        // 새로 추가한 사진일 때
                        if(now >= contSize){
                            path = pathList.get(now-contSize);
                        }else {
                            // db에 올라가 있는 사진일 때
                            path = postInfo.getContents().get(now);
                        }
                    }

                    // 혹시 모르니까 db에 올라간 형식인지 다시 검사..
                    if(isStorageUrl(path)){
                        StorageReference desertRef = storageRef.child("posts/" + postInfo.getId() + "/" + storageUrlToName(path));
                        desertRef.delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                showToast(PostActivity.this, "파일을 삭제하였습니다.");
                                ArrayList<String> temp = postInfo.getContents();
                                temp.remove(parent.indexOfChild(selectedView) - 1);
                                firebaseFirestore.collection("posts").document(postInfo.getId())
                                        .update("contents",temp);
                                parent.removeView(selectedView);
                                buttonsBackgroundLayout.setVisibility(View.GONE);
                            }
                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception exception) {
                                showToast(PostActivity.this, "파일을 삭제하는데 실패하였습니다.");
                            }
                        });
                    }else{
                        // 방금 올린 사진일 때
                        if(postInfo == null){
                            pathList.remove(now);
                        }else {
                            pathList.remove(now - postInfo.getContents().size());
                        }
                        System.out.println(parent.indexOfChild(selectedView) - 1);
                        parent.removeView(selectedView);
                        buttonsBackgroundLayout.setVisibility(View.GONE);
                    }
                    break;

            }
        }
    };

    View.OnFocusChangeListener onFocusChangeListener = new View.OnFocusChangeListener() {
        @Override
        public void onFocusChange(View v, boolean hasFocus) {
            if (hasFocus) {
                selectedEditText = (EditText) v;
            }
        }
    };


    // 수정하는 경우
    private void edit() {
        String title = ((EditText) findViewById(R.id.titleEditText)).getText().toString();
        String description = ((EditText) findViewById(R.id.contentsEditText)).getText().toString();

        if (title.length() > 0) {
            loaderLayout.setVisibility(View.VISIBLE);
            final ArrayList<String> contentsList = postInfo.getContents();

            // 만약 새로 작성한 글이면 postInfo == null -> posts 밑에 새 ID로 문서 생성
            // 기존 글을 수정하는거면 postInfo != null -> postInfo ID 찾아서 해당 글 문서에 다시 업로드
            final DocumentReference documentReference = firebaseFirestore.collection("posts").document(postInfo.getId());
            final Date date = postInfo.getCreatedAt();

            postInfo.setTitle(title);
            postInfo.setDescription(description);

            // 사진을 바꾸지 않았을 때 타이틀, 설명만 수정
            if(pathList.size() == 0){
                documentReference.update("title",title);
                documentReference.update("description",description);

                startIntent();
            }

            // 새로 올린 사진은 저장소에 올리고 contents 리스트 db에도 반영
            for (int i = 0; i < pathList.size(); i++) {

                pathCount = i;
                successCount++;
                String path = pathList.get(pathCount);
                contentsList.add(path);
                System.out.println("cont1: "+contentsList);
                String[] pathArray = path.split("\\.");
                // posts 테이블의 문서 ID 받아서 해당 문서에 정보 업로드
                final StorageReference mountainImagesRef = storageRef.child("posts/" + postInfo.getId() + "/" + (contentsList.size()-1) + "." + pathArray[pathArray.length - 1]);
                try {
                    InputStream stream = new FileInputStream(new File(pathList.get(pathCount)));
                    StorageMetadata metadata = new StorageMetadata.Builder().setCustomMetadata("index", "" + (contentsList.size() - 1)).build();
                    UploadTask uploadTask = mountainImagesRef.putStream(stream, metadata);
                    uploadTask.addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception exception) {
                        }
                    }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            final int index = Integer.parseInt(taskSnapshot.getMetadata().getCustomMetadata("index"));
                            System.out.println("index: "+index);
                            mountainImagesRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                @Override
                                public void onSuccess(Uri uri) {
                                    contentsList.set(index, uri.toString());
                                    postInfo.setContents(contentsList);
                                    // 새로운 파일들 저장소에 다 올렸을 때 db에 contentlist 저장
                                    successCount--;
                                    updateDB(postInfo);
                                    System.out.println("cont2: "+contentsList);

                                }
                            });
                        }
                    });
                } catch (FileNotFoundException e) {
                    Log.e("로그", "에러: " + e.toString());
                }

            }

        } else {
            showToast(PostActivity.this, "제목을 입력해주세요.");
        }
    }

    private void updateDB(PostInfo postInfo){
        if(successCount == 0){
            DocumentReference documentReference = firebaseFirestore.collection("posts").document(postInfo.getId());
            documentReference.update("title",postInfo.getTitle());
            documentReference.update("description",postInfo.getDescription());
            documentReference.update("contents",postInfo.getContents());
            startIntent();
        }
    }

    // 처음 올리는 글일 때
    private void storageUpload() {
        String title = ((EditText) findViewById(R.id.titleEditText)).getText().toString();
        String description = "독후감 내용: " + ((EditText) findViewById(R.id.contentsEditText)).getText().toString();;

        if (title.length() > 0) {
            loaderLayout.setVisibility(View.VISIBLE);
            final ArrayList<String> contentsList = new ArrayList<>();
            user = FirebaseAuth.getInstance().getCurrentUser();

            FirebaseFirestore firebaseFirestore = FirebaseFirestore.getInstance();
            // 만약 새로 작성한 글이면 postInfo == null -> posts 밑에 새 ID로 문서 생성
            // 기존 글을 수정하는거면 postInfo != null -> postInfo ID 찾아서 해당 글 문서에 다시 업로드
            final DocumentReference documentReference = firebaseFirestore.collection("posts").document();
            final Date date = new Date();

            if(bookInfo != null){
                contentsList.add(bookInfo.getImg());
            }

            for (int i = 0; i < parent.getChildCount(); i++) {
                LinearLayout linearLayout = (LinearLayout) parent.getChildAt(i);
                for (int ii = 0; ii < linearLayout.getChildCount(); ii++) {
                    View view = linearLayout.getChildAt(ii);
                    if (view instanceof EditText) {
                        String text = ((EditText) view).getText().toString();
                        if (text.length() > 0) {
                            // 내용 리스트에 약속 설명 추가
                        }

                    }
                    // 책 이미지, 갤러리 사진 모두 올리는 경우
                    else if (!isStorageUrl(pathList.get(pathCount))) {
                        String path = pathList.get(pathCount);
                        successCount++;
                        contentsList.add(path);
                        String[] pathArray = path.split("\\.");
                        // posts 테이블의 문서 ID 받아서 해당 문서에 정보 업로드
                        final StorageReference mountainImagesRef = storageRef.child("posts/" + documentReference.getId() + "/" + pathCount + "." + pathArray[pathArray.length - 1]);
                        try {
                            InputStream stream = new FileInputStream(new File(pathList.get(pathCount)));
                            StorageMetadata metadata = new StorageMetadata.Builder().setCustomMetadata("index", "" + (contentsList.size() - 1)).build();
                            UploadTask uploadTask = mountainImagesRef.putStream(stream, metadata);
                            uploadTask.addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception exception) {
                                }
                            }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                                @Override
                                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                                    final int index = Integer.parseInt(taskSnapshot.getMetadata().getCustomMetadata("index"));
                                    mountainImagesRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                        @Override
                                        public void onSuccess(Uri uri) {
                                            successCount--;
                                            contentsList.set(index, uri.toString());
                                            if (successCount == 0) {
                                                    storeUpload(documentReference, new PostInfo(title, description, contentsList, user.getUid(), date));
                                            }
                                        }
                                    });
                                }
                            });
                        } catch (FileNotFoundException e) {
                            Log.e("로그", "에러: " + e.toString());
                        }
                        pathCount++;
                    }
                }
            }
            if(successCount == 0) {
                    storeUpload(documentReference, new PostInfo(title, description, contentsList, user.getUid(), date));
            }
        } else {
            showToast(PostActivity.this, "제목을 입력해주세요.");
        }
    }

    // db에 등록 성공했는지 검사, 다시 FragBoard로 돌아옴
    private void storeUpload(DocumentReference documentReference, final PostInfo postInfo) {
        documentReference.set(postInfo.getPostInfo())
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.d(TAG, "DocumentSnapshot successfully written!");
                        loaderLayout.setVisibility(View.GONE);
                        startIntent();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.w(TAG, "Error writing document", e);
                        loaderLayout.setVisibility(View.GONE);
                    }
                });
    }

    // 작성한 글이 보이게 함
    private void postInit() {
        if (postInfo != null) {
            titleEditText.setText(postInfo.getTitle());
            descriptionText.setText(postInfo.getDescription());
            ArrayList<String> contentsList = postInfo.getContents();
            for (int i = 0; i < contentsList.size(); i++) {
                String contents = contentsList.get(i);
                if (isStorageUrl(contents)) {
                    showList.add(contents);
                    ContentsItemView contentsItemView = new ContentsItemView(this);
                    parent.addView(contentsItemView);

                    contentsItemView.setImage(contents);
                    // 이미지 누르면 삭제 버튼 보이게 함
                    contentsItemView.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            buttonsBackgroundLayout.setVisibility(View.VISIBLE);
                            selectedImageVIew = (ImageView) v;
                        }
                    });

                }
            }
        }
    }

    private void showBook(String image) {
        if(!image.equals("")){
            layout_buttons.setVisibility(layout_buttons.GONE);
            layout_cbook.setVisibility(layout_cbook.VISIBLE);
            //Picasso.get().load(image).into(holder.img);
            Glide.with(this).load(image).into(c_book_image);
        }
    }

    private void startIntent() {
        Intent intent= new Intent(getApplicationContext(),MainActivity.class);;
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        intent.putExtra("fragnum",3);
        startActivity(intent);
    }

    private void myStartActivity(Class c, int media, int requestCode) {
        Intent intent = new Intent(this, c);
        intent.putExtra(INTENT_MEDIA, media);
        startActivityForResult(intent, requestCode);
    }
}