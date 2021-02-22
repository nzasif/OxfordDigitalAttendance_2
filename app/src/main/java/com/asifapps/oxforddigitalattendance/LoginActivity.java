package com.asifapps.oxforddigitalattendance;

import android.content.Intent;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.asifapps.oxforddigitalattendance.Database.AppDb;
import com.asifapps.oxforddigitalattendance.Database.Daos.LoginDao;
import com.asifapps.oxforddigitalattendance.Database.Entities.Admin;
import com.asifapps.oxforddigitalattendance.Utils.DateTimeHelper;

import java.math.BigInteger;
import java.security.AlgorithmConstraints;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class LoginActivity extends AppCompatActivity {

    LoginDao loginDao;

    EditText userName;
    EditText password;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        userName = findViewById(R.id.userName);
        password = findViewById(R.id.password);

        new AsyncTask<Void, Void, Void>(){

            @Override
            protected Void doInBackground(Void... voids) {
                loginDao = AppDb.getDatabase(getApplicationContext()).loginDao();

                Admin admin = loginDao.getAdmin();

                if (admin == null) {
                    initAdmin(admin);
                } else {
                    checkPasswordUsage(admin);
                }
                return null;
            }
        }.execute();
    }

    public void login(View view) {
        new AsyncTask<Void, Void, Void>(){

            @Override
            protected Void doInBackground(Void... voids) {

                String n = userName.getText().toString();
                String p = password.getText().toString();

                Admin admin = loginDao.getAdmin(n, p);

                if (admin != null) {
                    checkPasswordUsage(admin);

                    incrPasswordUsage(admin);
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            startActivity(new Intent(getApplicationContext(), MainActivity.class));
                        }
                    });
                }
                return null;
            }
        }.execute();

    }

    private void initAdmin(Admin admin) {
        admin = new Admin();

        admin.UserName = "#123";

        Integer p = DateTimeHelper.GetCurrentMonth();
        Integer y = DateTimeHelper.GetCurrentYear();
        Integer pas = (p * 9 * 12349) + 94321 + y;

        admin.Password = Integer.toString(pas);

        admin.passwordUsage = 1;

        loginDao.updateAdmin(admin);
    }

    private void checkPasswordUsage(Admin admin) {
        if (admin.passwordUsage > 15) {
            resetPassword(admin);
        }
    }

    private void resetPassword(Admin admin) {
        Integer p = DateTimeHelper.GetCurrentMonth();
        Integer y = DateTimeHelper.GetCurrentYear();
        Integer pas = (p * 9 * 12349) + 94321 + y;

        admin.Password = Integer.toString(pas);

        loginDao.updateAdmin(admin);
    }

    private void incrPasswordUsage(Admin admin) {
        admin.passwordUsage += 1;

        loginDao.updateAdmin(admin);
    }
}
