package com.asifapps.oxforddigitalattendance;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.AsyncTask;
import android.provider.Telephony;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.telephony.SmsManager;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.asifapps.oxforddigitalattendance.CustomAdapters.AttRecyclerViewAdapter;
import com.asifapps.oxforddigitalattendance.Database.AppDb;
import com.asifapps.oxforddigitalattendance.Database.Daos.AttendanceDao;
import com.asifapps.oxforddigitalattendance.Database.Entities.Attendance;
import com.asifapps.oxforddigitalattendance.Interfaces.IAttendanceClickEvent;
import com.asifapps.oxforddigitalattendance.Utils.AttendanceHelper;
import com.asifapps.oxforddigitalattendance.Utils.Constants;
import com.asifapps.oxforddigitalattendance.Utils.DateTimeHelper;
import com.asifapps.oxforddigitalattendance.Utils.SmsBroadCastReceiver;
import com.asifapps.oxforddigitalattendance.Utils.SmsController;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class ViewAttActivity extends AppCompatActivity
        implements IAttendanceClickEvent, DatePickerDialog.OnDateSetListener {

    String date;
    String _class;
    String attStatus;

    String[] classes;

    AttendanceDao attendanceDao;

    List<Attendance> attendances;
    List<Attendance> attendancesCopy;

    AttRecyclerViewAdapter attRecyclerViewAdapter;

    TextView attDate;
    RecyclerView resView;
    LinearLayout sendToAllContainer;
    Button secMsgBtn;
    Button firstMsgBtn;

    Integer firstSmsCounter = 0;
    Integer secTimeSmsCounter = 0;

    // store broadCatRecivers for unregistering
    BroadcastReceiver smsBroadCast;
    Integer intentCounter = 0;

    boolean isFirstTime = true;

    SmsManager smsManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_att);

        // smsController = new SmsController(this);
        AppDb db = AppDb.getDatabase(this);
        attendanceDao = db.attendanceDao();

        setClassSpinner();
        attDate = (TextView)findViewById(R.id.attDate);
        sendToAllContainer = findViewById(R.id.sendToAllContainer);
        secMsgBtn = findViewById(R.id.secMsgBtn);
        firstMsgBtn = findViewById(R.id.firstMsgBtn);

        sendToAllContainer.setVisibility(View.INVISIBLE);

        resView = (RecyclerView)findViewById(R.id.attRecyclerView);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        resView.setLayoutManager(linearLayoutManager);

        attendances = new ArrayList<>();

        setAttRecyclerViewAdapter();

        smsManager = SmsManager.getDefault();

        smsBroadCast = new smsBroadCastet();

        try {
            String _cl = savedInstanceState.getString("Class");
            String st = savedInstanceState.getString("attStatus");
            String d = savedInstanceState.getString("date");

            if (_cl != null && st != null && d != null) {
                attStatus = st;
                date = d;
                _class = _cl;

                getAttendance();
            }
        } catch (Exception e) {

        }
    }

    private void setClassSpinner() {
        Spinner sp = (Spinner) findViewById(R.id._classesDropdown);

        classes = new String[]{"Prep", "1st", "2nd", "3rd", "4th", "5th", "6th", "7th", "8th", "9th", "10th", "old10th"};

        ArrayAdapter<String> arrayAdapter = new ArrayAdapter<>(this, R.layout.dropdown_item, classes);

        sp.setAdapter(arrayAdapter);

        sp.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                _class = classes[position];
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
    }

    public void openDateDialog(View v) {
        // Use the current date as the default date in the picker
        final Calendar c = Calendar.getInstance();
        int year = c.get(Calendar.YEAR);
        int month = c.get(Calendar.MONTH);
        int day = c.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(this, this, year, month, day);
        datePickerDialog.show();
    }

    public void onRadioButtonClicked(View view) {
        // Is the button now checked?
        boolean checked = ((RadioButton) view).isChecked();

        // Check which radio button was clicked
        switch(view.getId()) {
            case R.id.radioA:
                if (checked) {
                    attStatus = "A";
                }
                break;
            case R.id.radioL:
                if (checked) {
                    attStatus = Constants.leave;
                }
                break;
            case R.id.radioP:
                if (checked) {
                    attStatus = Constants.pesent;
                }
                break;
        }
    }

    // this does the rest for viewing attendances
    public void findAttendance(View v) {
        if (attStatus == "" || _class == "" || date == "") {
            return;
        }

        getAttendance();
    }

    public void getAttendance() {
        new AsyncTask<Void, Void, Void>() {

            @Override
            protected Void doInBackground(Void... voids) {
                attendances = attendanceDao.getAttendancesWithStatus(date, _class, attStatus);
                return null;
            }
            @Override
            protected void onPostExecute(Void aVoid) {
                super.onPostExecute(aVoid);
                attendancesCopy = attendances;

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        // Toast.makeText(getApplicationContext(), attendances.get(0).Name, Toast.LENGTH_LONG).show();
                        setAttRecyclerViewAdapter();
                    }
                });
            }
        }.execute();
    }

    private void setAttRecyclerViewAdapter() {
//        if (attendances.size() > 0)
//            setSendToAllContainerVisibility(true);
//        else
//            setSendToAllContainerVisibility(false);

        attRecyclerViewAdapter = new AttRecyclerViewAdapter(this, attendances);
        resView.setAdapter(attRecyclerViewAdapter);
    }

    @Override
    public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {

        String m = Integer.toString(month+1);
        String d = Integer.toString(dayOfMonth);

        date = Integer.toString(year) + "-" + m + "-" + d;

        attDate.setText(date);
    }

    @Override
    public void onClickReview(Attendance attendance) {
        Intent intent = new Intent(getBaseContext(), ReviewAttActivity.class);
        intent.putExtra("attId", attendance.AttId);
        intent.putExtra("stdId", attendance.StdId_FK);
        intent.putExtra("name", attendance.Name);
        intent.putExtra("attStatus", attendance.AttStatus);

        startActivity(intent);
    }

    @Override
    public void onClickDel(final Attendance attendance) {
        new AsyncTask<Void, Void, Void>() {

            @Override
            protected Void doInBackground(Void... voids) {
                attendanceDao.deleteAttendance(attendance);

                /// to refresh
                getAttendance();
                return null;
            }

            @Override
            protected void onPostExecute(Void aVoid) {
                super.onPostExecute(aVoid);

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(getApplicationContext(), "deleted", Toast.LENGTH_LONG).show();
                    }
                });
            }
        }.execute();
    }

    @Override
    public void onClickFirstTimeMsg(Attendance attendance) {
        //sendMsg(attendance, Constants.fTime);
    }

    @Override
    public void onClickSecTimeMsg(Attendance attendance) {
        //sendMsg(attendance, Constants.sTime);
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        super.onSaveInstanceState(savedInstanceState);
        // Save UI state changes to the savedInstanceState.
        // This bundle will be passed to onCreate if the process is
        // killed and restarted.
        savedInstanceState.putString("Class", _class);
        savedInstanceState.putString("attStatus", attStatus);
        savedInstanceState.putString("date", date);
    }

    public void sendToAllFirstTimeMsg(View view) {
        if(AttendanceHelper.isMsgSentToAll(attendances, Constants.fTime)) {
            Toast.makeText(this, "Already sent to all", Toast.LENGTH_LONG).show();
            return;
        }

        firstMsgBtn.setClickable(false);
        firstMsgBtn.setText("Sending...");

        registerReceiver(smsBroadCast, new IntentFilter(intentFilter));

        isFirstTime = true;

        sendMsg();
    }

    public void sendToAllSecondTimeMsg(View view) {
        if(AttendanceHelper.isMsgSentToAll(attendances, Constants.sTime)) {
            Toast.makeText(this, "Already sent to all", Toast.LENGTH_LONG).show();
            return;
        }

        secMsgBtn.setClickable(false);
        secMsgBtn.setText("Sending...");

        for(Attendance attendance: attendances) {
            if (attendance.LeaveMsgSent) {
                continue;
            }

            // sendMsg(attendance, Constants.sTime);
        }
    }


    String intentFilter = "SMS_SENT1";
