package com.example.mobilesw.fragment;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RelativeLayout;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentResultListener;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.mobilesw.R;
import com.example.mobilesw.activity.BookInfoActivity;
import com.example.mobilesw.adapter.BookAdapter;
import com.example.mobilesw.info.BookInfo;
import com.example.mobilesw.info.NaverAPI;
import com.google.common.primitives.Booleans;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.GregorianCalendar;
import java.util.Objects;

public class FragSearch extends Fragment {

    private ArrayList<BookInfo> bookList;
    private String apiURL = "https://openapi.naver.com/v1/search/book.json?";
    private String query ="";
    private int start=1;
    private int num = 10;

    RecyclerView rcv;
    LinearLayoutManager llm;
    RelativeLayout charactor_view;

    BookAdapter bookAdapter;

    private Button btn_search;
    private EditText edit_search;

    private boolean isRandomChat,isBookReport;
    private GregorianCalendar gc;
    private Boolean isPost;

    public static FragSearch newInstance(){
        return new FragSearch();
    }

    public FragSearch() { }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.frag_search, container, false);

        if(getArguments()!=null){
            gc = (GregorianCalendar)getArguments().getSerializable("date");
            isPost = getArguments().getBoolean("isPost", false);
        }
        getParentFragmentManager().setFragmentResultListener("chat",this, new FragmentResultListener() {
            @Override
            public void onFragmentResult(@NonNull String requestKey, @NonNull  Bundle bundle) {
                isRandomChat = bundle.getBoolean("isRandomChat");
                isBookReport = bundle.getBoolean("isBookReport");
                System.out.println("isRandomChat : " +isRandomChat);
                System.out.println("isBookReport : " +isBookReport);
            }
        });

        System.out.println("soopy"+"yes");

        charactor_view = view.findViewById(R.id.charactor_view);
        rcv = view.findViewById(R.id.search_list);
        llm = new LinearLayoutManager(getContext());
        rcv.setHasFixedSize(true);//각 아이템이 보여지는 것을 일정하게
        rcv.setLayoutManager(llm);//앞서 선언한 리싸이클러뷰를 레이아웃 매니저에 붙힌다.

        rcv.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
            }

            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);

                int lastPosition = ((LinearLayoutManager) rcv.getLayoutManager()).findLastCompletelyVisibleItemPosition();
                int totalCount = rcv.getAdapter().getItemCount();

                if(lastPosition+1==totalCount){
                    start+=10;
                    System.out.println("마지막 위치 도달 , 그 다음 시작 위치 : "+start);
                    new BookThread().execute();
                }
            }
        });

        btn_search = view.findViewById(R.id.btn_search);
        edit_search= view.findViewById(R.id.edit_search);


        bookList = new ArrayList<BookInfo>();

        btn_search.setOnClickListener(onClickListener);
        edit_search.setOnKeyListener(new View.OnKeyListener(){

            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                //키 이벤트가 2번 연속으로 발생하는 것을 방지
                if(event.getAction()==KeyEvent.ACTION_DOWN&&keyCode==KeyEvent.KEYCODE_ENTER){
                    System.out.println("엔터키 동작");
                    charactor_view.setVisibility(charactor_view.GONE);
                    rcv.setVisibility(rcv.VISIBLE);
                    start=1;
                    bookList.clear();
                    query =edit_search.getText().toString();
                    new BookThread().execute();
                    return true;
                }
                return false;
            }
        });
        return view;
    }

    View.OnClickListener onClickListener = new View.OnClickListener(){

        @Override
        public void onClick(View v) {
            switch (v.getId()){
                case R.id.btn_search:
                    charactor_view.setVisibility(charactor_view.GONE);
                    rcv.setVisibility(rcv.VISIBLE);
                    start=1;
                    bookList.clear();
                    query =edit_search.getText().toString();
                    new BookThread().execute();
            }
        }
    };
    //BackThread 생성
    public class BookThread extends AsyncTask<String,String,String> {

        @Override
        protected String doInBackground(String... strings) {
            return NaverAPI.main(apiURL,query,start,0,num);
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            System.out.println("결과.........."+s);
            try {
                JSONArray jArray=new JSONObject(s).getJSONArray("items");
                for(int i=0; i<jArray.length(); i++){
                    JSONObject obj=jArray.getJSONObject(i);

                    String title = obj.getString("title");
                    title = title.replaceAll("<(/)?([a-zA-Z]*)(\\\\s[a-zA-Z]*=[^>]*)?(\\\\s)*(/)?>","");
                    String author = obj.getString("author");
                    author = author.replaceAll("<(/)?([a-zA-Z]*)(\\\\s[a-zA-Z]*=[^>]*)?(\\\\s)*(/)?>","");
                    String image = obj.getString("image");
                    String publisher = obj.getString("publisher");
                    publisher = publisher.replaceAll("<(/)?([a-zA-Z]*)(\\\\s[a-zA-Z]*=[^>]*)?(\\\\s)*(/)?>","");
                    String pubdate = obj.getString("pubdate");
                    String description = obj.getString("description");
                    description = description.replaceAll("<(/)?([a-zA-Z]*)(\\\\s[a-zA-Z]*=[^>]*)?(\\\\s)*(/)?>","");
                    description = description.replaceAll("&lt(;)?(/)?([a-zA-Z]*)(\\s[a-zA-Z]*=[^>]*)?(\\s)*(/)?&gt(;)?","");

                    if(pubdate.length()==8){
                        pubdate = pubdate.substring(0,4)+"-"+pubdate.substring(4,6)+"-"+pubdate.substring(6,8);
                    }
                    BookInfo bookInfo=new BookInfo(title,author,image,publisher,pubdate,description);
                    bookList.add(bookInfo);
                }
                System.out.println("책 배열 크기 : "+bookList.size());
                bookAdapter=new BookAdapter(getContext(),bookList); //앞서 만든 리스트를 어댑터에 적용시켜 객체를 만든다.
                bookAdapter.setOnItemClickListener(
                        new BookAdapter.OnItemClickListener() {
                            @Override
                            public void onItemClick(View v, int pos) {
                                BookInfo obj = bookList.get(pos);
                                Intent intent = new Intent(getContext(), BookInfoActivity.class);

                                System.out.println("isRandomChat : " +isRandomChat);
                                System.out.println("isBookReport : " +isBookReport);
                                //목록에서 클릭한 게시물에 대한 내용을 전달
                                intent.putExtra("bookInfo", (Serializable) obj);
                                intent.putExtra("isRandomChat",isRandomChat);
                                intent.putExtra("isBookReport",isBookReport);
                                intent.putExtra("date",gc);
                                intent.putExtra("isPost", isPost);
                                startActivity(intent);
                            }
                        }
                );
                rcv.setAdapter(bookAdapter);
                LinearLayoutManager linearLayoutManager = (LinearLayoutManager)rcv.getLayoutManager();
                if(linearLayoutManager!=null){
                    if(start>1){
                        linearLayoutManager.scrollToPositionWithOffset(start-3,0);
                    }
                }
                //rcv.scrollToPosition(start);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }
}
