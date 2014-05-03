package com.nectivo.scanner;

import android.content.Context;
import android.graphics.Rect;
import android.hardware.Camera;
import android.util.AttributeSet;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.BinaryBitmap;
import com.google.zxing.DecodeHintType;
import com.google.zxing.MultiFormatReader;
import com.google.zxing.PlanarYUVLuminanceSource;
import com.google.zxing.ReaderException;
import com.google.zxing.Result;
import com.google.zxing.common.HybridBinarizer;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

/**
 * Created by pbabel on 03.05.2014.
 */
public class ZXingScannerView extends ScannerView {

    public interface ResultHandler {
        public void handleResult(Result result);
    }

    private MultiFormatReader mMultiFormatReader;
    private ResultHandler mResultHandler;

    public static final List<BarcodeFormat> BARCODE_FORMATS = new ArrayList<BarcodeFormat>();

    static {
        BARCODE_FORMATS.add(BarcodeFormat.UPC_A);
        BARCODE_FORMATS.add(BarcodeFormat.UPC_E);
        BARCODE_FORMATS.add(BarcodeFormat.EAN_13);
        BARCODE_FORMATS.add(BarcodeFormat.EAN_8);
        BARCODE_FORMATS.add(BarcodeFormat.RSS_14);
        BARCODE_FORMATS.add(BarcodeFormat.CODE_39);
        BARCODE_FORMATS.add(BarcodeFormat.CODE_93);
        BARCODE_FORMATS.add(BarcodeFormat.CODE_128);
        BARCODE_FORMATS.add(BarcodeFormat.ITF);
        BARCODE_FORMATS.add(BarcodeFormat.CODABAR);
        BARCODE_FORMATS.add(BarcodeFormat.QR_CODE);
        BARCODE_FORMATS.add(BarcodeFormat.DATA_MATRIX);
        BARCODE_FORMATS.add(BarcodeFormat.PDF_417);
    }

    public ZXingScannerView(Context context) {
        super(context);
        setupScanner(null);
    }

    public ZXingScannerView(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
        setupScanner(null);
    }

    /**
     * Specify recognized codes types.
     *
     * @param codeTypes list of codes types from ZXing library
     */
    public void setCodeTypes(List<BarcodeFormat> codeTypes) {
        setupScanner(codeTypes);
    }

    private void setupScanner(List<BarcodeFormat> symbols) {
        Map<DecodeHintType, Object> hints = new EnumMap<DecodeHintType, Object>(DecodeHintType.class);
        if(symbols == null) {
            symbols = BARCODE_FORMATS;
        }
        hints.put(DecodeHintType.POSSIBLE_FORMATS, symbols);
        mMultiFormatReader = new MultiFormatReader();
        mMultiFormatReader.setHints(hints);
    }

    /**
     * Register callback in order to receive data from scanner.
     *
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

        Result rawResult = null;
        PlanarYUVLuminanceSource source = buildLuminanceSource(data, width, height);

        if (source != null) {
            BinaryBitmap bitmap = new BinaryBitmap(new HybridBinarizer(source));
            try {
                rawResult = mMultiFormatReader.decodeWithState(bitmap);
            } catch (ReaderException re) {

            } catch (NullPointerException npe) {

            } catch (ArrayIndexOutOfBoundsException aoe) {

            } finally {
                mMultiFormatReader.reset();
            }
        }

        if (rawResult != null) {
            stopCamera();
            if (mResultHandler != null) {
                mResultHandler.handleResult(rawResult);
            }
        } else {
            camera.setOneShotPreviewCallback(this);
        }
    }

    public PlanarYUVLuminanceSource buildLuminanceSource(byte[] data, int width, int height) {
        Rect rect = new Rect(0, 0, width, height);
        if (rect == null) {
            return null;
        }
        // Go ahead and assume it's YUV rather than die.
        PlanarYUVLuminanceSource source = null;

        try {
            source = new PlanarYUVLuminanceSource(data, width, height, rect.left, rect.top,
                    rect.width(), rect.height(), false);
        } catch (Exception e) {
        }

        return source;
    }
}