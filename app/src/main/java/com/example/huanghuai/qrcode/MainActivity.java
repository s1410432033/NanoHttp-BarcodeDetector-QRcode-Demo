package com.example.huanghuai.qrcode;


import android.Manifest;
import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.net.Uri;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.os.Vibrator;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.util.SparseArray;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.widget.TextView;

import com.google.android.gms.vision.CameraSource;
import com.google.android.gms.vision.Detector;
import com.google.android.gms.vision.barcode.Barcode;
import com.google.android.gms.vision.barcode.BarcodeDetector;

import java.io.IOException;
import java.util.ArrayList;


public class MainActivity extends AppCompatActivity {

    SurfaceView surfaceView;
    CameraSource cameraSource;
    TextView qrCodeText;
    BarcodeDetector barcodeDetector;
    public static ArrayList<String> scannList;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        scannList = new ArrayList<>();
        String packageName = getApplication().getPackageName();
        Resources resources = getApplication().getResources();

        setContentView(resources.getIdentifier("activity_main", "layout", packageName));

        surfaceView = (SurfaceView) findViewById(resources.getIdentifier("qrCodeFind", "id", packageName));
        qrCodeText = (TextView) findViewById(resources.getIdentifier("qrCodeText", "id", packageName));

        barcodeDetector = new BarcodeDetector.Builder(this)
                .setBarcodeFormats(Barcode.QR_CODE)
                .build();

        cameraSource = new CameraSource.Builder(this, barcodeDetector)
                .setRequestedPreviewSize(640, 480)
                .setAutoFocusEnabled(true)
                .build();


        Intent intent = new Intent(this,ServerService.class);
        startService(intent);

        surfaceView.getHolder().addCallback(new SurfaceHolder.Callback() {
            @Override
            public void surfaceCreated(SurfaceHolder holder) {
                if (ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                    return;
                }
                try {
                    cameraSource.start(holder);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

            }

            @Override
            public void surfaceDestroyed(SurfaceHolder holder) {
                cameraSource.stop();
            }
        });


        barcodeDetector.setProcessor(new Detector.Processor<Barcode>() {
            @Override
            public void release() {
            }
            @Override
            public void receiveDetections(Detector.Detections<Barcode> detections) {
                final SparseArray<Barcode> qrCode = detections.getDetectedItems();

                if(qrCode.size()!=0){
                    qrCodeText.post(new Runnable() {
                        @Override
                        public void run() {
                            qrCodeText.setText(qrCode.valueAt(0).displayValue);
                            addInList(qrCode.valueAt(0).displayValue);
                        }
                    });
                }
            }
        });

    }

    private void addInList(String scannResult){
        if (scannList.isEmpty()){
            scannList.add(scannResult);
        }else{
            String a = scannList.get(scannList.size()-1);
            if (!a.equals(scannResult)) {
                scannList.add(scannResult);
            }
        }
            for(String s :scannList){
                Log.e("s", "onCreate: "+s);
            }
        Log.e("size", "addInList: "+scannList.size() );

    }
    @Override
    protected void onDestroy() {
        super.onDestroy();
        Intent intent = new Intent(this,ServerService.class);
        stopService(intent);
    }
}