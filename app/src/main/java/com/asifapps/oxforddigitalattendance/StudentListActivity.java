package com.asifapps.oxforddigitalattendance;

import android.content.Intent;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.Toast;

import com.asifapps.oxforddigitalattendance.CustomAdapters.StudentsRecyclerViewAdapter;
import com.asifapps.oxforddigitalattendance.Database.AppDb;
import com.asifapps.oxforddigitalattendance.Database.Daos.StudentDao;
import com.asifapps.oxforddigitalattendance.Database.Entities.Student;
import com.asifapps.oxforddigitalattendance.Interfaces.IStudentClickEvent;

import java.util.ArrayList;
import java.util.List;

public class StudentListActivity extends AppCompatActivity implements IStudentClickEvent, AdapterView.OnItemSelectedListener {

    RecyclerView resView;

    StudentsRecyclerViewAdapter studentsRecyclerViewAdapter;

    private List<Student> studentsList = new ArrayList<Student>();

    private String viewingClass = "";

    private String[] classes = {"Prep", "1st", "2nd", "3rd", "4th", "5th", "6th", "7th", "8th", "9th", "10th", "old10th"};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_student_list);

        setSpinner();

        resView = (RecyclerView)findViewById(R.id.stdListRecycler);

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        resView.setLayoutManager(linearLayoutManager);

        Student dummyStudent = new Student();
        dummyStudent.Rno = "-1";

        studentsList.add(dummyStudent);

        studentsRecyclerViewAdapter = new StudentsRecyclerViewAdapter(this, studentsList);

        resView.setAdapter(studentsRecyclerViewAdapter);
    }

    @Override
    public void onResume()
    {  // After a pause OR at startup
        super.onResume();

        getStudents(viewingClass);
    }

    private void setSpinner() {
        Spinner sp = (Spinner) findViewById(R.id.classesDropdown);

        ArrayAdapter<String> arrayAdapter = new ArrayAdapter<>(this, R.layout.dropdown_item, classes);

        sp.setAdapter(arrayAdapter);
        sp.setOnItemSelectedListener(this);
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        getStudents(classes[position]);
    }

    private void getStudents(final String _class) {
        new AsyncTask<Void, Void, Void>() {

            @Override
            protected Void doInBackground(Void... voids) {
                AppDb db = AppDb.getDatabase(getApplicationContext());
                StudentDao studentDao = db.studentDao();

                studentsList = studentDao.getStudents(_class);

                viewingClass = _class;
                return null;
            }

            @Override
            protected void onPostExecute(Void aVoid) {
                super.onPostExecute(aVoid);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        setUpStudentList();
                    }
                });
            }
        }.execute();

    }

    private void setUpStudentList() {

        studentsRecyclerViewAdapter = new StudentsRecyclerViewAdapter(this, studentsList);

        resView.setAdapter(studentsRecyclerViewAdapter);
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }

    @Override
    public void onClickDelStudent(final Student std) {
        new AsyncTask<Void, Void, Void>() {

            @Override
            protected Void doInBackground(Void... voids) {
                AppDb db = AppDb.getDatabase(getApplicationContext());
                StudentDao studentDao = db.studentDao();

                studentDao.deleteStudent(std);
                return null;
            }

            @Override
            protected void onPostExecute(Void avoid) {
                super.onPostExecute(avoid);

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        getStudents(viewingClass);

                        Toast.makeText(getApplicationContext(), "deleted ", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        }.execute();
    }

    @Override
    public void onClickEditStudent(Student std) {
        Intent mIntent = new Intent(this, UpdateStudentActivity.class);
        mIntent.putExtra("stdId", std.StdId);

        startActivity(mIntent);
    }
}
