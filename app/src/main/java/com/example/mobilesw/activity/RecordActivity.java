package com.example.mobilesw.activity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.example.mobilesw.info.RecordInfo;
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
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

import static com.example.mobilesw.info.Util.GALLERY_IMAGE;
import static com.example.mobilesw.info.Util.INTENT_MEDIA;
import static com.example.mobilesw.info.Util.INTENT_PATH;
import static com.example.mobilesw.info.Util.isStorageUrl;
import static com.example.mobilesw.info.Util.showToast;
import static com.example.mobilesw.info.Util.storageUrlToName;

public class RecordActivity extends AppCompatActivity {
    private static final String TAG = "RecordActivity";
    private FirebaseUser user;
    private FirebaseFirestore firebaseFirestore = FirebaseFirestore.getInstance();
    private StorageReference storageRef;
    private ArrayList<String> pathList = new ArrayList<>();
    private ArrayList<String> showList = new ArrayList<>();
    private LinearLayout parent;
    private RelativeLayout buttonsBackgroundLayout;
    private RelativeLayout loaderLayout;
    private ImageView selectedImageVIew;
    private EditText selectedEditText;
    private EditText descriptionText;
    private EditText readtimeEditText;
    private EditText titleEditText;
    private RecordInfo recordInfo;
    private int pathCount, successCount;
    private  String saveCurrentDate;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_record);

        parent = findViewById(R.id.contentsLayout);
        buttonsBackgroundLayout = findViewById(R.id.buttonsBackgroundLayout);
        loaderLayout = findViewById(R.id.loaderLayout);
        descriptionText = findViewById(R.id.contentsEditText);
        titleEditText = findViewById(R.id.titleEditText);
        readtimeEditText = findViewById(R.id.readtimeEditText);

        findViewById(R.id.check).setOnClickListener(onClickListener);
        findViewById(R.id.image).setOnClickListener(onClickListener);
        findViewById(R.id.delete).setOnClickListener(onClickListener);

        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat currentDate = new SimpleDateFormat("yyyy-MM-dd");
        saveCurrentDate = currentDate.format(calendar.getTime());

        buttonsBackgroundLayout.setOnClickListener(onClickListener);
        titleEditText.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    selectedEditText = null;
                }
            }
        });

        FirebaseStorage storage = FirebaseStorage.getInstance();
        storageRef = storage.getReference();
        Bundle bundle = getIntent().getExtras();
        recordInit();
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
            switch (v.getId()) {
                case R.id.check:
                    if(recordInfo!=null){
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
                case R.id.delete:
                    final View selectedView = (View) selectedImageVIew.getParent();
                    String path;
                    int contSize;
                    int now = parent.indexOfChild(selectedView) - 1;
                    if(recordInfo == null){
                        contSize = 0;
                        path = pathList.get(now);
                    }else{
                        contSize = recordInfo.getContents().size();
                        if(now >= contSize){
                            path = pathList.get(now-contSize);
                        }else {
                            path = recordInfo.getContents().get(now);
                        }
                    }

                    if(isStorageUrl(path)){
                        StorageReference desertRef = storageRef.child("records/" + recordInfo.getId() + "/" + storageUrlToName(path));
                        desertRef.delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                showToast(RecordActivity.this, "파일을 삭제하였습니다.");
                                ArrayList<String> temp = recordInfo.getContents();
                                temp.remove(parent.indexOfChild(selectedView) - 1);
                                firebaseFirestore.collection("records").document(recordInfo.getId())
                                        .update("contents",temp);
                                parent.removeView(selectedView);
                                buttonsBackgroundLayout.setVisibility(View.GONE);
                            }
                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception exception) {
                                showToast(RecordActivity.this, "파일을 삭제하는데 실패하였습니다.");
                            }
                        });
                    }else{
                        if(recordInfo == null){
                            pathList.remove(now);
                        }else {
                            pathList.remove(now - recordInfo.getContents().size());
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

    private void edit() {
        String title = ((EditText) findViewById(R.id.titleEditText)).getText().toString();
        String readtime = ((EditText) findViewById(R.id.readtimeEditText)).getText().toString();
        String description = ((EditText) findViewById(R.id.contentsEditText)).getText().toString();

        if (title.length() > 0) {
            loaderLayout.setVisibility(View.VISIBLE);
            final ArrayList<String> contentsList = recordInfo.getContents();

            final DocumentReference documentReference = firebaseFirestore.collection("records").document(recordInfo.getId());
            final Date date = recordInfo.getCreatedAt();

            recordInfo.setTitle(title);
            recordInfo.setReadtime(readtime);
            recordInfo.setDescription(description);

            if(pathList.size() == 0){
                documentReference.update("title",title);
                documentReference.update("readtime",readtime);
                documentReference.update("description",description);
                documentReference.update("date",saveCurrentDate);

                Intent resultIntent = new Intent();
                resultIntent.putExtra("recordinfo", recordInfo);
                setResult(RESULT_OK, resultIntent);
                finish();
            }

            for (int i = 0; i < pathList.size(); i++) {

                pathCount = i;
                successCount++;
                String path = pathList.get(pathCount);
                contentsList.add(path);
                System.out.println("cont1: "+contentsList);
                String[] pathArray = path.split("\\.");

                final StorageReference mountainImagesRef = storageRef.child("records/" + recordInfo.getId() + "/" + (contentsList.size()-1) + "." + pathArray[pathArray.length - 1]);
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
                                    recordInfo.setContents(contentsList);
                                    successCount--;
                                    updateDB(recordInfo);
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
            showToast(RecordActivity.this, "제목을 입력해주세요.");
        }
    }

    private void updateDB(RecordInfo recordInfo){
        if(successCount == 0){
            DocumentReference documentReference = firebaseFirestore.collection("records").document(recordInfo.getId());
            documentReference.update("title",recordInfo.getTitle());
            documentReference.update("readtime",recordInfo.getReadtime());
            documentReference.update("description",recordInfo.getDescription());
            documentReference.update("contents",recordInfo.getContents());
            documentReference.update("dates",recordInfo.getDate());

            Intent resultIntent = new Intent();
            resultIntent.putExtra("recordinfo", recordInfo);
            setResult(RESULT_OK, resultIntent);
            finish();
        }
    }

    private void storageUpload() {
        String title = ((EditText) findViewById(R.id.titleEditText)).getText().toString();
        String readtime = ((EditText) findViewById(R.id.readtimeEditText)).getText().toString();
        String description = ((EditText) findViewById(R.id.contentsEditText)).getText().toString();

        if (title.length() > 0) {
            loaderLayout.setVisibility(View.VISIBLE);
            final ArrayList<String> contentsList = new ArrayList<>();
            user = FirebaseAuth.getInstance().getCurrentUser();

            FirebaseFirestore firebaseFirestore = FirebaseFirestore.getInstance();
            final DocumentReference documentReference = firebaseFirestore.collection("records").document();
            final Date date = new Date();

            for (int i = 0; i < parent.getChildCount(); i++) {
                LinearLayout linearLayout = (LinearLayout) parent.getChildAt(i);
                for (int ii = 0; ii < linearLayout.getChildCount(); ii++) {
                    View view = linearLayout.getChildAt(ii);
                    if (view instanceof EditText) {
                        String text = ((EditText) view).getText().toString();
                        if (text.length() > 0) {
                        }

                    }
                    else if (!isStorageUrl(pathList.get(pathCount))) {
                        String path = pathList.get(pathCount);
                        successCount++;
                        contentsList.add(path);
                        String[] pathArray = path.split("\\.");
                        final StorageReference mountainImagesRef = storageRef.child("records/" + documentReference.getId() + "/" + pathCount + "." + pathArray[pathArray.length - 1]);
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
                                                storeUpload(documentReference, new RecordInfo(title, readtime, description, contentsList, user.getUid(), date, saveCurrentDate));
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
                storeUpload(documentReference, new RecordInfo(title, readtime, description, contentsList, user.getUid(), date,saveCurrentDate));
            }
        } else {
            showToast(RecordActivity.this, "제목을 입력해주세요.");
        }
    }

    private void storeUpload(DocumentReference documentReference, final RecordInfo recordInfo) {
        documentReference.set(recordInfo.getRecordInfo())
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.d(TAG, "DocumentSnapshot successfully written!");
                        loaderLayout.setVisibility(View.GONE);
                        Intent resultIntent = new Intent();
                        resultIntent.putExtra("recordinfo", recordInfo);
                        setResult(RESULT_OK, resultIntent);
                        finish();
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

    private void recordInit() {
        if (recordInfo != null) {
            titleEditText.setText(recordInfo.getTitle());
            readtimeEditText.setText(recordInfo.getReadtime());
            descriptionText.setText(recordInfo.getDescription());
            ArrayList<String> contentsList = recordInfo.getContents();
            for (int i = 0; i < contentsList.size(); i++) {
                String contents = contentsList.get(i);
                if (isStorageUrl(contents)) {
                    showList.add(contents);
                    ContentsItemView contentsItemView = new ContentsItemView(this);
                    parent.addView(contentsItemView);

                    contentsItemView.setImage(contents);
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

    private void myStartActivity(Class c, int media, int requestCode) {
        Intent intent = new Intent(this, c);
        intent.putExtra(INTENT_MEDIA, media);
        startActivityForResult(intent, requestCode);
    }
}
