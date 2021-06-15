package com.example.mobilesw.fragment;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import com.example.mobilesw.R;
import com.example.mobilesw.activity.LoginActivity;
import com.example.mobilesw.activity.MainActivity;
import com.example.mobilesw.adapter.ChatAdapter;
import com.example.mobilesw.info.ChatItem;
import com.example.mobilesw.info.PostInfo;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;

public class FragHome extends Fragment {
    FirebaseUser user;
    private FirebaseDatabase firebaseDatabase;
    private DatabaseReference chatRef;
    private String userId;

    private Button msgBtn;
    private Button menuBtn;
    private EditText et;
    private ListView listView;
    private BottomNavigationView chatNavi;

    private FragmentManager childFragment;

    private ArrayList<ChatItem> messageItems=new ArrayList<>();
    private ChatAdapter adapter;
    private ImageView mainIcon;
    private Handler questionHandler;
    private Handler reportHandler;
    private int reportNum = 0;
    private int questionNum = 0;
    private boolean isQuestion = false;
    private boolean isReport = false;
    private boolean isRandomChat = false;
    private boolean isBookReport = false;
    private String book_title="";
    private String book_image="";
    private ArrayList<String> questionList = new ArrayList<>(Arrays.asList("어떤 내용의 책이야?"));
    private ArrayList<String> reportList = new ArrayList<>(Arrays.asList("어떤 내용의 책이야?", "책을 읽고 느낀점을 말해줘", "책을 한마디로 표현하자면?"));
    private ArrayList<String> answerList = new ArrayList<>();

    SharedPreferences sp;

    public static FragHome newInstance(){
        return new FragHome();
    }

