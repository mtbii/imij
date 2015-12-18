package me.mtbii.imij_lib;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.support.v8.renderscript.*;

/**
 * Created by mtbii on 12/18/2015.
 */
public class Imij {

    RenderScript mRS;
    Allocation mInAllocation;
    Allocation mOutAllocation;

    public Imij(Context c) {
        setup(c);
    }

    private void setup(Context c) {
        mRS = RenderScript.create(c);
    }

    private void alloc(Bitmap inBmp, Bitmap outBmp) {
        if (mInAllocation != null && inBmp != null) {
            mInAllocation.destroy();
            mInAllocation = Allocation.createFromBitmap(mRS, inBmp);
        }

        if (mOutAllocation != null && outBmp != null) {
            mOutAllocation.destroy();
            mOutAllocation = Allocation.createFromBitmap(mRS, outBmp);
        }
    }

    private void copy(Bitmap out) {
        mOutAllocation.copyTo(out);
    }

    public void grayscale(Bitmap in, Bitmap out) {
        alloc(in, out);
        ScriptC_grayscale grayScript = new ScriptC_grayscale(mRS);
        grayScript.forEach_grayscale(mInAllocation, mOutAllocation);
        copy(out);
        grayScript.destroy();
    }

    public void gaussianBlur(Bitmap bmp, Bitmap bmpOut, int blockSize, float sigma) {

    }

    public void meanBlur(Bitmap bmp, Bitmap bmpOut, int blockSize) {
    }

    public void constantThreshold(Bitmap bmp, Bitmap bmpOut, int blockSize, int threshold, int maxValue) {
    }

    public void adaptiveThreshold(Bitmap bmp, Bitmap bmpOut, int blockSize, int maxValue) {
    }

    public void resize(Bitmap bmp, Bitmap bmpOut, int w, int h) {
    }

    public void edgeDetection(Bitmap bmp, Bitmap bmpOut, int lowThreshold, int highThrshold) {

    }
}
