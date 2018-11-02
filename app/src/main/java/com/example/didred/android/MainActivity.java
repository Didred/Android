package com.example.didred.android;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
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
        TextView versionView = findViewById(R.id.versionView);
        TextView imeiView = findViewById(R.id.imeiView);
        versionView.setText(String.format("%s: %s", R.string.version, getVersion()));
        imeiView.setText(String.format("%s: %s", R.string.imei, getIMEI()));
    }

    protected String getVersion() {
        return BuildConfig.VERSION_NAME;
    }

    protected String getIMEI() {
        TelephonyManager telephonyManager = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE)
                != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.READ_PHONE_STATE)) {
                AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);
                dialogBuilder.setMessage(R.string.explanation);
                dialogBuilder.setPositiveButton(R.string.close, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialogInterface, int id) {
                        ActivityCompat.requestPermissions(MainActivity.this,
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
            return "";
        }
        else {
            return telephonyManager != null ? telephonyManager.getDeviceId() : "";
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case PERMISSIONS_REQUEST_READ_PHONE_STATE: {
                TextView versionView = findViewById(R.id.versionView);
                TextView imeiView = findViewById(R.id.imeiView);
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    versionView.setText(String.format("%s: %s",R.string.version, getVersion()));
                    imeiView.setText(String.format("%s: %s", R.string.imei, getIMEI()));
                }
                else {
                    versionView.setText(String.format("%s: %s", R.string.version, getVersion()));
                }
            }
        }
    }
}
