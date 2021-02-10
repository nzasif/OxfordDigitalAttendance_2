package com.asifapps.oxforddigitalattendance.Utils;

import com.asifapps.oxforddigitalattendance.Database.Entities.Attendance;

import java.util.List;

public class AttendanceHelper {
    public static boolean isMsgSentToAll(List<Attendance> attendances, Integer time) {
        for(Attendance attendance: attendances) {
            if (!attendance.EntranceMsgSent && time.equals(Constants.fTime)) {
                return false;
            }

            if (!attendance.LeaveMsgSent && time.equals(Constants.sTime)) {
                return false;
            }
        }

        return true;
    }
}
