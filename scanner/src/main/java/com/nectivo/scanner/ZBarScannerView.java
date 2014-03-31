package com.nectivo.scanner;

import android.content.Context;
import android.hardware.Camera;
import android.text.TextUtils;
import android.util.AttributeSet;

import net.sourceforge.zbar.Config;
import net.sourceforge.zbar.Image;
import net.sourceforge.zbar.ImageScanner;
import net.sourceforge.zbar.Symbol;
import net.sourceforge.zbar.SymbolSet;

/**
 * Created by piobab on 28.03.2014.
 */
public class ZBarScannerView extends ScannerView {
    public interface ResultHandler {
        public void handleResult(Result result);
    }

    static {
        System.loadLibrary("iconv");
    }

    private ImageScanner mScanner;
    private ResultHandler mResultHandler;

    public ZBarScannerView(Context context) {
        super(context);
        setupScanner(null);
    }

    public ZBarScannerView(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
        setupScanner(null);
    }

    /**
     * Specify recognized codes types.
     * @param codeTypes list of codes types from #net.sourceforge.zbar.Symbol
     */
    public void setCodeTypes(int[] codeTypes) {
        setupScanner(codeTypes);
    }

    private void setupScanner(int[] symbols) {
        mScanner = new ImageScanner();
        // Set x and y density to one in order to support rotated qr code
        mScanner.setConfig(0, Config.X_DENSITY, 1);
        mScanner.setConfig(0, Config.Y_DENSITY, 1);

        if (symbols != null) {
            mScanner.setConfig(Symbol.NONE, Config.ENABLE, 0);
            for (int symbol : symbols) {
                mScanner.setConfig(symbol, Config.ENABLE, 1);
            }
        }
    }

    /**
     * Register callback in order to receive data from scanner.
     * @param resultHandler
     */
    public void setResultHandler(ResultHandler resultHandler) {
        mResultHandler = resultHandler;
    }

    @Override
    public void onPreviewFrame(byte[] data, Camera camera) {
        Camera.Parameters parameters = camera.getParameters();
        Camera.Size size = parameters.getPreviewSize();
        int width = size.width;
        int height = size.height;

        Image barcode = new Image(width, height, "Y800");
        barcode.setData(data);

        int result = mScanner.scanImage(barcode);

        if (result != 0) {
            stopCamera();
            if(mResultHandler != null) {
                SymbolSet syms = mScanner.getResults();
                Result rawResult = new Result();
                for (Symbol sym : syms) {
                    String symData = sym.getData();
                    if (!TextUtils.isEmpty(symData)) {
                        rawResult.setContent(symData);
                        rawResult.setCodeType(sym.getType());
                        break;
                    }
                }
                mResultHandler.handleResult(rawResult);
            }
        } else {
            camera.setOneShotPreviewCallback(this);
        }
    }
}