    public FragHome() { }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.frag_home, container, false);

        msgBtn = (Button) view.findViewById(R.id.msgBtn);
        menuBtn = view.findViewById(R.id.chatmenuBtn);
        et=view.findViewById(R.id.et);
        listView=view.findViewById(R.id.listView);
        listView.setVisibility(listView.INVISIBLE);
        chatNavi = view.findViewById(R.id.chatMenuNavi);
        mainIcon = view.findViewById(R.id.main_charactor);



        user = FirebaseAuth.getInstance().getCurrentUser();
        userId = user.getUid();
        firebaseDatabase= FirebaseDatabase.getInstance();
        chatRef= (DatabaseReference) firebaseDatabase.getReference("chat").child(userId);
        Query chatQuery = chatRef.orderByChild("timestamp");

        System.out.println("suhyun chat "+userId);
        //RealtimeDB에서 채팅 메세지들 실시간 읽어오기
        //'chat'노드에 저장되어 있는 데이터들을 읽어오기 > 데이터가 추가되면 작동하는 리스너
        chatQuery.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                childAdded(dataSnapshot);
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {}

            @Override
            public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {
                adapter.notifyDataSetChanged();
                listView.setSelection(0);
            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {}

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {}
        });

        adapter=new ChatAdapter(messageItems,getLayoutInflater());
        if(messageItems.size() == 0)
            mainIcon.setVisibility(View.VISIBLE);
        listView.setAdapter(adapter);
        listView.setVisibility(listView.VISIBLE);
        chatNavi.setVisibility(View.VISIBLE);


        msgBtn.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v) { clickSend(v); }
        });

        // + 버튼 클릭시 채팅 메뉴 Toggle
        menuBtn.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                if(chatNavi.getVisibility() == View.GONE){
                    chatNavi.setVisibility(View.VISIBLE);
                }else{
                    chatNavi.setVisibility(View.GONE);
                }
            }
        });

        // 채팅 메뉴 Navigation 클릭 시 행동
        chatNavi.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.chat_timer:
                        sendMsg("지금부터 읽은 시간을 측정할게 시~작!","bot");
                        setUI();
                        //타이머 뷰 visible
                        //timer_layout.setVisibility(view.VISIBLE);
                        childFragment = getChildFragmentManager();
                        new Handler().postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                childFragment.beginTransaction().add(R.id.chat_layout,FragTimer.getInstance(0))
                                        .addToBackStack(null).commit();
                            }
                        },700);
                        return true;
                    case R.id.chat_alarm:
                        sendMsg("알람을 설정할 시간을 입력해줘!","bot");
                        setUI();
                        childFragment = getChildFragmentManager();
                        new Handler().postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                childFragment.beginTransaction().add(R.id.chat_layout,FragAlarm.getInstance(0))
                                        .addToBackStack(null).commit();
                            }
                        },700);
                        return true;
                    case R.id.chat_question:
                        setUI();
                        randomQuestion();
                        return true;

                    case R.id.chat_report:
                        setUI();
                        setBundle("report", reportHandler);
                        return true;

                    case R.id.chat_delete:
                        chatRef.removeValue();
                        messageItems.clear();
                        adapter=new ChatAdapter(messageItems,getLayoutInflater());
                        listView.setAdapter(adapter);
                        mainIcon.setVisibility(View.VISIBLE);

                }
                return false;
            }
        });

        questionHandler =new Handler(){
            @Override
            public void handleMessage(Message msg) {
                Bundle bd = msg.getData();
                // 대답과 질문을 번갈아서 하기 위해 Handler 사용
                Boolean question = bd.getBoolean("question");
                String answer = bd.getString("answer");

                // 랜덤 질문 상태면 사용자 채팅 허용
                if (question) {
                    isQuestion = true;
                    msgBtn.setEnabled(true);
                }

                if(answer!=null) {
                    String answerStr= questionList.get(questionNum-2);
                    switch (answerStr) {
                        case "어떤 내용의 책이야?":
                            answerList.add("책 줄거리: "+answer);
                            break;
                        case "내가 주인공이었으면 어떻게 했을까?":
                            answerList.add("내가 주인공이었다면: "+answer);
                            break;
                        case "가장 기억에 남는 부분은 뭐야?":
                            answerList.add("가장 인상깊은 장면: "+answer);
                            break;
                        case "주인공에게 본받을 점은?":
                            answerList.add("주인공에게 본받을 점: "+answer);
                            break;
                        case "어떤 등장인물이 좋았고 왜 좋았는지 알려줘~":
                            answerList.add("가장 좋았던 인물과 좋은 이유: "+answer);
                            break;
                        case "새롭게 알게 된 점은 뭐야?":
                            answerList.add("새롭게 알게 된 점: "+answer);
                            break;
                    }
                }

                // 독후감 작성 상태라면 독후감에 필요한 양식을 채팅봇 메세지로 설정함
                if (questionNum == 0) {
                    // "어떤 책을 읽었는지 선택해줘" 책 선택 화면과 연결
                    ((MainActivity)getActivity()).replaceFragment(FragSearch.newInstance());
                    Bundle bundle = new Bundle();
                    bundle.putBoolean("isRandomChat",true);
                    getParentFragmentManager().setFragmentResult("chat",bundle);

                } else if(questionNum == 4) {
                    // 질문 종료
                    questionNum = 0;
                    msgBtn.setEnabled(false);
                    sendMsg("얘기해줘서 고마워^^ 네 얘기는 독후감 페이지에 정리했어", "bot");
                    isQuestion = false;
                    uploadReport();
                    Handler handler = new Handler();
                    handler.postDelayed(new Runnable(){
                        @Override
                        public void run() {
                            startIntent();
                        }
                    },1500);
                } else {
                    sendMsg(questionList.get(questionNum-1), "bot");
                    questionNum++;
                }
            }

        };

        reportHandler =new Handler() {
            @RequiresApi(api = Build.VERSION_CODES.O)
            @Override
            public void handleMessage (Message msg){
                Bundle bd = msg.getData();
                // 대답과 질문을 번갈아서 하기 위해 Handler 사용
                Boolean report = bd.getBoolean("report");
                String answer = bd.getString("answer");

                System.out.println("soopy"+reportNum);

                // 랜덤 질문 상태인지 독후감 작성 상태인지 기록, 사용자 채팅 허용
                if (report) {
                    isReport = true;
                    msgBtn.setEnabled(true);
                }

                if(answer!=null) {
                    switch (reportNum-1) {
                        case 1:
                            answerList.add("책 줄거리: "+answer);
                            break;
                        case 2:
                            answerList.add("책을 읽은 후 느낀점: "+answer);
                            break;
                        case 3:
                            answerList.add("이 책은 한 마디로: "+answer);
                            break;
                    }

                }

                // 독후감 작성 상태라면 독후감에 필요한 양식을 채팅봇 메세지로 설정함
                if (reportNum == 0) {
                    // "어떤 책을 읽었는지 선택해줘" 책 선택 화면과 연결
                    ((MainActivity)getActivity()).replaceFragment(FragSearch.newInstance());
                    Bundle bundle = new Bundle();
                    bundle.putBoolean("isBookReport",true);
                    getParentFragmentManager().setFragmentResult("chat",bundle);

                } else if(reportNum == 4) {
                    // 질문 종료
                    reportNum = 0;
                    msgBtn.setEnabled(false);
                    sendMsg("얘기해줘서 고마워^^ 네 얘기는 독후감 페이지에 정리했어", "bot");
                    isReport = false;
                    uploadReport();
                    Handler handler = new Handler();
                    handler.postDelayed(new Runnable(){
                        @Override
                        public void run() {
                            startIntent();
                        }
                    },1500);
                } else {
                    sendMsg(reportList.get(reportNum-1), "bot");
                    reportNum++;
                }
            }
        };

        if(getArguments()!=null){
            isRandomChat = getArguments().getBoolean("isRandomChat");
            isBookReport = getArguments().getBoolean("isBookReport");
            book_title = getArguments().getString("book_title");
            book_image = getArguments().getString("book_image");
            System.out.println("soopy"+book_image);
            System.out.println("랜덤챗으로 다시 돌아왔는가?" + isRandomChat);
            if(isRandomChat){
                questionNum=1;
                sp = getContext().getSharedPreferences("sp", Context.MODE_PRIVATE);
                String name = sp.getString("name", "");
                System.out.println("이름 : "+name);
                String str = name+"이(가) 선택한 책은 "+book_title+"(이)야";
                setUI();
                sendMsg(str,"bot");

                // 이전 대화가 완료되지 않았을 때 남아있는 데이터 제거
                if(answerList.size() != 0)
                    answerList.clear();
                answerList.add("책 제목: " + book_title);
                randomQuestion();
            }
            if(isBookReport){
                reportNum =1;
                sp = getContext().getSharedPreferences("sp", Context.MODE_PRIVATE);
                String name = sp.getString("name", "");
                System.out.println("이름 : "+name);
                String str = name+"이(가) 선택한 책은 "+book_title+"(이)야";
                setUI();
                sendMsg(str,"bot");

                // 이전 대화가 완료되지 않았을 때 남아있는 데이터 제거
                if(answerList.size() != 0)
                    answerList.clear();
                setBundle("report", reportHandler);
                answerList.add("책 제목: " + book_title);
            }
        }

        return view;
    }

    // 새로운 채팅이 추가 됐을 때 화면에 표시하는 메서드
    public void childAdded(DataSnapshot dataSnapshot) {
        //새로 추가된 데이터(값 : MessageItem객체) 가져오기
        ChatItem messageItem= dataSnapshot.getValue(ChatItem.class);

        Calendar cal = Calendar.getInstance();
        Calendar temp = Calendar.getInstance();

        // 메세지가 없는 경우 : 채팅봇 메세지가 처음 나오는 경우 & 날짜 받아오는 경우
        if(messageItems.size() == 0){
            cal.setTimeInMillis(messageItem.getTimestamp());
            int month = cal.get(Calendar.MONTH);
            int day = cal.get(Calendar.DAY_OF_MONTH);

            ChatItem date = new ChatItem();
            int nyear = cal.get(Calendar.YEAR);
            date.setId("date");
            date.setTime(nyear + "년 " + (month + 1) + "월 " + day + "일");
            messageItems.add(date);

            //새로운 메세지를 리스트뷰에 추가하기 위해 ArrayList에 추가
            messageItems.add(messageItem);
            adapter.notifyDataSetChanged();
            mainIcon.setVisibility(View.INVISIBLE);

        }else{
            System.out.println(messageItems);
            if(messageItems.get(messageItems.size() - 1).getTimestamp() == null){
                //새로운 메세지를 리스뷰에 추가하기 위해 ArrayList에 추가
                messageItems.add(messageItem);
                adapter.notifyDataSetChanged();
                listView.setSelection(messageItems.size()-1);

            }else {

                cal.setTimeInMillis(messageItems.get(messageItems.size() - 1).getTimestamp());
                int year = cal.get(Calendar.YEAR);
                int month = cal.get(Calendar.MONTH);
                int day = cal.get(Calendar.DAY_OF_MONTH);

                temp.setTimeInMillis(messageItem.getTimestamp());
                int nyear = temp.get(Calendar.YEAR);
                int nmonth = temp.get(Calendar.MONTH);
                int nday = temp.get(Calendar.DAY_OF_MONTH);

                // 마지막 메세지보다 날짜가 지난 경우
                if ((year < nyear) || (month < nmonth) || (month == nmonth && day < nday)) {
                    ChatItem date = new ChatItem();
                    date.setId("date");
                    date.setTime(nyear + "년 " + (nmonth + 1) + "월 " + nday + "일");
                    messageItems.add(date);
                }
                //새로운 메세지를 리스뷰에 추가하기 위해 ArrayList에 추가
                messageItems.add(messageItem);
                adapter.notifyDataSetChanged();
                listView.setSelection(messageItems.size() - 1);
            }
        }
    }

    // 전송 버튼 누른 후: 입력한 내용 DB에 저장, 화면 표시
    public void clickSend(View view) {
        String message= et.getText().toString();
        if(msgBtn.isEnabled() && !message.equals("")){
            // EditText의 문자열 DB에 전송
            sendMsg(message, "user");
            System.out.println("Flag"+isQuestion+isReport);

            //소프트키패드 안보이도록
            InputMethodManager imm=(InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(getActivity().getCurrentFocus().getWindowToken(),0);


            Bundle bd = new Bundle();
            bd.putString("answer", message);
            if(isQuestion) {
                sendBundle(bd, questionHandler);
            }
            if(isReport) {
                sendBundle(bd, reportHandler);
            }

        }
        System.out.println("click"+msgBtn.isEnabled());

        //EditText에 있는 글씨 지우기
        et.setText("");
    }


    public void sendMsg(String message, String type) {
        SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm");
        ChatItem messageItem;
        //메세지 작성 시간 문자열로
        Calendar calendar= Calendar.getInstance();
        String time = timeFormat.format(calendar.getTime());
        Date now = new Date();
        Long timestamp = now.getTime();

        if(type.equals("bot")){
            //DB에 저장할 값들(닉네임, 메세지, 시간)
            messageItem= new ChatItem("bot", message, time, timestamp);
        }else{
            messageItem= new ChatItem("user", message, time, timestamp);
        }

        //DB에 메세지 등록
        chatRef.push().setValue(messageItem);

    }

    // 독후감 작성을 위한 랜덤 질문 생성
    public void randomQuestion() {
        // 랜덤 질문 리스트
        String randomQuestions[] = { "내가 주인공이었으면 어떻게 했을까?", "가장 기억에 남는 부분은 뭐야?", "주인공에게 본받을 점은?", "어떤 등장인물이 좋았고 왜 좋았는지 알려줘~", "새롭게 알게 된 점은 뭐야?"};

        ArrayList<Integer> ranNum = new ArrayList<Integer>();

        // 인덱스 번호를 이용해 랜덤 질문 4개 중에 2개를 뽑음
        for(int i=0; i<=3; i++) {
            ranNum.add(i);
        }
        Collections.shuffle(ranNum);

        for(int i=0; i<=1; i++){
            questionList.add(randomQuestions[ranNum.get(i)]);
        }

        setBundle("question", questionHandler);

    }

    public void uploadReport() {
        FirebaseFirestore firestore = FirebaseFirestore.getInstance();
        DocumentReference docRef = firestore.collection("posts").document();
        FirebaseAuth auth = FirebaseAuth.getInstance();

        // 사진
        ArrayList<String> contents = new ArrayList<String>();

        // 글
        String answerStr = "";
        for (String s : answerList)
        {
            answerStr += s + "\n";
        }

        if(book_image!= null){
            contents.add(book_image);
        }
        System.out.println("soopy"+answerStr);

        PostInfo postInfo = new PostInfo(book_title+" 독후감", answerStr, contents, auth.getUid() ,new Date());
        docRef.set(postInfo.getPostInfo())
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        answerList.clear();
                        book_image = "";
                        book_title = "";
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                    }
                });

    }

    // Handler에 번들 msg 전달
    public void setBundle(String msg, Handler handler) {
        Bundle bd = new Bundle();
        bd.putBoolean(msg, true);
        sendBundle(bd, handler);

    }

    public void sendBundle(Bundle bd, Handler handler) {
        Message msg = handler.obtainMessage();
        msg.setData(bd);
        handler.sendMessage(msg);
    }

    public void setUI() {
        chatNavi.setVisibility(View.GONE);
    }

    private void startIntent() {
        Intent intent= new Intent(getActivity(),MainActivity.class);;
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        intent.putExtra("fragnum",3);
        startActivity(intent);
    }


}
