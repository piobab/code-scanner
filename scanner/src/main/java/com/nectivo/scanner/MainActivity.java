package com.nectivo.scanner;

import android.app.Activity;
import android.os.Bundle;

public class MainActivity extends Activity implements ZBarScannerView.ResultHandler {

    private ZBarScannerView mScannerView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mScannerView = (ZBarScannerView) findViewById(R.id.zbar_scanner_view);
    }

    @Override
    public void onResume() {
        super.onResume();
        mScannerView.setResultHandler(this);    // Register ourselves as a handler for scan results.
        mScannerView.startBackCamera();         // Start camera on resume
    }

    @Override
    public void onPause() {
        super.onPause();
        mScannerView.stopCamera();              // Stop camera on pause
    }

    @Override
    public void handleResult(Result result) {
        //mScannerView.startBackCamera();
    }
}
