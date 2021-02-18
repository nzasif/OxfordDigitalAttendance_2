package com.asifapps.oxforddigitalattendance;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.telephony.SmsManager;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.asifapps.oxforddigitalattendance.Database.AppDb;
import com.asifapps.oxforddigitalattendance.Database.Daos.AttendanceDao;
import com.asifapps.oxforddigitalattendance.Database.Entities.Attendance;
import com.asifapps.oxforddigitalattendance.Utils.DateTimeHelper;
import com.asifapps.oxforddigitalattendance.Utils.SmsBroadCastReceiver;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;

public class SmsSenderActivity extends AppCompatActivity {

    List<Attendance> attendances;
    AttendanceDao attendanceDao;
    String currentDate;
    boolean msgStatus = false;
    int time = 0;

    String attStatus = "P";
    List<Attendance> attendancesCopy;

    SmsManager smsManager;

    BroadcastReceiver smsResult;

    String SENT = "SMS_SENT";
    String DELIVERED = "SMS_DELIVERED";

    TextView smsCounterText;
    Button fTimeSmsBtn;
    Button sTimeSmsBtn;
    ProgressBar progressBar;

    int totalMessagesToSent = 0;
    int leftMessages = 0;

    int sentSmsCounter = 0;

    Attendance tempAttendance;
    int msgSize;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sms_sender);
        smsCounterText = findViewById(R.id.smsCountText);
        progressBar = findViewById(R.id.progressBar);
        progressBar.setVisibility(View.INVISIBLE);

        currentDate = DateTimeHelper.GetCurrentDate();

        AppDb db = AppDb.getDatabase(this);
        attendanceDao = db.attendanceDao();
        attendances = new ArrayList<>();

        sTimeSmsBtn = findViewById(R.id.sTimeSmsBtn);
        fTimeSmsBtn = findViewById(R.id.fTimeSmsBtn);

        //registerBrodcaster();
//        sentPI = PendingIntent.getBroadcast(this, 0, new Intent(SENT), 0);

        // getAttendance(null);
    }

    public void sendFirstTimeSms(View v) {
        time = 0;

        getAttendance();
    }

    public void sendSecondTimeSms(View v) {
        time = 1;

        getAttendance();
    }

    public void getAttendance() {

        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... voids) {
                if (time == 0) {
                    attendancesCopy = attendanceDao.getFirstTimeAttendancesWithStatus(currentDate, attStatus, msgStatus);
                    return null;
                }

                attendancesCopy = attendanceDao.getSecondTimeAttendancesWithStatus(currentDate, attStatus, msgStatus);
                return null;
            }

            @Override
            protected void onPostExecute(Void aVoid) {
                super.onPostExecute(aVoid);

                totalMessagesToSent = attendancesCopy.size();
                leftMessages = totalMessagesToSent;

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (leftMessages > 0) {
                            progressBar.setVisibility(View.VISIBLE);
                            fTimeSmsBtn.setClickable(false);
                            sTimeSmsBtn.setClickable(false);

                            sentSmsCounter = 0;
                        }

                        smsCounterText.setText("Pending messages: " + totalMessagesToSent);
                        sendSMS2();
                        //Toast.makeText(getApplicationContext(), String.valueOf(attendancesCopy.size()), Toast.LENGTH_LONG).show();
                    }
                });
            }
        }.execute();
    }

