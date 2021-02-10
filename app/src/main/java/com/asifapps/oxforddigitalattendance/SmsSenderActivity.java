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
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.asifapps.oxforddigitalattendance.Database.AppDb;
import com.asifapps.oxforddigitalattendance.Database.Daos.AttendanceDao;
import com.asifapps.oxforddigitalattendance.Database.Entities.Attendance;
import com.asifapps.oxforddigitalattendance.Utils.DateTimeHelper;
import com.asifapps.oxforddigitalattendance.Utils.SmsBroadCastReceiver;

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

    ProgressBar progressBar;

    int totalMessagesToSent;

    Attendance tempAttendance;

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

        smsManager = SmsManager.getDefault();

        registerBrodcaster();
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
        progressBar.setVisibility(View.VISIBLE);

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
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        sendSMS2();
                        //Toast.makeText(getApplicationContext(), String.valueOf(attendancesCopy.size()), Toast.LENGTH_LONG).show();
                    }
                });
            }
        }.execute();
    }

    int smsCounter = 0;

    public void sendNextMsg() {

        int size = attendancesCopy.size();

        if (size == 0) {
            Toast.makeText(getApplicationContext(), "size is 0", Toast.LENGTH_LONG).show();
            return;
        }

        smsCounter++;
        smsCounterText.setText(Integer.toString(smsCounter));

        String msgText = "";

        String st1 = "OXFORD School & College Bakak Khel\n";

        String st2 = "%s  طالب علم\n" +
                "سکول میں داخل ہوچکا ہے۔";
        String st3 = "%s  طالب علم\n" +
                "سکول سے نکل چکا ہے۔";

        Attendance attendance = attendancesCopy.get(0);

        if (time == 0) {
            msgText = st1 + String.format(st2, attendance.Name);
        } else {
            msgText = st1 + String.format(st3, attendance.Name);
        }

        // STEP-1___
        // SEND PendingIntent
        String SENT = "SMS_SENT";
        String DELIVERED = "SMS_DELIVERED";

        PendingIntent sentPI = PendingIntent.getBroadcast(this, 0, new Intent(SENT).putExtra("attId", attendance.AttId), 0);
        PendingIntent deliveredPI = PendingIntent.getBroadcast(this, 0, new Intent(DELIVERED), 0);

        smsManager.sendTextMessage(attendance.Phone, null, msgText, sentPI,deliveredPI);

        attendancesCopy.remove(0);
    }

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
                Integer attId = intent.getIntExtra("attId", -1);
                int resultCode = getResultCode();

                switch (resultCode) {
                    case Activity.RESULT_OK:
                        Toast.makeText(getApplicationContext(), "Ok", Toast.LENGTH_SHORT).show();
                        if (time == 0) {
                            tempAttendance.EntranceMsgSent = true;
                        } else {
                            tempAttendance.LeaveMsgSent = true;
                        }

                        attendances.add(tempAttendance);

                        smsCounter++;
                        break;
                        default:
                            Toast.makeText(getApplicationContext(), "failed", Toast.LENGTH_SHORT).show();
                }

                sendSMS2();
            }
        };

        getBaseContext().registerReceiver(smsResult, new IntentFilter(SENT));
    }

    private void sendSMS2() {

        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        int size = attendancesCopy.size();

        if (size == 0) {
            Toast.makeText(getApplicationContext(), "Message Sending process completed; total " + smsCounter + " messages sent", Toast.LENGTH_LONG).show();
            smsCounterText.setText("Message Sending process completed; total " + smsCounter + "/" + totalMessagesToSent + " messages sent ");
            progressBar.setVisibility(View.INVISIBLE);

            if (attendances.size() != 0) {
                updateSmsStatus();
            }

            return;
        }

        String msgText = "";

        String st1 = "(%s)\n\nOXFORD School Baka Khel";

        String st2 = "%s طالب علم\n" +
                "سکول میں داخل ہوچکاہے";
        String st3 = "%s طالب علم\n" +
                "سکول سے نکل چکاہے";

        tempAttendance = attendancesCopy.get(0);
        String[] fArray = tempAttendance.EntranceTime.split(":");
        String[] sArray = tempAttendance.LeaveTime.split(":");

        if (time == 0) {
            msgText = String.format(st2, tempAttendance.Name) + String.format(st1, fArray[0] + ":" + fArray[1]);
        } else {
            msgText = st1 + String.format(st3, tempAttendance.Name) + String.format(st1, sArray[0] + ":" + sArray[1]);
        }

        PendingIntent sentPI = PendingIntent.getBroadcast(this, 0, new Intent(SENT).putExtra("attId", tempAttendance.AttId), 0);
        PendingIntent deliveredPI = PendingIntent.getBroadcast(this, 0, new Intent(DELIVERED), 0);

        // ---when the SMS has been sent---
//        registerReceiver(new BroadcastReceiver() {
//            @Override
//            public void onReceive(Context arg0, Intent arg1) {
//
//                switch (getResultCode()) {
//
//                    case Activity.RESULT_OK:
//
//                        Toast.makeText(getBaseContext(), "SMS sent",
//                                Toast.LENGTH_SHORT).show();
//                        break;
//
//                    case SmsManager.RESULT_ERROR_GENERIC_FAILURE:
//
//                        Toast.makeText(getBaseContext(), "Generic failure",
//                                Toast.LENGTH_SHORT).show();
//                        break;
//
//                    case SmsManager.RESULT_ERROR_NO_SERVICE:
//
//                        Toast.makeText(getBaseContext(), "No service",
//                                Toast.LENGTH_SHORT).show();
//                        break;
//
//                    case SmsManager.RESULT_ERROR_NULL_PDU:
//
//                        Toast.makeText(getBaseContext(), "Null PDU",
//                                Toast.LENGTH_SHORT).show();
//                        break;
//
//                    case SmsManager.RESULT_ERROR_RADIO_OFF:
//
//                        Toast.makeText(getBaseContext(), "Radio off",
//                                Toast.LENGTH_SHORT).show();
//                        break;
//                }
//            }
//        }, new IntentFilter(SENT));
//
//        // ---when the SMS has been delivered---
//        registerReceiver(new BroadcastReceiver() {
//            @Override
//            public void onReceive(Context arg0, Intent arg1) {
//
//                switch (getResultCode()) {
//
//                    case Activity.RESULT_OK:
//
//                        Toast.makeText(getBaseContext(), "SMS delivered",
//                                Toast.LENGTH_SHORT).show();
//                        break;
//
//                    case Activity.RESULT_CANCELED:
//
//                        Toast.makeText(getBaseContext(), "SMS not delivered",
//                                Toast.LENGTH_SHORT).show();
//                        break;
//                }
//            }
//        }, new IntentFilter(DELIVERED));

        SmsManager sms = SmsManager.getDefault();
        try {
            sms.sendTextMessage(tempAttendance.Phone, null, msgText, sentPI, deliveredPI);
        } catch (Exception e) {
            Toast.makeText(this, e.getMessage(), Toast.LENGTH_LONG).show();
        }

        attendancesCopy.remove(0);
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
