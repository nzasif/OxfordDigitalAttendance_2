package com.asifapps.oxforddigitalattendance;

import android.app.Activity;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

import com.asifapps.oxforddigitalattendance.Database.AppDb;
import com.asifapps.oxforddigitalattendance.Database.Daos.AttendanceDao;
import com.asifapps.oxforddigitalattendance.Database.Entities.Attendance;
import com.asifapps.oxforddigitalattendance.Utils.Constants;
import com.asifapps.oxforddigitalattendance.Utils.DateTimeHelper;

public class ReviewAttActivity extends AppCompatActivity {

    Attendance attendance;

    Integer attId;
    String rno;
    String name;
    String _class;
    String stdId;
    String attStatus;

    TextView stdAttName;

    RadioButton a;
    RadioButton l;
    RadioButton p;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_review_att);

        a = findViewById(R.id.a);
        l = findViewById(R.id.l);
        p = findViewById(R.id.p);

        stdAttName = findViewById(R.id.stdAttName);

        attId = getIntent().getIntExtra("attId", 0);
        name = getIntent().getStringExtra("name");
        stdId = getIntent().getStringExtra("stdId");
        attStatus = getIntent().getStringExtra("attStatus");

        stdAttName.setText(name);
        setRadios(attStatus);

        getAtt();
    }

    private void  setRadios(String attStatus) {

        // Check which radio button was clicked
        switch(attStatus) {
            case "A":
                a.setActivated(true);
                break;
            case "P":
                p.setActivated(true);
                break;
            case "L":
                l.setActivated(true);
                break;
        }
    }

    public void onRadioButtonClicked(View view) {
        // Is the button now checked?
        boolean checked = ((RadioButton) view).isChecked();

        // Check which radio button was clicked
        switch(view.getId()) {
            case R.id.a:
                if (checked)
                    attStatus = "A";
                break;
            case R.id.l:
                if (checked)
                    attStatus = "L";
                break;
            case R.id.p:
                if (checked)
                    attStatus = "P";
                break;
        }
    }

    public void getAtt() {
        new AsyncTask<Void, Void, Void>(){

            @Override
            protected Void doInBackground(Void... voids) {
                AppDb appDb = AppDb.getDatabase(getApplicationContext());

                AttendanceDao attendanceDao = appDb.attendanceDao();
                attendance = attendanceDao.getAttendance(attId);
                return null;
            }
//
//            @Override
//            protected void onPostExecute(Void aVoid) {
//                super.onPostExecute(aVoid);
//
//                runOnUiThread(new Runnable() {
//                    @Override
//                    public void run() {
//                        Toast.makeText(getApplicationContext(), "updated", Toast.LENGTH_LONG).show();
//                    }
//                });
//            }
        }.execute();
    }

    public void updateAtt(View view) {
        new AsyncTask<Void, Void, Void>(){

            @Override
            protected Void doInBackground(Void... voids) {
                AppDb appDb = AppDb.getDatabase(getApplicationContext());

                AttendanceDao attendanceDao = appDb.attendanceDao();

                attendance.AttStatus = attStatus;
                if (attStatus == Constants.pesent) {
                    attendance.EntranceTime = DateTimeHelper.GetCurrentTime();
                }
                attendanceDao.updateAttendance(attendance);

                return null;
            }

            @Override
            protected void onPostExecute(Void aVoid) {
                super.onPostExecute(aVoid);

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(getApplicationContext(), "updated ", Toast.LENGTH_LONG).show();
                    }
                });
            }
        }.execute();
    }
}
