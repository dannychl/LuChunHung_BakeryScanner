package com.example.bakeryscanner;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.example.bakeryscanner.utils.CheckCameraPermission;
import com.uuzuche.lib_zxing.activity.CodeUtils;

import pub.devrel.easypermissions.AfterPermissionGranted;
import pub.devrel.easypermissions.EasyPermissions;

public class MainActivity extends AppCompatActivity {

    public static final int REQUEST_CODE = 111;

    public static final int REQUEST_CAMERA_PERM = 101;
    public Button button1 = null, button2 = null, button3 = null;
    private String customerId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initView();
        initPermission();
    }

    private void initPermission() {
        String[] permissions = CheckCameraPermission.checkPermission(this);
        if (permissions.length == 0) {

        } else {
            ActivityCompat.requestPermissions(this, permissions, 100);
        }
    }

    private void initView() {

        button3 = (Button) findViewById(R.id.button3);
        button3.setOnClickListener(new ButtonOnClickListener(button3.getId()));
    }

    class ButtonOnClickListener implements View.OnClickListener{

        private int buttonId;

        public ButtonOnClickListener(int buttonId) {
            this.buttonId = buttonId;
        }

        @Override
        public void onClick(View v) {
            cameraTask(buttonId);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE) {
            if (null != data) {
                Bundle bundle = data.getExtras();
                if (bundle == null) {
                    return;
                }
                if (bundle.getInt(CodeUtils.RESULT_TYPE) == CodeUtils.RESULT_SUCCESS) {
                    String result = bundle.getString(CodeUtils.RESULT_STRING);

                    String[] separated = result.split(" ");
                    String check = separated[0];

                    if (check.equals("CI:")) {

                        customerId = separated[1];
                        Intent intent = new Intent(MainActivity.this, OrderDetails.class);
                        intent.putExtra("CustId", customerId);
                        startActivity(intent);
                    }
                    else{
                        Toast.makeText(this, "Order Not Found....", Toast.LENGTH_SHORT).show();
                    }

                } else if (bundle.getInt(CodeUtils.RESULT_TYPE) == CodeUtils.RESULT_FAILED) {
                    Toast.makeText(MainActivity.this, "Anlayze QR CODE failed", Toast.LENGTH_LONG).show();
                }
            }
        }
        else if (requestCode == REQUEST_CAMERA_PERM) {
            Toast.makeText(this, "Back from setting page now...", Toast.LENGTH_SHORT)
                    .show();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        // Forward results to EasyPermissions
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this);
    }


    @AfterPermissionGranted(REQUEST_CAMERA_PERM)
    public void cameraTask(int viewId) {
        if (EasyPermissions.hasPermissions(this, Manifest.permission.CAMERA)) {
            // Have permission, do the thing!
            onClick(viewId);
        } else {
            // Ask for one permission
            EasyPermissions.requestPermissions(this, "Request camera permission",
                    REQUEST_CAMERA_PERM, Manifest.permission.CAMERA);
        }
    }

    private void onClick(int buttonId) {
        switch (buttonId) {
            case R.id.button3:
                Intent intent = new Intent(MainActivity.this, SecondActivity.class);
                startActivityForResult(intent, REQUEST_CODE);
                break;
            default:
                break;
        }
    }

}