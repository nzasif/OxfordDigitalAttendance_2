package com.asifapps.oxforddigitalattendance.Database.Daos;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Delete;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.OnConflictStrategy;
import android.arch.persistence.room.Query;
import android.arch.persistence.room.Update;

import com.asifapps.oxforddigitalattendance.Database.Entities.Attendance;
import com.asifapps.oxforddigitalattendance.Database.ModelClasses.StdAttendance;

import java.util.Date;
import java.util.List;

@Dao
public interface AttendanceDao {

    @Query("select * from Attendance where AttDate = :date order by Name ASC")
    public List<Attendance> getAttendances(String date);

    @Query("select * from Attendance order by Name ASC")
    public List<Attendance> getAttendances();

    @Query("select * from Attendance where AttId = :attId")
    public Attendance getAttendance(Integer attId);

    @Query("select * from Attendance" +
            " where AttDate = :date And Class = :Class And AttStatus = :status order by Name ASC")
    public List<Attendance> getAttendancesWithStatus(String date, String Class, String status);

    @Query("select * from Attendance" +
            " where AttDate = :date And AttStatus = :status And EntranceTime != '--:--:--' And EntranceMsgSent = :msgStatus")
    public List<Attendance> getFirstTimeAttendancesWithStatus(String date, String status, boolean msgStatus);

    @Query("select * from Attendance" +
            " where AttDate = :date And AttStatus = :status And LeaveTime <> '--:--:--' And LeaveMsgSent = :msgStatus")
    public List<Attendance> getSecondTimeAttendancesWithStatus(String date, String status, boolean msgStatus);

    @Query("select * from Attendance" +
            " where Rno = :rno And Class = :Class And AttDate = :date And EntranceTime = '--:--:--'")
    public Attendance getFirstAttendance(String rno, String Class, String date);

    @Query("select * from Attendance" +
            " where Rno = :rno And Class = :Class And AttDate = :date And LeaveTime = '--:--:--'")
    public Attendance getSecondAttendance(String rno, String Class, String date);

    @Insert
    public void insertAttendance(Attendance attendance);

    @Update
    public void updateAttendance(Attendance attendance);

    @Update(onConflict = OnConflictStrategy.REPLACE)
    public void updateAttendances(List<Attendance> attendance);

    @Delete
    public void deleteAttendance(Attendance attendance);

    @Query("Update Attendance Set EntranceMsgSent = :msgSent where AttId = :attId")
    public void upadateEntranceMsgSent(int attId, boolean msgSent);

    @Query("Update Attendance Set LeaveMsgSent = :msgSent where AttId = :attId")
    public void upadateLeaveMsgSent(int attId, boolean msgSent);
}
