package com.asifapps.oxforddigitalattendance.Database.Entities;

import android.arch.persistence.room.Entity;
import android.arch.persistence.room.Index;
import android.arch.persistence.room.PrimaryKey;

import java.util.Date;

@Entity(indices = {@Index(value = {"Rno", "Class"}, unique = true)})
public class Student {
    @PrimaryKey(autoGenerate = true)
    public int StdId;

    public String Rno;
    public String Name;
    public String FatherName;
    public String Phone;
    public String Class;
    public String BloodGroup;
    public String DOB;
    public Date RegDate;
}
