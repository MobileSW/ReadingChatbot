package com.example.mobilesw.activity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.example.mobilesw.R;
import com.example.mobilesw.info.MemberInfo;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import static com.example.mobilesw.info.Util.INTENT_MEDIA;
import static com.example.mobilesw.info.Util.INTENT_PATH;
import static com.example.mobilesw.info.Util.GALLERY_IMAGE;
import static com.example.mobilesw.info.Util.isProfileUrl;
import static com.example.mobilesw.info.Util.makeDialog;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;


public class ProfileActivity extends AppCompatActivity implements View.OnClickListener {

    private Button checkButton;
    private TextView nameTv;
    private SharedPreferences sp;
    private ImageView profileImageVIew;
    private String profilePath;
    private RelativeLayout buttonBackgroundLayout;
    FirebaseStorage storage = FirebaseStorage.getInstance();
    StorageReference storageRef = storage.getReference();
    DocumentReference docRef;

    FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
    FirebaseFirestore db = FirebaseFirestore.getInstance();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        checkButton = findViewById(R.id.checkButton);
        nameTv = (EditText) findViewById(R.id.nameEditText);

        profileImageVIew = findViewById(R.id.profileImageView);
        buttonBackgroundLayout = findViewById(R.id.buttonsBackgroundLayout);

        sp = getSharedPreferences("sp", MODE_PRIVATE);


        beforeInfo();

        System.out.println("main:" + profilePath);

        checkButton.setOnClickListener(this);
        profileImageVIew.setOnClickListener(this);

