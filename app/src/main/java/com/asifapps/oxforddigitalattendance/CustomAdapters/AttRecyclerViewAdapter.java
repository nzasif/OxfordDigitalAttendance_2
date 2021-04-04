package com.asifapps.oxforddigitalattendance.CustomAdapters;

import android.content.Context;
import android.graphics.Color;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

import com.asifapps.oxforddigitalattendance.Database.Entities.Attendance;
import com.asifapps.oxforddigitalattendance.Database.ModelClasses.StdAttendance;
import com.asifapps.oxforddigitalattendance.Interfaces.IAttendanceClickEvent;
import com.asifapps.oxforddigitalattendance.R;

import java.util.List;

public class AttRecyclerViewAdapter extends RecyclerView.Adapter<AttRecyclerViewAdapter.AttendanceViewHolder> {

    private List<Attendance> attendances;

    private IAttendanceClickEvent attendanceClickEvent;

    private Context context;

    public AttRecyclerViewAdapter(IAttendanceClickEvent clickEvent, List<Attendance> list) {
        attendances = list;
        attendanceClickEvent = clickEvent;
    }

    @Override
    public AttendanceViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.att_list_item, parent, false);

        context = this.context;

        return new AttendanceViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull AttendanceViewHolder holder, final int position) {
        Attendance att = attendances.get(position);

        holder.name.setText(att.Name);
        holder.phone.setText(att.Phone);
        holder.rno.setText(Integer.toString(att.Rno));

        holder.EntranceTime.setText(att.EntranceTime);
        holder.LeaveTime.setText(att.LeaveTime);

        if (att.EntranceMsgSent) {
            holder.FirstTimeMsg.setText("1st Time Msg sent");
            holder.FirstTimeMsg.setTextColor(Color.BLUE);

            holder.fMsgBtn.setText("alreday sent");
        } else {
            holder.FirstTimeMsg.setText("1st Time Msg is NOT Sent");
            holder.FirstTimeMsg.setTextColor(Color.RED);

            holder.fMsgBtn.setText("Send");
        }

        if (att.LeaveMsgSent) {
            holder.SecondTimeMsg.setText("2nd Time Msg is sent");
            holder.SecondTimeMsg.setTextColor(Color.BLUE);
            holder.sMsgBtn.setText("Sent");

        } else {
            holder.SecondTimeMsg.setText("2nd Time Msg is NOT Sent");
            holder.SecondTimeMsg.setTextColor(Color.RED);
            holder.sMsgBtn.setText("Send");
        }

        holder.attReview.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                attendanceClickEvent.onClickReview(attendances.get(position));
                // Toast.makeText(this.context, position, Toast.LENGTH_LONG);
            }
        });

        holder.fMsgBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                attendanceClickEvent.onClickFirstTimeMsg(attendances.get(position));
                // Toast.makeText(this.context, position, Toast.LENGTH_LONG);
            }
        });

        holder.sMsgBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                attendanceClickEvent.onClickSecTimeMsg(attendances.get(position));
                // Toast.makeText(this.context, position, Toast.LENGTH_LONG);
            }
        });

        holder.attDel.setOnLongClickListener(new View.OnLongClickListener(){

            @Override
            public boolean onLongClick(View v) {
                attendanceClickEvent.onClickDel(attendances.get(position));
                return false;
            }
        });
    }

    @Override
    public int getItemCount() {
        return attendances.size();
    }

    // the view holder
    public class AttendanceViewHolder extends RecyclerView.ViewHolder {
        public TextView rno;
        public TextView name;
        public TextView phone;

        public TextView EntranceTime;
        public TextView LeaveTime;

        public TextView FirstTimeMsg;
        public TextView SecondTimeMsg;

        public TextView AttStatus;

        public ImageButton attReview;
        public ImageButton attDel;

        public Button fMsgBtn;
        public Button sMsgBtn;

        public AttendanceViewHolder(@NonNull View itemView) {
            super(itemView);

            rno = itemView.findViewById(R.id.attRno);
            name = itemView.findViewById(R.id.attName);
            phone = itemView.findViewById(R.id.attPhone);

            EntranceTime = itemView.findViewById(R.id.entranceTime);
            LeaveTime = itemView.findViewById(R.id.leaveTime);

            attReview = itemView.findViewById(R.id.attReview);
            attDel = itemView.findViewById(R.id.attDel);

            FirstTimeMsg = itemView.findViewById(R.id.firstTimeMsgStatus);
            SecondTimeMsg = itemView.findViewById(R.id.secondTimeMsgStatus);

            fMsgBtn = itemView.findViewById(R.id.fMsgBtn);
            sMsgBtn = itemView.findViewById(R.id.sMsgBtn);
        }

        public void setName(String name) {
            this.name.setText(name);
        }

        public void setPhone(String phone) {
            this.phone.setText(phone);
        }
    }
}
