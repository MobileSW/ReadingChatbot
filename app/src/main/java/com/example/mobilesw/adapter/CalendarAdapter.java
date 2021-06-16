package com.example.mobilesw.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModel;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;

import com.bumptech.glide.Glide;
import com.example.mobilesw.R;
import com.example.mobilesw.model.CalendarHeader;
import com.example.mobilesw.model.Day;
import com.example.mobilesw.model.EmptyDay;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;

public class CalendarAdapter extends RecyclerView.Adapter {
    private final int HEADER_TYPE = 0;
    private final int EMPTY_TYPE = 1;
    private final int DAY_TYPE = 2;

    private List<Object> mCalendarList;

    private Context mContext;


    public CalendarAdapter(Context context,List<Object> calendarList) {
        mContext = context;
        mCalendarList = calendarList;
    }

    public void setCalendarList(List<Object> calendarList) {
        mCalendarList = calendarList;
        notifyDataSetChanged();
    }

    @Override
    public int getItemViewType(int position) { //뷰타입 나누기
        Object item = mCalendarList.get(position);
        if (item instanceof Long) {
            return HEADER_TYPE; //날짜 타입
        } else if (item instanceof String) {
            return EMPTY_TYPE; // 비어있는 일자 타입
        } else {
            return DAY_TYPE; // 일자 타입

        }
    }


    // viewHolder 생성
    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        LayoutInflater inflater = LayoutInflater.from(parent.getContext());

        // 날짜 타입
        if (viewType == HEADER_TYPE) {

            HeaderViewHolder viewHolder = new HeaderViewHolder(inflater.inflate(R.layout.item_calendar_header, parent, false));

            StaggeredGridLayoutManager.LayoutParams params = (StaggeredGridLayoutManager.LayoutParams)viewHolder.itemView.getLayoutParams();
            params.setFullSpan(true); //Span을 하나로 통합하기
            viewHolder.itemView.setLayoutParams(params);

            return viewHolder;

            //비어있는 일자 타입
        } else if (viewType == EMPTY_TYPE) {
            return new EmptyViewHolder(inflater.inflate(R.layout.item_day_empty, parent, false));

        }
        // 일자 타입
        else {
            return new DayViewHolder(inflater.inflate(R.layout.item_day, parent, false));

        }

    }

    // 데이터 넣어서 완성시키는것
    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder viewHolder, int position) {
        int viewType = getItemViewType(position);

        /**날짜 타입 꾸미기*/
        /** EX : 2018년 8월*/
        if (viewType == HEADER_TYPE) {
            HeaderViewHolder holder = (HeaderViewHolder) viewHolder;
            Object item = mCalendarList.get(position);
            CalendarHeader model = new CalendarHeader();

            // long type의 현재시간
            if (item instanceof Long) {
                // 현재시간 넣으면, 2017년 7월 같이 패턴에 맞게 model에 데이터들어감.
                model.setHeader((Long) item);
            }
            // view에 표시하기
            holder.bind(model);
        }
        /** 비어있는 날짜 타입 꾸미기 */
        /** EX : empty */
        else if (viewType == EMPTY_TYPE) {
            EmptyViewHolder holder = (EmptyViewHolder) viewHolder;
            EmptyDay model = new EmptyDay();
            holder.bind(model);
        }
        /** 일자 타입 꾸미기 */
        /** EX : 22 */
        else if (viewType == DAY_TYPE) {
            DayViewHolder holder = (DayViewHolder) viewHolder;
            Object item = mCalendarList.get(position);
            ArrayList<Object> arr = new ArrayList<Object>();
            arr = (ArrayList<Object>) item;

            Day model = new Day();
            if (arr.get(0) instanceof Calendar) {
                // Model에 Calendar값을 넣어서 몇일인지 데이터 넣기
                model.setCalendar((Calendar) arr.get(0));
                if(arr.size()>1){
                    model.setImg(arr.get(1).toString());
                }
            }
            // Model의 데이터를 View에 표현하기
            holder.bind(model);
        }
    }

    // 개수구하기
    @Override
    public int getItemCount() {
        if (mCalendarList != null) {
            return mCalendarList.size();
        }
        return 0;
    }



    /** viewHolder */
    private class HeaderViewHolder extends RecyclerView.ViewHolder { //날짜 타입 ViewHolder

        TextView itemHeaderTitle;

        public HeaderViewHolder(@NonNull View itemView) {
            super(itemView);

            initView(itemView);
        }


        public void initView(View v){

            itemHeaderTitle = (TextView)v.findViewById(R.id.item_header_title);

        }

        public void bind(CalendarHeader model){

            // 일자 값 가져오기
            String header = ((CalendarHeader)model).getHeader();
            System.out.println("header : "+header);

            // header에 표시하기, ex : 2018년 8월
            itemHeaderTitle.setText(header);


        };
    }


    private class EmptyViewHolder extends RecyclerView.ViewHolder { // 비어있는 요일 타입 ViewHolder


        public EmptyViewHolder(@NonNull View itemView) {
            super(itemView);

            initView(itemView);
        }

        public void initView(View v){

        }

        public void bind(EmptyDay model){


        };
    }

    public interface OnItemClickListener{
        void onItemClick(View v,int pos);
    }
    private CalendarAdapter.OnItemClickListener mListener = null;
    public void setOnItemClickListener(CalendarAdapter.OnItemClickListener listener){
        this.mListener = listener;
    }

    // TODO : item_day와 매칭
    private class DayViewHolder extends RecyclerView.ViewHolder {// 요일 입 ViewHolder

        TextView itemDay;
        ImageView itemImage;

        public DayViewHolder(@NonNull View itemView) {
            super(itemView);

            initView(itemView);

        }

        public void initView(View v){

            itemDay = (TextView)v.findViewById(R.id.item_day);
            itemImage = (ImageView)v.findViewById(R.id.item_image);

            v.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int pos = getBindingAdapterPosition();
                    if(pos!= RecyclerView.NO_POSITION){
                        if(mListener!=null){
                            mListener.onItemClick(v,pos);
                        }
                    }
                }
            });

        }

        public void bind(Day model){

            // 일자 값 가져오기
            String day = ((Day)model).getDay();
            String img = ((Day)model).getImg();

            // 일자 값 View에 보이게하기
            itemDay.setText(day);
            if(img!=null){
                //Picasso.get().load(image).into(holder.img);
                Glide.with(mContext).load(img).into(itemImage);
            }
        };
    }

}