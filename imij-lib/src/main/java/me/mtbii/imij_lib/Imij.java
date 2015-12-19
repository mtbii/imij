package me.mtbii.imij_lib;

import android.content.Context;
import android.graphics.Bitmap;
import android.support.v8.renderscript.Allocation;
import android.support.v8.renderscript.Element;
import android.support.v8.renderscript.RenderScript;
import android.support.v8.renderscript.ScriptIntrinsicBlur;
import android.support.v8.renderscript.ScriptIntrinsicConvolve3x3;

/**
 * Created by mtbii on 12/18/2015.
 */
public class Imij {

    RenderScript mRS;
    ScriptC_imij imijScript;
    Allocation mInAllocation;
    Allocation mOutAllocation;

    public Imij(Context c) {
        setup(c);
    }

    private void setup(Context c) {
        mRS = RenderScript.create(c);
        imijScript = new ScriptC_imij(mRS);
    }

    private void alloc(Bitmap inBmp, Bitmap outBmp) {
        if (mInAllocation != null) {
            mInAllocation.destroy();
            mInAllocation = null;
        }

        if (inBmp != null) {
            mInAllocation = Allocation.createFromBitmap(mRS, inBmp);
        }

        if (mOutAllocation != null) {
            mOutAllocation.destroy();
            mOutAllocation = null;
        }

        if (outBmp != null) {
            mOutAllocation = Allocation.createFromBitmap(mRS, outBmp);
        }

    }

    private void copy(Bitmap out) {
        mOutAllocation.copyTo(out);
    }

    public void grayscale(Bitmap bmp, Bitmap bmpOut) {
        if (bmp.getConfig() != Bitmap.Config.ARGB_8888 || bmpOut.getConfig() != Bitmap.Config.ARGB_8888) {
            throw new IllegalArgumentException("Input and output bitmaps must be 1 channel, 8-bit configuration");
        }

        alloc(bmp, bmpOut);
        imijScript.forEach_grayscale(mInAllocation, mOutAllocation);
        copy(bmpOut);
    }

    public void gaussianBlur(Bitmap bmp, Bitmap bmpOut, int blockSize) {
        if (bmp.getConfig() != Bitmap.Config.ARGB_8888 || bmpOut.getConfig() != Bitmap.Config.ARGB_8888) {
            throw new IllegalArgumentException("Input and output bitmaps must be 1 channel, 8-bit configuration");
        }

        alloc(bmp, bmpOut);

        ScriptIntrinsicBlur script = ScriptIntrinsicBlur.create(mRS, Element.U8_4(mRS));
        script.setInput(mInAllocation);
        script.setRadius(blockSize);
        script.forEach(mOutAllocation);
        copy(bmpOut);
        script.destroy();
    }

    public void meanBlur(Bitmap bmp, Bitmap bmpOut, int blockSize) {
        if (bmp.getConfig() != Bitmap.Config.ARGB_8888 || bmpOut.getConfig() != Bitmap.Config.ARGB_8888) {
            throw new IllegalArgumentException("Input and output bitmaps must be 1 channel, 8-bit configuration");
        }

        alloc(bmp, bmpOut);

        imijScript.set_kernelSize(blockSize);
        imijScript.set_imgIn(mInAllocation);
        imijScript.set_width(bmp.getWidth());
        imijScript.set_height(bmp.getHeight());
        imijScript.forEach_meanBlur(mOutAllocation);

        copy(bmpOut);
    }

    public void constantThreshold(Bitmap bmp, Bitmap bmpOut, int threshold, int maxValue) throws IllegalArgumentException {
        if (bmp.getConfig() != Bitmap.Config.ARGB_8888 || bmpOut.getConfig() != Bitmap.Config.ARGB_8888) {
            throw new IllegalArgumentException("Input and output bitmaps must be 1 channel, 8-bit configuration");
        }

        alloc(bmp, bmpOut);

        imijScript.set_thresholdValue(threshold);
        imijScript.set_thresholdMaxValue(maxValue);
        imijScript.forEach_constantThreshold(mInAllocation, mOutAllocation);

        copy(bmpOut);
    }

    public void adaptiveThreshold(Bitmap bmp, Bitmap bmpOut, int blockSize, int maxValue) {
        if (bmp.getConfig() != Bitmap.Config.ARGB_8888 || bmpOut.getConfig() != Bitmap.Config.ARGB_8888) {
            throw new IllegalArgumentException("Input and output bitmaps must be 1 channel, 8-bit configuration");
        }

        alloc(bmp, bmpOut);

        imijScript.set_kernelSize(blockSize);
        imijScript.set_imgIn(mInAllocation);
        imijScript.set_width(bmp.getWidth());

        imijScript.set_height(bmp.getHeight());
        imijScript.set_thresholdMaxValue(maxValue);
        imijScript.forEach_adaptiveThreshold(mInAllocation, mOutAllocation);

        copy(bmpOut);
    }

    public void resize(Bitmap bmp, Bitmap bmpOut, int w, int h) {
        if (bmp.getConfig() != Bitmap.Config.ARGB_8888 || bmpOut.getConfig() != Bitmap.Config.ARGB_8888) {
            throw new IllegalArgumentException("Input and output bitmaps must be 1 channel, 8-bit configuration");
        }
    }

    public void sobel(Bitmap bmp, Bitmap bmpOut) {
        if (bmp.getConfig() != Bitmap.Config.ARGB_8888 || bmpOut.getConfig() != Bitmap.Config.ARGB_8888) {
            throw new IllegalArgumentException("Input and output bitmaps must be 1 channel, 8-bit configuration");
        }

        alloc(bmp, bmpOut);

        float[] sobelX = new float[]{
                -1, 0, 1,
                -2, 0, 2,
                -1, 0, 1
        };

        float[] sobelY = new float[]{
                -1, -2, -1,
                0, 0, 0,
                1, 2, 1
        };

        Allocation intermediateEdges = Allocation.createFromBitmap(mRS, bmpOut);

        ScriptIntrinsicConvolve3x3 script = ScriptIntrinsicConvolve3x3.create(mRS, Element.U8_4(mRS));
        script.setInput(mInAllocation);
        script.setCoefficients(sobelX);
        script.forEach(intermediateEdges);
        script.setCoefficients(sobelY);
        script.forEach(mOutAllocation);

        imijScript.set_sobelXEdges(intermediateEdges);
        imijScript.set_sobelYEdges(mOutAllocation);
        imijScript.forEach_sobelCombine(mOutAllocation);

        copy(bmpOut);
        script.destroy();
    }

    public void convolve(Bitmap bmp, Bitmap bmpOut, float[] kernel, int blockSize) {
        if (bmp.getConfig() != Bitmap.Config.ARGB_8888 || bmpOut.getConfig() != Bitmap.Config.ARGB_8888) {
            throw new IllegalArgumentException("Input and output bitmaps must be 1 channel, 8-bit configuration");
        }

        alloc(bmp, bmpOut);

        Allocation kernelAlloc = Allocation.createSized(mRS, Element.F32(mRS), blockSize * blockSize);
        kernelAlloc.copyFrom(kernel);

        imijScript.set_imgIn(mInAllocation);
        imijScript.set_width(bmp.getWidth());
        imijScript.set_height(bmp.getHeight());

        imijScript.forEach_convolve(mOutAllocation);
        copy(bmpOut);
    }
}