//    final Attendance attendance, Integer time
    public void sendMsg() {

//        if (time.equals(Constants.fTime)) {
//            firstSmsCounter++;
//        } else {
//            secTimeSmsCounter++;
//        }

        if (attendancesCopy.size() == 0) {
            return;
        }

        String msgText = "";

        String st1 = "\n\n\nOXFORD School Bakak Khel ";

        String st2 = "%s  طالب علم\n" +
                "سکول میں داخل ہوچکا ہے۔";
        String st3 = "%s  طالب علم\n" +
                "سکول سے نکل چکا ہے۔";
        Attendance attendance = attendancesCopy.get(0);

        if (isFirstTime) {
            msgText =  String.format(st2, attendance.Name) + st1;
        } else {
            msgText = String.format(st3, attendance.Name) + st1;
        }

        Integer index = attendances.indexOf(attendance);

        // STEP-1___
        // SEND PendingIntent
        int requestCode = attendancesCopy.size();

        Intent smsIntent = new Intent(intentFilter);
//
//        smsIntent.putExtra("time", time);
        smsIntent.putExtra("attId", attendance.AttId);
        smsIntent.putExtra("index", index);

        PendingIntent sentPI = PendingIntent.getBroadcast(this, requestCode, smsIntent, PendingIntent.FLAG_ONE_SHOT);
        // STEP-2___
        // SEND BroadcastReceiver
        smsManager.sendTextMessage(attendance.Phone, "Oxford", msgText, sentPI,null);

        attendancesCopy.remove(0);
    }

    private class smsBroadCastet extends BroadcastReceiver{
        @Override
        public void onReceive(Context context, Intent intent) {
         Toast.makeText(context, "woooo", Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        registerReceiver(smsBroadCast, new IntentFilter(intentFilter));
    }

    @Override
    public void onStop() {
        super.onStop();

        unregisterReceiver(smsBroadCast);
    }

    private void updateSmsStatus(final Integer attId) {

        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... voids) {

                if (isFirstTime)
                {
                    attendanceDao.upadateEntranceMsgSent(attId, true);
                }
                else {
                    attendanceDao.upadateLeaveMsgSent(attId, true);
                }

                return null;
            }
        }.execute();
    }

    private void setSendToAllContainerVisibility(boolean visibility) {
        if (visibility && attStatus == Constants.pesent && date.equals(DateTimeHelper.GetCurrentDate())) {
            sendToAllContainer.setVisibility(View.VISIBLE);
            return;
        }

        sendToAllContainer.setVisibility(View.INVISIBLE);
    }
}
