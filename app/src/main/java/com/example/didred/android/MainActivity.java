package com.example.didred.android;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.support.v4.app.ActivityCompat;
import android.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.telephony.TelephonyManager;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity {
    static final int PERMISSIONS_REQUEST_READ_PHONE_STATE = 2375;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        TextView textView = findViewById(R.id.versionView);
        textView.setText(String.format("Version: %s\n\nIMEI: %s", getVersion(), getIMEI()));
    }

    protected String getVersion() {
        String version = "";
        try {
            PackageInfo pInfo = this.getPackageManager().getPackageInfo(getPackageName(), 0);
            version = pInfo.versionName;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return version;
    }

    protected String getIMEI() {
        TelephonyManager telephonyManager = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE)
                != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.READ_PHONE_STATE)) {
                final Activity thisActivity = this;
                AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(thisActivity);
                dialogBuilder.setMessage("The application requests permission to access your functions to show your IMEI.");
                dialogBuilder.setPositiveButton("close", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialogInterface, int id) {
                        ActivityCompat.requestPermissions(thisActivity,
                                new String[] { Manifest.permission.READ_PHONE_STATE },
                                PERMISSIONS_REQUEST_READ_PHONE_STATE);
                    }
                });

                dialogBuilder.show();
            }
            else {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.READ_PHONE_STATE},
                        PERMISSIONS_REQUEST_READ_PHONE_STATE);
            }
            return null;
        }
        else {
            return telephonyManager.getDeviceId();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case PERMISSIONS_REQUEST_READ_PHONE_STATE: {
                TextView textView = findViewById(R.id.versionView);
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    textView.setText(String.format("Version: %s\n\nIMEI: %s", getVersion(), getIMEI()));
                }
                else {
                    textView.setText(String.format("Version: %s", getVersion()));
                }
            }
        }
    }
}