//    public void sendNextMsg() {
//
//        int size = attendancesCopy.size();
//
//        if (size == 0) {
//            Toast.makeText(getApplicationContext(), "size is 0", Toast.LENGTH_LONG).show();
//            return;
//        }
//
//        smsCounter++;
//        smsCounterText.setText(Integer.toString(smsCounter));
//
//        String msgText = "";
//
//        String st1 = "OXFORD School & College Bakak Khel\n";
//
//        String st2 = "%s  طالب علم\n" +
//                "سکول میں داخل ہوچکا ہے۔";
//        String st3 = "%s  طالب علم\n" +
//                "سکول سے نکل چکا ہے۔";
//
//        Attendance attendance = attendancesCopy.get(0);
//
//        if (time == 0) {
//            msgText = st1 + String.format(st2, attendance.Name);
//        } else {
//            msgText = st1 + String.format(st3, attendance.Name);
//        }
//
//        // STEP-1___
//        // SEND PendingIntent
//        String SENT = "SMS_SENT";
//        String DELIVERED = "SMS_DELIVERED";
//
//        PendingIntent sentPI = PendingIntent.getBroadcast(this, 0, new Intent(SENT).putExtra("attId", attendance.AttId), 0);
//        PendingIntent deliveredPI = PendingIntent.getBroadcast(this, 0, new Intent(DELIVERED), 0);
//
//        smsManager.sendTextMessage(attendance.Phone, null, msgText, sentPI,deliveredPI);
//
//        attendancesCopy.remove(0);
//    }

    private void updateSmsStatus() {
        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... voids) {
                attendanceDao.updateAttendances(attendances);
                return null;
            }

            @Override
            protected void onPostExecute(Void aVoid) {
                super.onPostExecute(aVoid);

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(getApplicationContext(), "updated", Toast.LENGTH_LONG).show();
                    }
                });
            }
        }.execute();
    }

    private void registerBrodcaster() {
        smsResult = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                // int index = intent.getIntExtra("index", -1);
                int resultCode = getResultCode();

                leftMessages--;

                switch (resultCode) {
                    case SmsManager.RESULT_ERROR_NO_SERVICE:
                    case SmsManager.RESULT_ERROR_GENERIC_FAILURE:
                    case SmsManager.RESULT_ERROR_RADIO_OFF:
                        Toast.makeText(getApplicationContext(), "Network error, try again.", Toast.LENGTH_LONG).show();
                        leftMessages = 0;
                        attendancesCopy.clear();
                        break;
                    case SmsManager.RESULT_ERROR_NULL_PDU:
                        // if phone no is wrong then try on next one;
                        break;
                }

                if (resultCode == Activity.RESULT_OK) {
                    Toast.makeText(getApplicationContext(), "sent", Toast.LENGTH_LONG).show();

                    if (time == 0) {
                        tempAttendance.EntranceMsgSent = true;
                    } else {
                        tempAttendance.LeaveMsgSent = true;
                    }

                    attendances.add(tempAttendance);
                    sentSmsCounter++;
                }

                if (leftMessages <= 0) {
                    messageSendingProcessCompleted();
                } else {
                    sendSMS2();
                }
            }
        };

        getBaseContext().registerReceiver(smsResult, new IntentFilter(SENT));
    }

    private void registerBrodcaster2() {
        smsResult = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                // int index = intent.getIntExtra("index", -1);
                int resultCode = getResultCode();

                msgSize--;

                switch (resultCode) {
                    case SmsManager.RESULT_ERROR_NO_SERVICE:
                    case SmsManager.RESULT_ERROR_GENERIC_FAILURE:
                    case SmsManager.RESULT_ERROR_RADIO_OFF:
                        Toast.makeText(getApplicationContext(), "Network error, try again.", Toast.LENGTH_LONG).show();
                        leftMessages = 0;
                        msgSize = 0;
                        attendancesCopy.clear();
                        break;
                    case SmsManager.RESULT_ERROR_NULL_PDU:
                        msgSize = 0;
                        // if phone no is wrong then try on next one;
                        break;
                }

                if (msgSize <= 0) {

                    leftMessages--;

                    if (resultCode == Activity.RESULT_OK) {
                        Toast.makeText(getApplicationContext(), "sent", Toast.LENGTH_LONG).show();

                        if (time == 0) {
                            tempAttendance.EntranceMsgSent = true;
                        } else {
                            tempAttendance.LeaveMsgSent = true;
                        }

                        attendances.add(tempAttendance);
                        sentSmsCounter++;
                    }

                    if (resultCode != Activity.RESULT_OK ) {
                        Toast.makeText(getApplicationContext(), "failed!", Toast.LENGTH_LONG).show();
                    }

                    if (leftMessages <= 0) {
                        messageSendingProcessCompleted();
                    } else {
                        sendSMS2();
                    }

                }
            }
        };

        getBaseContext().registerReceiver(smsResult, new IntentFilter(SENT));
    }

    private void sendSMS2() {
        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            //e.printStackTrace();
        }

        int size = attendancesCopy.size();

        if (size == 0) {
            messageSendingProcessCompleted();
            return;
        }

        String msgText = "";

        String st1 = "\nTime: (%s) \nDate: (" + tempAttendance.AttDate + ")" +
                "\n\nThis message has been sent from OXFORD digital attendance system\n" +
                "Developed by: Asif Nawaz Khan";

        String st2 = "Student \"%s\" has entered into the school.";
        String st3 = "Student \"%s\" has left for home.";

        tempAttendance = attendancesCopy.get(0);
        attendancesCopy.remove(0);

        if (time == 0) {
            msgText = String.format(st2, tempAttendance.Name) + String.format(st1, tempAttendance.EntranceTime);
        } else {
            msgText = String.format(st3, tempAttendance.Name) + String.format(st1, tempAttendance.LeaveTime);
        }

        SmsManager sms = SmsManager.getDefault();

        // ArrayList<String> msgParts = sms.divideMessage(msgText);
        // msgSize = msgParts.size();

//        ArrayList<PendingIntent> pendingIntents = new ArrayList<>();
//
//        for (int i = 0; i < size; i++) {
//            PendingIntent pi = PendingIntent.getBroadcast(this, i, new Intent(SENT), 0);
//            pendingIntents.add(pi);
//        }
//
        PendingIntent sentPI = PendingIntent.getBroadcast(this, 0, new Intent(SENT).putExtra("attId", tempAttendance.AttId), 0);
//      PendingIntent deliveredPI = PendingIntent.getBroadcast(this, 0, new Intent(DELIVERED), 0);

        try {
             sms.sendTextMessage(tempAttendance.Phone, null, msgText, sentPI, null);
            // sms.sendMultipartTextMessage(tempAttendance.Phone, "", msgParts, pendingIntents, null);
        } catch (Exception e) {
            Toast.makeText(this, e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    private void messageSendingProcessCompleted() {
        // Toast.makeText(getApplicationContext(), "Message Sending process completed; " + smsCounter + " messages sent", Toast.LENGTH_LONG).show();
        smsCounterText.setText("Message Sending process completed; " + sentSmsCounter + "/" + totalMessagesToSent + " messages sent ");
        progressBar.setVisibility(View.INVISIBLE);
        fTimeSmsBtn.setClickable(true);
        sTimeSmsBtn.setClickable(true);

        if (attendances.size() != 0) {
            updateSmsStatus();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        registerBrodcaster();
    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(smsResult);
    }

    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.options_menu, menu);
        return true;
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
            case R.id.sendSms:
                startActivity(new Intent(this, SmsSenderActivity.class));
                break;
        }
        return true;
    }
}
