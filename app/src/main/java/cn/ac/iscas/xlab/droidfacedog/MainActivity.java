package cn.ac.iscas.xlab.droidfacedog;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;

/**
 * Created by Nguyen on 5/20/2016.
 */

public class MainActivity extends AppCompatActivity {

    public static final String TAG = MainActivity.class.getSimpleName();

    private static final int RC_HANDLE_CAMERA_PERM_RGB = 1;

    private Context mContext;
    ImageButton btnCameraRGB;
    ImageButton btnXbotFace;
    ImageButton btnRegisterVIP;
    ImageButton btnSetting;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mContext = this;

        btnCameraRGB = (ImageButton) findViewById(R.id.id_ibt_facedetect);
        btnCameraRGB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int rc = ActivityCompat.checkSelfPermission(mContext, Manifest.permission.CAMERA);
                if (rc == PackageManager.PERMISSION_GRANTED) {
                    Intent intent = new Intent(mContext, FaceDetectRGBActivity.class);
                    startActivity(intent);
                } else {
                    requestCameraPermission(RC_HANDLE_CAMERA_PERM_RGB);
                }
            }
        });

        btnXbotFace = (ImageButton) findViewById(R.id.id_ibt_xbot_face);
        btnXbotFace.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int rc = ActivityCompat.checkSelfPermission(mContext, Manifest.permission.CAMERA);
                if (rc == PackageManager.PERMISSION_GRANTED) {
                    Intent intent = new Intent(mContext, XBotFace.class);
                    startActivity(intent);
                } else {
                    requestCameraPermission(RC_HANDLE_CAMERA_PERM_RGB);
                }
            }
        });

//        btnRegisterVIP = (Button) findViewById(R.id.id_ibt_vip);
//        btnRegisterVIP.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                int rc = ActivityCompat.checkSelfPermission(mContext, Manifest.permission.CAMERA);
//                if (rc == PackageManager.PERMISSION_GRANTED) {
//                    Intent intent = new Intent(mContext, btnRegisterVIP.class);
//                    startActivity(intent);
//                } else {
//                    requestCameraPermission(RC_HANDLE_CAMERA_PERM_RGB);
//                }
//            }
//        });

        btnSetting = (ImageButton) findViewById(R.id.id_ibt_settings);
        btnSetting.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                    Intent intent = new Intent(mContext, SettingsActivity.class);
                    // http://stackoverflow.com/questions/15172111/preferenceactivity-actionbar-home-icon-wont-return-home-unlike-et
                    startActivityForResult(intent, 1);
            }
        });

    }


    private void requestCameraPermission(final int RC_HANDLE_CAMERA_PERM) {
        Log.w(TAG, "Camera permission is not granted. Requesting permission");

        final String[] permissions = new String[]{Manifest.permission.CAMERA};

        ActivityCompat.requestPermissions(this, permissions, RC_HANDLE_CAMERA_PERM);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {

        if (grantResults.length != 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED && requestCode == RC_HANDLE_CAMERA_PERM_RGB) {
            Intent intent = new Intent(mContext, FaceDetectRGBActivity.class);
            startActivity(intent);
            return;
        }


        Log.e(TAG, "Permission not granted: results len = " + grantResults.length +
                " Result code = " + (grantResults.length > 0 ? grantResults[0] : "(empty)"));
    }

}