        buttonBackgroundLayout.setOnClickListener(this);
        findViewById(R.id.delete).setOnClickListener(this);
        findViewById(R.id.gallery).setOnClickListener(this);


    }

    private void beforeInfo() {
        // ?????? ?????? ??? ???????????? -> ??? ?????? ??? ???????????? ????????? ??????

        String name = sp.getString("name", "");
        profilePath = sp.getString("profilePath", "");

        // ?????? ??????
        nameTv.setText(name);

        if (profilePath.equals("")) {
            profileImageVIew.setImageResource(R.drawable.default_profile);

        } else if (profilePath != null) {
            Glide.with(this).load(profilePath).centerCrop().override(500).into(profileImageVIew);
        }
    }

    private void save() {
        SharedPreferences.Editor editor = sp.edit(); // editor ????????? ??????
        // ????????? ?????? ??? ??????
        editor.putString("name", nameTv.getText().toString());
        editor.putString("profilePath", profilePath);
        Log.d("info Test ", "work value" + nameTv.getText().toString());
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            // ?????? ?????? -> ?????? ?????? db ??????
            case R.id.checkButton:
                // ?????? ?????? ??????
                if (((EditText) findViewById(R.id.nameEditText)).getText().toString().length() == 0) {
                    makeDialog("", "????????? ???????????????." , ProfileActivity.this);
                    break;
                } else {
                    profileUpdate();
                    break;
                }
            case R.id.profileImageView:
                save();
                buttonBackgroundLayout.setVisibility(View.VISIBLE);
                break;

            case R.id.buttonsBackgroundLayout:
                buttonBackgroundLayout.setVisibility(View.GONE);
                break;

            case R.id.gallery:
                myStartActivity(GalleryActivity.class, GALLERY_IMAGE, 0);
                break;

            case R.id.delete:
                FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                System.out.println("delpath:" + profilePath);
                if (isProfileUrl(profilePath)) {
                    final StorageReference mountainImagesRef = storageRef.child("users/" + user.getUid() + "/profileImage.jpg");
                    mountainImagesRef.delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            makeDialog("", "????????? ????????? ?????????????????????." , ProfileActivity.this);
                            db.collection("users").document(user.getUid())
                                    .update("profilePath", null);
                            profilePath = "";
                            SharedPreferences.Editor editor = sp.edit(); // editor ????????? ??????
                            editor.remove("profilePath");
                            editor.commit();
                            System.out.println("db:" + profilePath);
                            profileImageVIew.setImageResource(R.drawable.default_profile);
                            buttonBackgroundLayout.setVisibility(View.GONE);
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception exception) {
                        }
                    });
                } else {
                    // ?????? ?????? ????????? ???
                    profilePath = "";
                    System.out.println("del:" + profilePath);
                    profileImageVIew.setImageResource(R.drawable.default_profile);
                    buttonBackgroundLayout.setVisibility(View.GONE);
                }
                break;
        }
    }


    // ????????? ?????? ?????? db??? ??????
    private void profileUpdate() {
        String name = ((EditText) findViewById(R.id.nameEditText)).getText().toString();

        // ?????? ?????? ?????? ?????? -> db??????
        MemberInfo memberInfo = new MemberInfo(name);

        if (user != null) {
            if (profilePath != null) {
                if (profilePath.equals("") || isProfileUrl(profilePath)) {
                    if (!profilePath.equals(""))
                        memberInfo.setProfilePath(profilePath);
                    storeUploader(memberInfo);
                } else {
                    System.out.println(profilePath);
                    System.out.println("profile");
                    final StorageReference mountainImagesRef = storageRef.child("users/" + user.getUid() + "/profileImage.jpg");
                    try {
                        InputStream stream = new FileInputStream(new File(profilePath));
                        UploadTask uploadTask = mountainImagesRef.putStream(stream);
                        uploadTask.continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
                            @Override
                            public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                                if (!task.isSuccessful()) {
                                    throw task.getException();
                                }
                                return mountainImagesRef.getDownloadUrl();
                            }
                        }).addOnCompleteListener(new OnCompleteListener<Uri>() {
                            @Override
                            public void onComplete(@NonNull Task<Uri> task) {
                                if (task.isSuccessful()) {
                                    Uri downloadUri = task.getResult();
                                    memberInfo.setProfilePath(downloadUri.toString());
                                    System.out.println("profilepath" + downloadUri.toString());
                                    storeUploader(memberInfo);
                                } else {
                                    makeDialog("", "????????? ???????????? ??????????????????" , ProfileActivity.this);
                                }
                            }
                        });
                    } catch (FileNotFoundException e) {
                        Log.e("??????", "??????: " + e.toString());
                    }
                }
            } else {
                storeUploader(memberInfo);
            }
        } else {
            makeDialog("", "??????????????? ??????????????????." , ProfileActivity.this);
        }

    }

    private void storeUploader(MemberInfo memberInfo) {
        docRef = db.collection("users").document(user.getUid());
        docRef.update("name", memberInfo.getName());
        docRef.update("profilePath", memberInfo.getProfilePath());

        SharedPreferences.Editor editor = sp.edit(); // editor ????????? ??????

        // ????????? ?????? ??? ??????
        editor.putString("name", nameTv.getText().toString());
        if (memberInfo.getProfilePath() != null) {
            System.out.println(memberInfo.getProfilePath());
            editor.putString("profilePath", memberInfo.getProfilePath());
        } else {
            editor.remove("profilePath");
            System.out.println("remove");
        }

        editor.commit(); // ?????? ??????
        makeDialog("", "?????? ?????? ????????? ??????????????????." , ProfileActivity.this);
        myStartActivity(MainActivity.class);
        finish();

/*
        db.collection("users").document(user.getUid()).set(memberInfo)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        SharedPreferences.Editor editor = sp.edit(); // editor ????????? ??????

                        // ????????? ?????? ??? ??????
                        editor.putString("name", nameTv.getText().toString());
                        if (memberInfo.getProfilePath() != null) {
                            System.out.println(memberInfo.getProfilePath());
                            editor.putString("profilePath", memberInfo.getProfilePath());
                        } else {
                            editor.remove("profilePath");
                            System.out.println("remove");
                        }

                        editor.commit(); // ?????? ??????
                        startToast("???????????? ????????? ?????????????????????.");
                        myStartActivity(MainActivity.class);
                        finish();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        startToast("???????????? ????????? ?????????????????????.");
                    }
                });

 */

    }

    private void myStartActivity(Class c) {
        Intent intent = new Intent(this, c);
        intent.addFlags(intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
    }


    private void myStartActivity(Class c, int media, int requestCode) {
        Intent intent = new Intent(this, c);
        intent.putExtra(INTENT_MEDIA, media);
        startActivityForResult(intent, requestCode);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK) {
            profilePath = data.getStringExtra(INTENT_PATH);
            System.out.println(profilePath);
            Glide.with(this).load(profilePath).centerCrop().override(500).into(profileImageVIew);
            buttonBackgroundLayout.setVisibility(View.GONE);
        }
    }
}
