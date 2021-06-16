package com.example.mobilesw.model;

import com.example.mobilesw.info.DateUtil;

import java.util.Calendar;

public class Day extends ViewModel {

    String day;
    String img;

    public Day() {
    }

    public String getDay() {
        return day;
    }

    public void setDay(String day) {
        this.day = day;
    }

    public String getImg(){return img; }
    public void setImg(String img){this.img=img;}

    // TODO : day에 달력일값넣기
    public void setCalendar(Calendar calendar){

        day = DateUtil.getDate(calendar.getTimeInMillis(), DateUtil.DAY_FORMAT);

    }



}

