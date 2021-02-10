package com.asifapps.oxforddigitalattendance.Utils;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

public class SmsBroadCastReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        Integer attendanceId = intent.getIntExtra("attId", -1);

        switch (getResultCode()) {
            case Activity.RESULT_OK:
                // updateSmsStatus(attendanceId, index, time);
                Toast.makeText(context, "sent", Toast.LENGTH_LONG).show();
                break;

            default:
                Toast.makeText(context, "failed ", Toast.LENGTH_LONG).show();
        }
    }
}
