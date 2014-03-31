ZBarScanner
=======

Use this library for easy BarCode or QrCode integration.

Installation
------------

Add the library as git submodule to your project.

Simple Usage
------------

1.) Add camera permission to your AndroidManifest.xml file:

```xml
<uses-permission android:name="android.permission.CAMERA" />
<uses-feature android:name="android.hardware.camera" />
<uses-feature android:name="android.hardware.camera.autofocus" />
```

2.) Add ZBarScannerView to your layout:

```xml
<com.nectivo.scanner.ZBarScannerView
        android:layout_width="match_parent"
        android:layout_height="match_parent"
        android:id="@+id/zbar_scanner_view"/>
```

ZBarScannerView does not have to fill the whole screen. 
Use listSupportedPreviewSizes() and then specify required width and height for landscape and portrait mode. 

Remember: 
All preview sizes are given for landscape mode so for portrait mode we have to replace width with height.

3.) A very basic activity would look like this:

```java
public class ZBarScannerActivity extends Activity implements ZBarScannerView.ResultHandler {
    private ZBarScannerView mScannerView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_zbar_scanner);
        mScannerView = (ZBarScannerView) findViewById(R.id.zbar_scanner_view);
    }

    @Override
    public void onResume() {
        super.onResume();
        mScannerView.setResultHandler(this);     // Register ourselves as a handler for scan results.
        mScannerView.startFrontCamera();         // Start back or front camera on resume
    }

    @Override
    public void onPause() {
        super.onPause();
        mScannerView.stopCamera();               // Stop camera on pause
    }

    @Override
    public void handleResult(Result result) {
        // Read scanned code.
    }
}

```

4.) Use codes types from package net.sourceforge.zbar:

```java
    Symbol.NONE
    Symbol.PARTIAL
    Symbol.EAN8
    Symbol.UPCE
    Symbol.ISBN10
    Symbol.UPCA
    Symbol.EAN13
    Symbol.ISBN13
    Symbol.I25
    Symbol.DATABAR
    Symbol.DATABAR_EXP
    Symbol.CODABAR
    Symbol.CODE39
    Symbol.PDF417
    Symbol.QRCODE
    Symbol.CODE93
    Symbol.CODE128
```