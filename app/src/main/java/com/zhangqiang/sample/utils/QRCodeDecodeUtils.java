package com.zhangqiang.sample.utils;

import android.graphics.Bitmap;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.BinaryBitmap;
import com.google.zxing.ChecksumException;
import com.google.zxing.DecodeHintType;
import com.google.zxing.FormatException;
import com.google.zxing.MultiFormatReader;
import com.google.zxing.NotFoundException;
import com.google.zxing.RGBLuminanceSource;
import com.google.zxing.Result;
import com.google.zxing.common.GlobalHistogramBinarizer;
import com.google.zxing.common.HybridBinarizer;
import com.google.zxing.qrcode.QRCodeReader;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class QRCodeDecodeUtils {

    public static final int MAX_DECODE_SIZE = 2048;
    public static final int MIN_DECODE_SIZE = 400;

    public static String decodeQRCode(String imageFilePath) throws NotFoundException {
        BitmapUtils.DecodeResult decodeResult = BitmapUtils.decodeBitmap(imageFilePath, MAX_DECODE_SIZE, MAX_DECODE_SIZE);
        try {
            return decodeQRCode(decodeResult.getBitmap());
        } catch (NotFoundException e) {
            return decodeQRCode(imageFilePath, decodeResult.getSampleSize() + 1);
        }
    }

    private static String decodeQRCode(String imageFilePath, int sampleSize) throws NotFoundException {
        Bitmap bitmap = BitmapUtils.decodeBitmap(imageFilePath, sampleSize);
        try {
            return decodeQRCode(bitmap);
        } catch (NotFoundException e) {
            if (bitmap.getWidth() < MIN_DECODE_SIZE || bitmap.getHeight() < MIN_DECODE_SIZE) {
                throw e;
            }
            return decodeQRCode(imageFilePath, sampleSize + 1);
        }
    }

    public static String decodeQRCode(Bitmap bitmap) throws NotFoundException {
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();
        int[] pixels = new int[width * height];
        bitmap.getPixels(pixels, 0, width, 0, 0, width, height);
        RGBLuminanceSource source = new RGBLuminanceSource(width, height, pixels);

        Map<DecodeHintType, Object> hints = new HashMap<>();
        List<BarcodeFormat> formats = new ArrayList<>();
        formats.add(BarcodeFormat.QR_CODE);
        hints.put(DecodeHintType.POSSIBLE_FORMATS, formats);
        try {
            return new MultiFormatReader()
                    .decode(new BinaryBitmap(
                            new HybridBinarizer(source)), hints).getText();
        } catch (NotFoundException e) {
            return new MultiFormatReader()
                    .decode(new BinaryBitmap(
                            new GlobalHistogramBinarizer(source)), hints).getText();
        }
    }
}
