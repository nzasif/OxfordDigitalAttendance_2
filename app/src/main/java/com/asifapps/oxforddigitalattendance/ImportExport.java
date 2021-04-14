package com.asifapps.oxforddigitalattendance;

import android.os.AsyncTask;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.Toast;

import com.asifapps.oxforddigitalattendance.Database.AppDb;
import com.asifapps.oxforddigitalattendance.Database.Daos.AttendanceDao;
import com.asifapps.oxforddigitalattendance.Database.Daos.StudentDao;
import com.asifapps.oxforddigitalattendance.Database.Entities.Attendance;
import com.asifapps.oxforddigitalattendance.Database.Entities.Student;
import com.asifapps.oxforddigitalattendance.Utils.Constants;
import com.asifapps.oxforddigitalattendance.Utils.DateTimeHelper;
import com.opencsv.CSVReader;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class ImportExport extends AppCompatActivity implements AdapterView.OnItemSelectedListener {

    String filePath = "";
    String bluetoothPathUpperCase = "Bluetooth";
    // String bluetoothPathLowerCase = "bluetooth";

    private String[] fileNames = {"Oxford_A", "Oxford_B", "Oxford_C", "Oxford_D", "Oxford_E", "Oxford_F", "Oxford_G", "Oxford_H", "Oxford_I", "Oxford_J"};

    // main app
    AttendanceDao attendanceDao;
    private String fileName;
    int count = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_import_export);

        attendanceDao = AppDb.getDatabase(this).attendanceDao();

        setSpinner();
        filePath = Environment.getExternalStorageDirectory() + "/" + fileNames[0] + ".csv";

        Button btn = (Button) findViewById(R.id.clearFilesBtn);

        btn.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                clearFiles();
                return false;
            }
        });
    }

    private void setSpinner() {
        Spinner sp = (Spinner) findViewById(R.id.fileNameDropdown);
        ArrayAdapter<String> arrayAdapter = new ArrayAdapter<>(this, R.layout.dropdown_item, fileNames);

        sp.setAdapter(arrayAdapter);
        sp.setOnItemSelectedListener(this);
    }

    public void readStudentsCSV(View view) {
        String stdFile = "ops_std_list.csv";
        try {
            File csvfile = new File(Environment.getExternalStorageDirectory() + "/" + stdFile);
            CSVReader reader = new CSVReader(new FileReader(csvfile.getAbsolutePath()));
            String[] nextLine;

            final ArrayList<Student> students = new ArrayList<>();

            while ((nextLine = reader.readNext()) != null) {
                // nextLine[] is an array of values from the line
                Student std = new Student();

                std.Rno = Integer.parseInt(nextLine[0]);
                std.Class = nextLine[1];
                std.Phone = nextLine[2];
                std.Name = nextLine[3];
                std.FatherName = nextLine[4];

                students.add(std);
            }

            insertStudents(students);

        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(
                    this,
                    "The " + stdFile + " file was not found, place this file in root dir of external/internal storage.",
                    Toast.LENGTH_LONG).show();
        }
    }

    // run in background
    private void insertStudents (final ArrayList<Student> students) {
        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... voids) {
                AppDb db = AppDb.getDatabase(getApplicationContext());
                StudentDao studentDao = db.studentDao();
                studentDao.insertStudents(students);
                return null;
            }

            @Override
            protected void onPostExecute(Void aVoid) {
                super.onPostExecute(aVoid);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(getApplicationContext(), Integer.toString(students.size()) + " imported successfully.", Toast.LENGTH_SHORT).show();
                    }
                });
            }

        }.execute();
    }

    public void readAttendanceCSV(View view) {
        if (filePath == "") {
            Toast.makeText(this, "select a file name then try again.", Toast.LENGTH_SHORT).show();
            return;
        }

        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... voids) {
                try {

                    File csvfile = new File(filePath);
                    CSVReader reader = new CSVReader(new FileReader(csvfile.getAbsolutePath()));
                    String[] nextLine; //Rno, Class, EntranceTime, LeaveTime, AttDate

                    String date = DateTimeHelper.GetCurrentDate();
                    String nullTime = "--:--:--";

                    while ((nextLine = reader.readNext()) != null) {
                        final Attendance attendance = attendanceDao.getAttendance(Integer.parseInt(nextLine[0]), nextLine[1], date);
                        if (attendance == null) {
                            continue;
                        }

                        if (attendance.EntranceTime.equalsIgnoreCase(nullTime) && nextLine[2] != "") {
                            attendance.EntranceTime = nextLine[2];
                            attendance.AttStatus = Constants.pesent;
                            count++;
                        }

                        if (attendance.LeaveTime.equalsIgnoreCase(nullTime) && nextLine[3] != "") {
                            attendance.LeaveTime = nextLine[3];
                            attendance.AttStatus = Constants.pesent;
                            count++;
                        }

                        attendanceDao.updateAttendance(attendance);
                    }

                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(getApplicationContext(), Integer.toString(count) + " attendances Imported", Toast.LENGTH_SHORT).show();
                            count = 0;
                        }
                    });
                } catch (final Exception e) {
                    count = 0;
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(getApplicationContext(), "failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
                }
                return null;
            }
        }.execute();
    }

    private void clearFiles() {
        for (String fName: fileNames) {
            fileName = fName;
            setFilePath(bluetoothPathUpperCase);
            try {
                File f = new File(filePath);
                f.delete();
            } catch (Exception e) {
            }
        }
        Toast.makeText(this, "Cleaned", Toast.LENGTH_SHORT).show();
    }

    private void setFilePath(String folderName) {
        if (folderName.isEmpty()) {
            filePath = Environment.getExternalStorageDirectory() + "/" + fileName + ".csv";
            return;
        }

        filePath = Environment.getExternalStorageDirectory() + "/" + folderName + "/" + fileName + ".csv";
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        fileName = fileNames[position];
        setFilePath(bluetoothPathUpperCase);
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {
    }
}