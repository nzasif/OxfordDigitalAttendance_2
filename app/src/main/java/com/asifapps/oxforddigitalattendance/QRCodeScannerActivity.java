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
import com.asifapps.oxforddigitalattendance.Utils.PermissionHelper;
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

        currentDate = DateTimeHelper.GetCurrentDate();
    }

    public void onRadioButtonClicked(View view) {
        boolean checked = ((RadioButton) view).isChecked();
        // Check which radio button was clicked
        switch(view.getId()) {
            case R.id.firstTimeRadio:
                if (checked) {
                    time = 0;
                }
                break;
            case R.id.secTimeRadio:
                if (checked) {
                    time = 1;
                }
                break;
        }
    }

    private void takeAtt(final String rno, final String cl) {

        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... voids) {

                if (time == 0) {
                    attendance = attendanceDao.getFirstAttendance(rno, cl, currentDate);

                    if (attendance != null) {
                        attendance.AttStatus = Constants.pesent;
                        attendance.EntranceTime = DateTimeHelper.GetCurrentTime();

                        attendanceDao.updateAttendance(attendance);

                        playBeep();
                    }

                } else {
                    attendance = attendanceDao.getSecondAttendance(rno, cl, currentDate);

                    if (attendance != null) {
                        attendance.AttStatus = Constants.pesent;
                        attendance.LeaveTime = DateTimeHelper.GetCurrentTime();

                        attendanceDao.updateAttendance(attendance);

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

    @Override
    protected void onStart() {
        super.onStart();
        checkPermissions();
    }

    private boolean checkPermissions() {
        if (!PermissionHelper.checkPermissions(this)) {
            Toast.makeText(this, "Permissions denied.", Toast.LENGTH_LONG).show();
            return false;
        }

        return true;
    }
}
