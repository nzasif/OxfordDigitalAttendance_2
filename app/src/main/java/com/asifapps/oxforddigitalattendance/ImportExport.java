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
import android.widget.TextView;
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
    ArrayList<AttImportLog> attImportLogs = new ArrayList<>();
    // String bluetoothPathLowerCase = "bluetooth";
    TextView attLogText;

    private String[] fileNames = {"Cl_Prep", "Cl_1st", "Cl_2nd", "Cl_3rd", "Cl_4th", "Cl_5th", "Cl_6th", "Cl_7th", "Cl_8th", "Cl_9th", "Cl_10th"};

    AttendanceDao attendanceDao;
    private String fileName;
    int count = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_import_export);
        attLogText = findViewById(R.id.attImportLog);
        attendanceDao = AppDb.getDatabase(this).attendanceDao();

        // setSpinner();

        Button btn = (Button) findViewById(R.id.clearFilesBtn);
        btn.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                clearFiles();
                return false;
            }
        });
    }

    // not used yet
//    private void setSpinner() {
//        Spinner sp = (Spinner) findViewById(R.id.fileNameDropdown);
//        ArrayAdapter<String> arrayAdapter = new ArrayAdapter<>(this, R.layout.dropdown_item, fileNames);
//
//        sp.setAdapter(arrayAdapter);
//        sp.setOnItemSelectedListener(this);
//    }

    public void readStudentsCSV(View view) {
        String stdFile = "opsstdlist.csv";

        final ArrayList<Student> students = new ArrayList<>();
        int counter = 0;

        try {
            File csvfile = new File(Environment.getExternalStorageDirectory() + "/" + stdFile);
            CSVReader reader = new CSVReader(new FileReader(csvfile.getAbsolutePath()));
            String[] nextLine;

            while ((nextLine = reader.readNext()) != null) {
                counter++;
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
            Toast.makeText(
                    this,
                    e.getMessage() + " error occurred at line #" + counter,
                    Toast.LENGTH_LONG).show();
            if (students.size() > 0) {
                insertStudents(students);
            }
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

        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... voids) {
                    for(String fName: fileNames) {
                        AttImportLog attImportLog = new AttImportLog();

                        attImportLog.enterdAttendances = 0;
                        attImportLog.corruptLineNo = -1;
                        attImportLog.totalAttendances = 0;

                        try {
                            filePath = Environment.getExternalStorageDirectory() + "/" + bluetoothPathUpperCase + "/" + fName + ".csv";
                            File csvfile = new File(filePath);
                            if (!csvfile.exists()) {
                                continue;
                            }
                            attImportLog.fileName = fName;

                            CSVReader reader = new CSVReader(new FileReader(csvfile.getAbsolutePath()));
                            String[] nextLine; //Rno, Class, EntranceTime, LeaveTime, AttDate

                            String date = DateTimeHelper.GetCurrentDate();
                            String nullTime = "--:--:--";

                            while ((nextLine = reader.readNext()) != null) {
                                final Attendance attendance = attendanceDao.getAttendance(Integer.parseInt(nextLine[0]), nextLine[1], date);
                                if (attendance == null) {
                                    continue;
                                }

                                attImportLog.corruptLineNo++;

                                if (attendance.EntranceTime.equalsIgnoreCase(nullTime) && !nextLine[2].isEmpty()) {
                                    attendance.EntranceTime = nextLine[2];
                                    attendance.AttStatus = Constants.pesent;

                                    attImportLog.enterdAttendances++;
                                }

                                if (attendance.LeaveTime.equalsIgnoreCase(nullTime) && !nextLine[3].isEmpty()) {
                                    attendance.LeaveTime = nextLine[3];
                                    attendance.AttStatus = Constants.pesent;

                                    attImportLog.enterdAttendances++;
                                }

                                attImportLog.totalAttendances++;

                                attendanceDao.updateAttendance(attendance);
                            }
                        } catch (final Exception e) {
                            attImportLog.errorMsg = e.getMessage();
                        }
                        attImportLogs.add(attImportLog);
                        attImportLog = null;
                    } // for ends

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        StringBuilder text = new StringBuilder();
                        text.append("Status::\n");

                        for(AttImportLog log: attImportLogs) {
                            text.append("Class Name: ").append(log.fileName).append("\n")
                            .append("Total Lines: ").append(log.totalAttendances).append("\n")
                            .append("Total Entries: ").append(log.enterdAttendances).append("\n")
                            .append("Errors: ").append(log.errorMsg).append("\n");
                            if (log.errorMsg != null) {
                                text.append("Corrupt Line #").append(log.corruptLineNo).append("\n");
                            }
                            text.append("-------------\n");
                        }

                        attLogText.setText(text);
                        attImportLogs.clear();
                    }
                });
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

class AttImportLog {
    public String errorMsg;
    public String fileName;
    public int totalAttendances;
    public int enterdAttendances;
    public int corruptLineNo;
}