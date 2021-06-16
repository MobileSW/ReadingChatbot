package com.example.mobilesw.model;

import com.example.mobilesw.info.DateUtil;
import com.example.mobilesw.model.ViewModel;

public class CalendarHeader extends ViewModel {

    String header;
    long mCurrentTime;

    public CalendarHeader() {
    }

    public String getHeader() {
        return header;
    }

    public void setHeader(long time) {

        String value = DateUtil.getDate(time, DateUtil.YEAR_MONTH_FORMAT);
        this.header = value;

    }

    public void setHeader(String header) {
        this.header = header;

    }

}