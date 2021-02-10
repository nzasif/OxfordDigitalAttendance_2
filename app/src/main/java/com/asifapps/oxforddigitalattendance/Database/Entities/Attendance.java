package com.asifapps.oxforddigitalattendance.Database.Entities;

import android.arch.persistence.room.Entity;
import android.arch.persistence.room.ForeignKey;
import android.arch.persistence.room.Ignore;
import android.arch.persistence.room.Index;
import android.arch.persistence.room.PrimaryKey;

import java.util.Date;

@Entity(
        indices = {@Index(value = {"AttDate", "StdId_FK"})},
        foreignKeys = @ForeignKey(
        entity = Student.class,
        parentColumns = "StdId",
        childColumns = "StdId_FK",
        onDelete = ForeignKey.CASCADE
))
public class Attendance {
    @PrimaryKey(autoGenerate = true)
    public int AttId;

    public String EntranceTime;
    public String LeaveTime;
    // A, L, P
    public String AttStatus;
    public String AttDate;
    public boolean EntranceMsgSent;
    public boolean LeaveMsgSent;

    public int StdId_FK;
    public String Name;
    public String Class;
    public String Rno;
    public  String Phone;
}
