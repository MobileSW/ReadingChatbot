// 게시판 글 수정, 삭제시의 동작 담당 Activity
package com.example.mobilesw.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.example.mobilesw.info.PostInfo;
import com.example.mobilesw.R;
import com.example.mobilesw.view.ReadContentsView;

public class BoardActivity extends AppCompatActivity {
    private PostInfo postInfo;
    private ReadContentsView readContentsVIew;
    private LinearLayout contentsLayout;

    private TextView toolbar_title;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.content_board);

        postInfo = (PostInfo) getIntent().getSerializableExtra("postInfo");
        contentsLayout = findViewById(R.id.contentsLayout);
        readContentsVIew = findViewById(R.id.readContentsView);

        toolbar_title = findViewById(R.id.toolbar_title);
        toolbar_title.setText("작성한 독후감");

        uiUpdate();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == 0 && requestCode == Activity.RESULT_OK){
            postInfo = (PostInfo)data.getSerializableExtra("postInfo");
            contentsLayout.removeAllViews();
            uiUpdate();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.post_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }


    private void uiUpdate(){
        readContentsVIew.setPostInfo(postInfo);
    }

    private void myStartActivity(Class c, PostInfo postInfo) {
        Intent intent = new Intent(this, c);
        intent.putExtra("postInfo", postInfo);
        startActivityForResult(intent, 0);
    }
}