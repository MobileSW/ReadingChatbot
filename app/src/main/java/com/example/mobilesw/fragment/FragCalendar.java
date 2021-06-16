package com.example.mobilesw.fragment;

import android.content.Intent;
import android.nfc.Tag;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.mobilesw.R;
import com.example.mobilesw.activity.PostActivity;
import com.example.mobilesw.activity.PostCalendarAvtivity;
import com.example.mobilesw.activity.RecordActivity;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;

import com.example.mobilesw.activity.RecordActivity;
import com.example.mobilesw.activity.ShowCalendarPost;
import com.example.mobilesw.adapter.CalendarAdapter;
import com.example.mobilesw.info.BookInfo;
import com.example.mobilesw.info.DateUtil;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QuerySnapshot;

import java.lang.reflect.Array;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FragCalendar extends Fragment {
    private View view;
    public int mCenterPosition;
    private int i=0;
    private long mCurrentTime;
    public ArrayList<Object> mCalendarList = new ArrayList<>();

    public TextView textView;
    public RecyclerView recyclerView;
    private CalendarAdapter calendarAdapter;
    private StaggeredGridLayoutManager manager;
    private GregorianCalendar cal;

    private Button btn_before,btn_after;

    private ArrayList<String> date;
    private ArrayList<String> image;
    private ArrayList<HashMap<String, String>> booklist;

    final FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
    FirebaseFirestore db;
    DocumentReference docRef;

    private ArrayList<Object> cbooks;

    public FragCalendar(){

    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.frag_calendar,container,false);

        btn_before = (Button) view.findViewById(R.id.btn_before);
        btn_after = (Button) view.findViewById(R.id.btn_after);

        btn_before.setOnClickListener(onClickListener);
        btn_after.setOnClickListener(onClickListener);

        initView(view);

        initSet();

        //setRecycler();

        return view;

    }
    public void initView(View v){
        //textView = (TextView)v.findViewById(R.id.title);

        date = new ArrayList<String>();
        image = new ArrayList<String>();
        recyclerView = (RecyclerView)v.findViewById(R.id.calendar);

    }

    View.OnClickListener onClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            switch(v.getId()){
                case R.id.btn_before:
                    i--;
                    setCalendarList(cal,date,image);
                    System.out.println("뒤로가기 선택, i 값은 : "+i);
                    System.out.println("date 값은 : "+date);
                    break;
                case R.id.btn_after:
                    i++;
                    setCalendarList(cal,date,image);
                    break;
            }
        }
    };

    public void initSet(){

        initCalendarList();

    }

    public void initCalendarList() {

        db = FirebaseFirestore.getInstance();
        docRef = db.collection("users").document(user.getUid());

        docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                cbooks = new ArrayList<Object>();
                DocumentSnapshot document = task.getResult();
                booklist = (ArrayList<HashMap<String, String>>) document.get("bookCalendar");
                if (booklist != null) {
                    for (int i = 0; i < booklist.size(); i++) {
                        HashMap<String, String> hm = booklist.get(i);
                        System.out.println("booklist 출력 : " + hm);
                        date.add(hm.get("date"));
                        image.add(hm.get("image"));
                    }
                    System.out.println("독서달력 date 배열(db) : " + date);
                }
                cal = new GregorianCalendar();
                setCalendarList(cal,date,image);
            }
        });

    }

    private void setRecycler() {

        if (mCalendarList == null) {
            System.out.println( "No Query, not initializing RecyclerView");
        }

        manager = new StaggeredGridLayoutManager(7, StaggeredGridLayoutManager.VERTICAL);

        calendarAdapter = new CalendarAdapter(getContext(),mCalendarList);
        calendarAdapter.setOnItemClickListener(
                new CalendarAdapter.OnItemClickListener(){

                    @Override
                    public void onItemClick(View v, int pos) {
                        Intent intent;
                        ArrayList<Object> arr = (ArrayList<Object>)mCalendarList.get(pos);
                        GregorianCalendar gc = (GregorianCalendar)arr.get(0);
                        System.out.println("선택한 날짜를 출력합니다. "+ gc.get(Calendar.YEAR)+gc.get(Calendar.MONTH)+gc.get(Calendar.DAY_OF_MONTH));
                        SimpleDateFormat fmt = new SimpleDateFormat("yyyy-MM-dd");
                        fmt.setCalendar(gc);
                        String fm = fmt.format(gc.getTime());
                        if(date.contains(fm)) {
//                            HashMap<String,String> hm = new HashMap<String,String>();
                            int idx = date.indexOf(fm);
//                            hm = booklist.get(idx);
                            intent = new Intent(getContext(), ShowCalendarPost.class);
                            intent.putExtra("calbook_list",booklist);
                            intent.putExtra("pos",idx);
//                            intent.putExtra("cPostInfo",hm);
                            startActivity(intent);
                        }else{
                            intent = new Intent(getContext(), PostCalendarAvtivity.class);
                            intent.putExtra("date",gc);
                            startActivity(intent);
                        }

                    }
                }
        );

        calendarAdapter.setCalendarList(mCalendarList);
        recyclerView.setLayoutManager(manager);
        recyclerView.setAdapter(calendarAdapter);

        if (mCenterPosition >= 0) {
            recyclerView.scrollToPosition(mCenterPosition);
        }
    }

    public void setCalendarList(GregorianCalendar cal,ArrayList<String> date, ArrayList<String> image) {

            //setTitle(cal.getTimeInMillis());
            ArrayList<Object> calendarList = new ArrayList<>();

            //for (int i = -300; i < 300; i++) {
            try {
                GregorianCalendar calendar = new GregorianCalendar(cal.get(Calendar.YEAR), cal.get(Calendar.MONTH)+i , 1, 0, 0, 0);
//                if (i == 0) {
//                    mCenterPosition = calendarList.size();
//                }

                // 타이틀인듯
                calendarList.add(calendar.getTimeInMillis());

                int dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK) - 1; //해당 월에 시작하는 요일 -1 을 하면 빈칸을 구할 수 있겠죠 ?
                int max = calendar.getActualMaximum(Calendar.DAY_OF_MONTH); // 해당 월에 마지막 요일

                // EMPTY 생성
                for (int j = 0; j < dayOfWeek; j++) {
                    calendarList.add(DateUtil.Keys.EMPTY);
                }
                for (int j = 1; j <= max; j++) {
                    GregorianCalendar gc = new GregorianCalendar(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), j);
                    SimpleDateFormat fmt = new SimpleDateFormat("yyyy-MM-dd");
                    fmt.setCalendar(gc);
                    String fm = fmt.format(gc.getTime());

                    //calendar객체랑 이미지를 담을 배열리스트
                    ArrayList<Object> array = new ArrayList<>();
                    array.add(gc);
                    System.out.println("독서달력 date 배열 : "+date);
                    System.out.println("독서달력 img 배열 : "+image);

                    System.out.println("Fm : "+fm);
                    if(date.contains(fm)){
                        int idx = date.indexOf(fm);
                        array.add(image.get(idx));
                        System.out.println("------------------");
                        System.out.println("date : "+date.get(idx));
                        System.out.println("image : "+image.get(idx));
                    }
                    calendarList.add(array);
                }

                //calendarList.add(calendar);
                // TODO : 결과값 넣을떄 여기다하면될듯

            } catch (Exception e) {
                e.printStackTrace();
            }
            //}

            mCalendarList = calendarList;
            setRecycler();
    }
}
