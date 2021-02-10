package com.asifapps.oxforddigitalattendance.Database.ModelClasses;

import java.util.Date;

public class StdAttendance {
    public int AttId;

    public String EntranceTime;
    public String LeaveTime;
    // A, L, P
    public String AttStatus;
    public String AttDate;
    public boolean EntranceMsgSent;
    public boolean LeaveMsgSent;

    public int StdId_FK;

    public int StdId;
    public String Name;
    public String Rno;
    public String Phone;
    public String Class;
}
