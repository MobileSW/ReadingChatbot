package com.example.mobilesw;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

import androidx.core.app.NotificationCompat;

import com.example.mobilesw.R;
import com.example.mobilesw.activity.MainActivity;
import com.example.mobilesw.fragment.FragAlarm;
import com.example.mobilesw.fragment.FragHome;
import com.google.common.primitives.Booleans;

import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class AlarmReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {


        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        Intent notificationIntent = new Intent(context, MainActivity.class);

        notificationIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP
                | Intent.FLAG_ACTIVITY_SINGLE_TOP);

        int ind = intent.getIntExtra("index", 0);
        boolean[] week = intent.getBooleanArrayExtra("weekday");

//        System.out.println("--------------------------");
//        for(int i=0;i<week.length;i++){
//            System.out.println("week"+i+":"+week[i]+" ");
//        }


        Calendar calendar = Calendar.getInstance();
//        System.out.println("선택한 요일 :"+ calendar.get(Calendar.DAY_OF_WEEK));
//        System.out.println("들어있는가?"+ Booleans.contains(week,true));


        if(!week[calendar.get(Calendar.DAY_OF_WEEK)]&&(Booleans.contains(week,true))) return; //체크한 요일이 아니면

        PendingIntent pendingI = PendingIntent.getActivity(context, ind, notificationIntent, 0);


        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, "default");


        //OREO API 26 이상에서는 채널 필요
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {

            builder.setSmallIcon(R.drawable.ic_launcher_foreground); //mipmap 사용시 Oreo 이상에서 시스템 UI 에러남


            String channelName ="독서시간 알람 채널";
            String description = "독서 시간을 알려줍니다.";
            int importance = NotificationManager.IMPORTANCE_HIGH; //소리와 알림메시지를 같이 보여줌

            NotificationChannel channel = new NotificationChannel("default", channelName, importance);
            channel.setDescription(description);

            if (notificationManager != null) {
                // 노티피케이션 채널을 시스템에 등록
                notificationManager.createNotificationChannel(channel);
            }
        }else builder.setSmallIcon(R.mipmap.main_icon); // Oreo 이하에서 mipmap 사용하지 않으면 Couldn't create icon: StatusBarIcon 에러남


        String hour = String.valueOf(calendar.get(Calendar.HOUR_OF_DAY));
        String min = String.valueOf(calendar.get(Calendar.MINUTE));

        System.out.println("알람이 울린 요일 : "+calendar.get(Calendar.DAY_OF_WEEK));

        builder.setAutoCancel(true)
                .setDefaults(NotificationCompat.DEFAULT_ALL)
                .setWhen(System.currentTimeMillis())
                .setTicker("{Time to watch some cool stuff!}")
                .setContentTitle("약속 예정 알림")
                .setContentText("책 읽을 시간이야!")
                .setContentInfo("INFO")
                .setContentIntent(pendingI);

        if (notificationManager != null) {

            // 노티피케이션 동작시킴
            notificationManager.notify(ind, builder.build());


            Calendar nextNotifyTime = Calendar.getInstance();

            // 내일 같은 시간으로 알람시간 결정
            nextNotifyTime.add(Calendar.DATE, -1);

            Date currentDateTime = nextNotifyTime.getTime();
            String date_text = new SimpleDateFormat("yyyy년 MM월 dd일 EE요일 a hh시 mm분 ", Locale.getDefault()).format(currentDateTime);
            Toast.makeText(context.getApplicationContext(), date_text +"에 알람이 설정되었습니다!", Toast.LENGTH_SHORT).show();

        }
    }
}
