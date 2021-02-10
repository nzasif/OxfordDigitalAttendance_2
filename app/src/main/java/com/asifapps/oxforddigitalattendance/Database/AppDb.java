package com.asifapps.oxforddigitalattendance.Database;

import android.arch.persistence.room.Database;
import android.arch.persistence.room.Room;
import android.arch.persistence.room.RoomDatabase;
import android.arch.persistence.room.TypeConverters;
import android.content.Context;

import com.asifapps.oxforddigitalattendance.Database.Daos.AttendanceDao;
import com.asifapps.oxforddigitalattendance.Database.Daos.StudentDao;
import com.asifapps.oxforddigitalattendance.Database.Entities.Admin;
import com.asifapps.oxforddigitalattendance.Database.Entities.Attendance;
import com.asifapps.oxforddigitalattendance.Database.Entities.Converters;
import com.asifapps.oxforddigitalattendance.Database.Entities.Student;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Database(
        entities = { Student.class, Attendance.class, Admin.class},
        version = 4, exportSchema = false)
@TypeConverters({Converters.class})

public abstract class AppDb extends RoomDatabase {
    public abstract StudentDao studentDao();
    public abstract AttendanceDao attendanceDao();
    // public abstract Admin adminDao();

    private static volatile AppDb INSTANCE;
    private static final int NUMBER_OF_THREADS = 4;
    static final ExecutorService databaseWriteExecutor =
            Executors.newFixedThreadPool(NUMBER_OF_THREADS);

    public static AppDb getDatabase(final Context context) {
        if (INSTANCE == null) {
            synchronized (AppDb.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(context.getApplicationContext(),
                            AppDb.class, "mydb11")
                            .build();
                }
            }
        }
        return INSTANCE;
    }
}
