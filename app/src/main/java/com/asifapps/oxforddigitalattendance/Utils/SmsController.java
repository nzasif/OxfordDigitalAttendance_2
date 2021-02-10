package com.asifapps.oxforddigitalattendance.Utils;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.AsyncTask;
import android.telephony.SmsManager;
import android.widget.Toast;

import com.asifapps.oxforddigitalattendance.Database.AppDb;
import com.asifapps.oxforddigitalattendance.Database.Daos.AttendanceDao;

public class SmsController {

    private Context context;
    private AttendanceDao attendanceDao;


    String msgText = "";

    SmsManager sms;

    public SmsController(Context con) {
        context = con;

        AppDb db = AppDb.getDatabase(context);

        attendanceDao = db.attendanceDao();

        //Get the SmsManager instance and call the sendTextMessage method to send message
        sms = SmsManager.getDefault();
    }

    public boolean SendMsg(String name, final Integer attId, String _class, String phone, final Integer time) {

        String st1 = "OXFORD School & College Bakak Khel\n";

        String st2 = "%s  طالب علم\n" +
                "سکول میں داخل ہوچکا ہے۔";
        String st3 = "%s  طالب علم\n" +
                "سکول سے نکل چکا ہے۔";;

        if (time == 0) {
         msgText = st1 + String.format(st2, name);
        } else {
            msgText = st1 + String.format(st3, name);
        }

        // Intent Filter Tags for SMS SEND and DELIVER
        String SENT = "SMS_SENT";
        String DELIVERED = "SMS_DELIVERED";

        // STEP-1___
        // SEND PendingIntent
        final PendingIntent sentPI = PendingIntent.getBroadcast(context, 0, new Intent(SENT), 0);

        sms.sendTextMessage(phone, null, msgText, sentPI,null);

        return true;
    }

    private void updateSmsStatus(final Integer attId, final Integer time) {
        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... voids) {
                if (time == 0)
                    attendanceDao.upadateEntranceMsgSent(attId, true);
                else
                    attendanceDao.upadateLeaveMsgSent(attId, true);
                return null;
            }
        }.execute();
    }
}
