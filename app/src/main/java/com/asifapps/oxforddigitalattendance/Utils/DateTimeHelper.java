package com.asifapps.oxforddigitalattendance.Utils;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class DateTimeHelper {
    // sqlite date and time stored as text in formate -> "yyyy-mm-dd hh:mm:ss.sss"
    public static String GetCurrentDate() {
        Calendar c = Calendar.getInstance();
        Integer d = c.get(Calendar.DAY_OF_MONTH);
        Integer m = c.get(Calendar.MONTH)+1;
        Integer y = c.get(Calendar.YEAR);

        return Integer.toString(y) + "-" + Integer.toString(m) + "-" + Integer.toString(d);
    }

    public static String GetCurrentDateTime() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-mm-dd hh:mm:ss", Locale.getDefault());
        String currentDateTime = sdf.format(new Date());

        return currentDateTime;
    }

    public static String GetCurrentTime() {
        SimpleDateFormat sdf = new SimpleDateFormat("hh:mm:ss", Locale.getDefault());
        String currentTime = sdf.format(new Date());

        return currentTime;
    }

    public static Integer DetermineTime() {
        return 1;
    }

    public static String getTodayDate() {
        return null;
    }
}
