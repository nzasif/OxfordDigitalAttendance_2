package com.asifapps.oxforddigitalattendance.Interfaces;

import com.asifapps.oxforddigitalattendance.Database.Entities.Attendance;
import com.asifapps.oxforddigitalattendance.Database.ModelClasses.StdAttendance;

public interface IAttendanceClickEvent {
    void onClickReview(Attendance Attendance);
    void onClickDel(Attendance Attendance);
    void onClickFirstTimeMsg(Attendance Attendance);
    void onClickSecTimeMsg(Attendance Attendance);
}
