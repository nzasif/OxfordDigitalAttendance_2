package com.asifapps.oxforddigitalattendance;

import android.content.Intent;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.asifapps.oxforddigitalattendance.Database.AppDb;
import com.asifapps.oxforddigitalattendance.Database.Daos.AttendanceDao;
import com.asifapps.oxforddigitalattendance.Database.Daos.StudentDao;
import com.asifapps.oxforddigitalattendance.Database.Entities.Attendance;
import com.asifapps.oxforddigitalattendance.Database.Entities.Student;
import com.asifapps.oxforddigitalattendance.Utils.Constants;
import com.asifapps.oxforddigitalattendance.Utils.DateTimeHelper;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    List<Attendance> attendances;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initAtt();
    }

    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.options_menu, menu);
        return true;
    }

    private void initAtt() {
        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... voids) {
                AppDb db = AppDb.getDatabase(getApplicationContext());
                StudentDao studentDao = db.studentDao();
                AttendanceDao attendanceDao = db.attendanceDao();

                List<Attendance> att = attendanceDao.getAttendances(DateTimeHelper.GetCurrentDate());

                if (att.size() > 0) {
                    return null;
                }

                final List<Student> students = studentDao.getAllStudents();

                if (students.size() == 0) {
                    return null;
                }

                for (int i = 0; i < students.size(); i++) {
                    Attendance attendance = new Attendance();
                    Student student = students.get(i);

                    attendance.StdId_FK = student.StdId;
                    attendance.Rno = student.Rno;
                    attendance.Class = student.Class;
                    attendance.Phone = student.Phone;
                    attendance.Name = student.Name;

                    attendance.AttDate = DateTimeHelper.GetCurrentDate();
                    attendance.AttStatus = Constants.absent;
                    attendance.EntranceTime = "--:--:--";
                    attendance.LeaveTime = "--:--:--";
                    attendance.EntranceMsgSent = false;
                    attendance.LeaveMsgSent = false;

                    attendanceDao.insertAttendance(attendance);
                }

                runOnUiThread(new Runnable() {
                    @Override

                    public void run() {
                        Toast.makeText(getApplicationContext(), "Initialized.", Toast.LENGTH_LONG).show();
                    }
                });

                return null;
            }
        }.execute();
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.stdListView:
                startActivity(new Intent(this, StudentListActivity.class));
                break;
            case R.id.addNew:
                startActivity(new Intent(this, AddNewActivity.class));
                break;
            case R.id.cardScanner:
                startActivity(new Intent(this, QRCodeScannerActivity.class));
                break;
            case R.id.viewAtt:
                startActivity(new Intent(this, ViewAttActivity.class));
                break;
            case R.id.stdEntry:
                startActivity(new Intent(this, EnterStdRecordActivity.class));
                break;
            case R.id.importExport:
                startActivity(new Intent(this, ImportExport.class));
                break;
            case R.id.sendSms:
                startActivity(new Intent(this, SmsSenderActivity.class));
                break;
        }
        return true;
    }

    public void reInitAttendance(View view) {
        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... voids) {
                AppDb db = AppDb.getDatabase(getApplicationContext());
                StudentDao studentDao = db.studentDao();
                AttendanceDao attendanceDao = db.attendanceDao();

                attendances = attendanceDao.getAttendances(DateTimeHelper.GetCurrentDate());

                final List<Student> students = studentDao.getAllStudents();

                if (students.size() == 0) {
                    return null;
                }

                for (int i = 0; i < students.size(); i++) {
                    Student student = students.get(i);

                    if (isExist(student)) {
                        continue;
                    }

                    Attendance attendance = new Attendance();

                    attendance.StdId_FK = student.StdId;
                    attendance.Rno = student.Rno;
                    attendance.Class = student.Class;
                    attendance.Phone = student.Phone;
                    attendance.Name = student.Name;

                    attendance.AttDate = DateTimeHelper.GetCurrentDate();
                    attendance.AttStatus = Constants.absent;
                    attendance.EntranceTime = "--:--:--";
                    attendance.LeaveTime = "--:--:--";
                    attendance.EntranceMsgSent = false;
                    attendance.LeaveMsgSent = false;

                    attendanceDao.insertAttendance(attendance);
                }

                runOnUiThread(new Runnable() {
                    @Override

                    public void run() {
                        Toast.makeText(getApplicationContext(), "Initialized.", Toast.LENGTH_LONG).show();
                    }
                });

                return null;
            }
        }.execute();
    }

    private boolean isExist(Student student) {
        for (Attendance at: attendances) {
            if (at.StdId_FK == student.StdId) {
                return true;
            }
        }

        return false;
    }

    public void openStdList(View v) {
        startActivity(new Intent(this, StudentListActivity.class));
    }

    public void openCodeScanner(View v) {
        startActivity(new Intent(this, QRCodeScannerActivity.class));
    }

    public void openAttendance(View v) {
        startActivity(new Intent(this, ViewAttActivity.class));
    }

    public void openAddNew(View v) {
        startActivity(new Intent(this, AddNewActivity.class));
    }
}
