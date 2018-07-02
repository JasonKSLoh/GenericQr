package com.lohjason.genericqr.ui.scanner;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.ImageFormat;
import android.graphics.Rect;
import android.graphics.YuvImage;
import android.util.SparseArray;

import com.google.android.gms.vision.Detector;
import com.google.android.gms.vision.Frame;
import com.google.android.gms.vision.barcode.Barcode;
import com.google.android.gms.vision.barcode.BarcodeDetector;

import java.io.ByteArrayOutputStream;

/**
 * ConstrainedBarcodeDetector
 * Created by jason on 26/6/18.
 */
public class ConstrainedBarcodeDetector extends Detector<Barcode> {

    private BarcodeDetector delegateDetector;
    private int percentWidth;

    ConstrainedBarcodeDetector(BarcodeDetector barcodeDetector, int percentWidth) {
        delegateDetector = barcodeDetector;
        this.percentWidth = percentWidth;
    }

    @Override
    public SparseArray<Barcode> detect(Frame frame) {
        return delegateDetector.detect(frame);
    }

    @Override
    public void receiveFrame(Frame frame) {
        int width     = frame.getMetadata().getWidth();
        int height    = frame.getMetadata().getHeight();
        int boxWidth  = width / 2 * percentWidth / 100;
        int boxHeight = height / 2 * percentWidth / 100;
        int boxDimens = boxWidth < boxHeight ? boxWidth : boxHeight;
        int right     = (width / 2) + boxDimens;
        int left      = (width / 2) - boxDimens;
        int bottom    = (height / 2) + boxDimens;
        int top       = (height / 2) - boxDimens;

        YuvImage              yuvImage              = new YuvImage(frame.getGrayscaleImageData().array(), ImageFormat.NV21, width, height, null);
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        yuvImage.compressToJpeg(new Rect(left, top, right, bottom), 100, byteArrayOutputStream);
        byte[] jpegArray = byteArrayOutputStream.toByteArray();
        Bitmap bitmap    = BitmapFactory.decodeByteArray(jpegArray, 0, jpegArray.length);

        Frame croppedFrame = new Frame.Builder()
                .setBitmap(bitmap)
                .setRotation(frame.getMetadata().getRotation())
                .build();

        delegateDetector.receiveFrame(croppedFrame);
    }

    @Override
    public boolean isOperational() {
        return delegateDetector.isOperational();
    }

    @Override
    public boolean setFocus(int id) {
        return delegateDetector.setFocus(id);
    }


}
