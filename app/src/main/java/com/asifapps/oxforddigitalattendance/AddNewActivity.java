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

public class AddNewActivity extends AppCompatActivity implements AdapterView.OnItemSelectedListener {

    private String[] classes = {"Prep", "1st", "2nd", "3rd", "4th", "5th", "6th", "7th", "8th", "9th", "10th"};
    private String selectedClass = "";

    private Spinner spinner;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_new);

        setSpinner();
    }

    private void setSpinner() {
        spinner = findViewById(R.id.classesDropdown);

        final ArrayAdapter<String> arrayAdapter = new ArrayAdapter<>(this, R.layout.dropdown_item, classes);

        spinner.setAdapter(arrayAdapter);
        spinner.setOnItemSelectedListener(this);
    }

    public void SubmitData(View v) {
        EditText nameView = findViewById(R.id.name);
        String name = nameView.getText().toString();

        EditText phoneView = findViewById(R.id.phone);
        String phone = phoneView.getText().toString();

        EditText rnoView = findViewById(R.id.rno);
        String rno = rnoView.getText().toString();

        EditText guardView = findViewById(R.id.guard);
        String guard = guardView.getText().toString();

        EditText dobView = findViewById(R.id.dob);
        String dob = dobView.getText().toString();

        EditText bldGroupView = findViewById(R.id.bgroup);
        String bgroup = bldGroupView.getText().toString();

        if (rno.isEmpty() || name.isEmpty() || phone.isEmpty() || selectedClass.isEmpty() || guard.isEmpty()) {

            Toast.makeText(this, "Student info should not be empty..", Toast.LENGTH_LONG).show();
            return;
        }

        final Student std = new Student();

        std.BloodGroup = bgroup;
        std.Name = name;
        std.Class = selectedClass;
        std.DOB = dob;
        std.FatherName = guard;
        std.Rno = Integer.parseInt(rno);
        std.Phone = phone;

        insertSudent(std);
    }

    // run in background
    private void insertSudent (final Student std) {
        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... voids) {
                AppDb db = AppDb.getDatabase(getApplicationContext());

                StudentDao studentDao = db.studentDao();

                studentDao.insertStudent(std);

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(getApplicationContext(), "Student added successfully", Toast.LENGTH_LONG).show();
                    }
                });

                return null;
            }

        }.execute();
    }


    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        selectedClass = classes[position];
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }
}
