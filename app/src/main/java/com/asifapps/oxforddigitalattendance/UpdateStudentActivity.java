package com.asifapps.oxforddigitalattendance;

import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import com.asifapps.oxforddigitalattendance.Database.AppDb;
import com.asifapps.oxforddigitalattendance.Database.Daos.StudentDao;
import com.asifapps.oxforddigitalattendance.Database.Entities.Student;

import java.security.SecureRandom;

public class UpdateStudentActivity extends AppCompatActivity implements AdapterView.OnItemSelectedListener {

    private String[] classes = {"1st", "2nd", "3rd", "4th", "5th", "6th", "7th", "8th", "9th", "10th"};
    private String selectedClass = "";

    private Spinner spinner;

    private Student student;

    private EditText nameView;
    private EditText phoneView;
    private EditText rnoView;
    private EditText guardView;
    private EditText dobView;
    private EditText bldGroupView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_update_student);

        Integer stdId = getIntent().getExtras().getInt("stdId");

        if (stdId == null) {
            Toast.makeText(this, "Failed try again.", Toast.LENGTH_SHORT).show();
        } else {
            getStudent(stdId);
        }
    }

    private void getStudent(final int stdId) {

        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... voids) {
                AppDb db = AppDb.getDatabase(getApplicationContext());
                StudentDao studentDao = db.studentDao();

                student = studentDao.getStudent(stdId);
                return null;
            }

            @Override
            protected void onPostExecute(Void aVoid) {
                super.onPostExecute(aVoid);
                if (student != null)
                    runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        initViews(student);
                    }
                });
            }
        }.execute();
    }

    private void initViews(Student std) {
        nameView = findViewById(R.id.updateName);
        nameView.setText(std.Name);

        phoneView = findViewById(R.id.updatePhone);
        phoneView.setText(std.Phone);

        rnoView = findViewById(R.id.updateRno);
        rnoView.setText(std.Rno);

        guardView = findViewById(R.id.updateGuard);
        guardView.setText(std.FatherName);

        dobView = findViewById(R.id.updateDob);
        dobView.setText(std.DOB);

        bldGroupView = findViewById(R.id.updateBgroup);
        bldGroupView.setText(std.BloodGroup);

        setSpinner(std.Class);
    }

    private void setSpinner(final String _class) {
        spinner = findViewById(R.id.updateClassesDropdown);

        final ArrayAdapter<String> arrayAdapter = new ArrayAdapter<>(this, R.layout.dropdown_item, classes);

        spinner.setAdapter(arrayAdapter);
        spinner.setOnItemSelectedListener(this);

        spinner.post(new Runnable() {
            @Override
            public void run() {
                spinner.setSelection(arrayAdapter.getPosition(_class));
            }
        });
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        selectedClass = classes[position];
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {
    }

    public void SubmitData(View v) {
        String name = nameView.getText().toString();

        String phone = phoneView.getText().toString();

        String rno = rnoView.getText().toString();

        String guard = guardView.getText().toString();

        String dob = dobView.getText().toString();

        String bgroup = bldGroupView.getText().toString();

        if (rno.isEmpty() || name.isEmpty() || phone.isEmpty() || selectedClass.isEmpty() || guard.isEmpty()) {

            Toast.makeText(this, "Student info should not be empty..", Toast.LENGTH_LONG);
            return;
        }

        student.Name = name;
        student.BloodGroup = bgroup;
        student.Class = selectedClass;
        student.DOB = dob;
        student.FatherName = guard;
        student.Rno = rno;
        student.Phone = phone;

        updateStudent(student);
    }

    // run in background
    private void updateStudent (final Student std) {
        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... voids) {
                AppDb db = AppDb.getDatabase(getApplicationContext());

                StudentDao studentDao = db.studentDao();

                studentDao.updateStudent(std);

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(getApplicationContext(), "Student updated successfully", Toast.LENGTH_LONG).show();
                    }
                });

                return null;
            }

        }.execute();
    }
}
