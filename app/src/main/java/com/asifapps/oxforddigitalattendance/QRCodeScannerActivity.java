package com.asifapps.oxforddigitalattendance;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.MediaPlayer;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.telephony.SmsManager;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Switch;
import android.widget.Toast;

import com.asifapps.oxforddigitalattendance.Database.AppDb;
import com.asifapps.oxforddigitalattendance.Database.Daos.AttendanceDao;
import com.asifapps.oxforddigitalattendance.Database.Daos.StudentDao;
import com.asifapps.oxforddigitalattendance.Database.Entities.Attendance;
import com.asifapps.oxforddigitalattendance.Database.Entities.Student;
import com.asifapps.oxforddigitalattendance.Utils.Constants;
import com.asifapps.oxforddigitalattendance.Utils.DateTimeHelper;
import com.asifapps.oxforddigitalattendance.Utils.QrDecoder;
import com.asifapps.oxforddigitalattendance.Utils.SmsController;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.ResultPoint;
import com.google.zxing.client.android.BeepManager;
import com.journeyapps.barcodescanner.BarcodeCallback;
import com.journeyapps.barcodescanner.BarcodeResult;
import com.journeyapps.barcodescanner.DecoratedBarcodeView;
import com.journeyapps.barcodescanner.DefaultDecoderFactory;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

public class QRCodeScannerActivity extends AppCompatActivity {

    private DecoratedBarcodeView barcodeView;
    private BeepManager beepManager;
    private String lastText;

    private AttendanceDao attendanceDao;
    private StudentDao studentDao;

    private RadioGroup attTimeRadio;

    MediaPlayer mediaPlayer = new MediaPlayer();

    private String currentDate;

    private Attendance attendance;

    private int time = 0;

    boolean isOld10th = false;

    private BarcodeCallback callback = new BarcodeCallback() {

        @Override
        public void barcodeResult(BarcodeResult result) {
            if(result.getText() == null || result.getText().equals(lastText)) {
                // Prevent duplicate scans
                return;
            }

            String codeText = result.getText();

            String[] data = QrDecoder.decodeQr(codeText);

            try {
                if (data != null) {
                    takeAtt(data[0], data[1]);

                    lastText = codeText;
                    barcodeView.setStatusText(codeText);
                }
            } catch (Exception e) {

                barcodeView.setStatusText("Wrong data: " + codeText);
            }
        }

        @Override
        public void possibleResultPoints(List<ResultPoint> resultPoints) {
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_qrcode_scanner);
        barcodeView = findViewById(R.id.barcode_scanner);
        Collection<BarcodeFormat> formats = Arrays.asList(BarcodeFormat.QR_CODE);
        barcodeView.getBarcodeView().setDecoderFactory(new DefaultDecoderFactory());

        barcodeView.initializeFromIntent(getIntent());
        barcodeView.getBarcodeView().getCameraSettings().setAutoFocusEnabled(true);
        barcodeView.decodeContinuous(callback);

        beepManager = new BeepManager(this);
        attTimeRadio = findViewById(R.id.attTimeRadio);

        AppDb db = AppDb.getDatabase(this);
        attendanceDao = db.attendanceDao();
        studentDao = db.studentDao();
        // registerBroadCastReceiver();
    }

    public void onRadioButtonClicked(View view) {
        boolean checked = ((RadioButton) view).isChecked();
        // Check which radio button was clicked
        switch(view.getId()) {
            case R.id.firstTimeRadio:
                if (checked) {
                    time = 0;
                    Toast.makeText(this, "first", Toast.LENGTH_LONG).show();
                }
                break;
            case R.id.secTimeRadio:
                if (checked) {
                    time = 1;
                    Toast.makeText(this, "se", Toast.LENGTH_LONG).show();
                }
                break;
        }
    }

    public void selectOld10th(View view) {
        isOld10th = ((Switch) view).isChecked();
    }

    private void takeAtt(final String rno, final String cl) {

        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... voids) {
                String _cl = cl;

                if (isOld10th) {
                    _cl = "old10th";
                }

                if (time == 0) {
                    attendance = attendanceDao.getFirstAttendance(rno, _cl, DateTimeHelper.GetCurrentDate());

                    if (attendance != null) {
                        attendance.AttStatus = Constants.pesent;
                        attendance.EntranceTime = DateTimeHelper.GetCurrentTime();

                        attendanceDao.updateAttendance(attendance);

                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {

                                // sendMsg(attendance.Name, attendance.AttId, "", attendance.Phone );
                            }
                        });

                        playBeep();
                    }

                } else {
                    attendance = attendanceDao.getSecondAttendance(rno, _cl, DateTimeHelper.GetCurrentDate());

                    if (attendance != null) {
                        attendance.AttStatus = Constants.pesent;
                        attendance.LeaveTime = DateTimeHelper.GetCurrentTime();

                        attendanceDao.updateAttendance(attendance);
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {

                                // sendMsg(attendance.Name, attendance.AttId, "", attendance.Phone );
                            }
                        });
                        playBeep();
                    }
                }

                return null;
            }
        }.execute();
    }

    public void playBeep() {
        mediaPlayer = MediaPlayer.create(getApplicationContext(), R.raw.beep2);
        mediaPlayer.start(); // no need to call prepare(); create() does that for you
    }

    @Override
    protected void onResume() {
        super.onResume();

        barcodeView.resume();
    }

    public void sendMsg(String name, final Integer attId, String _class, String phone) {

        String msgText = "";

        String st1 = "\n\n OXFORD School Baka Khel ";

        String st2 = "%s  طالب علم\n" +
                "سکول میں داخل ہوچکا ہے۔";
        String st3 = "%s  طالب علم\n" +
                "سکول سے نکل چکا ہے۔ وقت: s%";

        if (time == 0) {
            msgText =  String.format(st2, name) + st1;
        } else {
            msgText = String.format(st3, name) + st1;
        }

        Toast.makeText(this, "sneding...", Toast.LENGTH_LONG).show();

        SmsManager.getDefault().sendTextMessage(phone, null, msgText, null,null);
    }

    private void updateSmsStatus(final Integer attId, final Integer t) {
        if (attId == 0) {
            return;
        }

        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... voids) {
                if (t == 0)
                    attendanceDao.upadateEntranceMsgSent(attId, true);
                else
                    attendanceDao.upadateLeaveMsgSent(attId, true);
                return null;
            }
        }.execute();
    }

    @Override
    protected void onPause() {
        super.onPause();
        barcodeView.pause();
    }

    @Override
    public void onStop() {
        super.onStop();
    }

    public void pause(View view) {
        barcodeView.pause();
    }

    public void resume(View view) {
        barcodeView.resume();
    }

    public void triggerScan(View view) {
        barcodeView.decodeSingle(callback);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        return barcodeView.onKeyDown(keyCode, event) || super.onKeyDown(keyCode, event);
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
