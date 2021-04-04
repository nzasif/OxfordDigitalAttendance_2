package com.asifapps.oxforddigitalattendance.CustomAdapters;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;


import com.asifapps.oxforddigitalattendance.Database.Entities.Student;
import com.asifapps.oxforddigitalattendance.Interfaces.IStudentClickEvent;
import com.asifapps.oxforddigitalattendance.R;

import java.util.ArrayList;
import java.util.List;

public class StudentsRecyclerViewAdapter extends RecyclerView.Adapter<StudentsRecyclerViewAdapter.StudentViewHolder> {

    private List<Student> students;

    private IStudentClickEvent studentClickEvent;

    private Context context;

    public StudentsRecyclerViewAdapter(IStudentClickEvent clickEvent, List<Student> list) {
        students = list;
        studentClickEvent = clickEvent;
    }

    @Override
    public StudentViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.std_list_item, parent, false);

        context = this.context;

        return new StudentViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull StudentViewHolder holder, final int position) {
        Student std = students.get(position);

        holder.setRno(Integer.toString(std.Rno));
        holder.setName(std.Name);
        holder.setPhone(std.Phone);

        holder.del.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                studentClickEvent.onClickDelStudent(students.get(position));
                return false;
            }
        });

        holder.edit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                studentClickEvent.onClickEditStudent(students.get(position));
                // Toast.makeText(this.context, position, Toast.LENGTH_LONG);
            }
        });
    }

    @Override
    public int getItemCount() {
        return students.size();
    }

    // the view holder
    public class StudentViewHolder extends RecyclerView.ViewHolder {
        private TextView rno;
        private TextView name;
        private TextView phone;

        private ImageButton del;
        private ImageButton edit;

        public StudentViewHolder(@NonNull View itemView) {
            super(itemView);

            rno = itemView.findViewById(R.id.rnoText);
            name = itemView.findViewById(R.id.nameText);
            phone = itemView.findViewById(R.id.phoneText);

            del = itemView.findViewById(R.id.del);
            edit = itemView.findViewById(R.id.edit);
        }

        public void setName(String name) {
            this.name.setText(name);
        }

        public void setRno(String rno) {
            this.rno.setText(rno);
        }

        public void setPhone(String phone) {
            this.phone.setText(phone);
        }
    }

}
