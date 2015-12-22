package me.mtbii.imij_demo;

import android.app.Fragment;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Toast;

import me.mtbii.imij_lib.Imij;

/**
 * Created by mtbii on 12/18/2015.
 */
public class ImijFragment extends Fragment {
    public static final String ARG_IMIJ_NUMBER = "planet_number";

    private boolean isProcessing;
    //private ProgressDialog progress;
    private View mFragmentView;
    private ImageView mImageView;
    private String mImijFeatureTitle;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        mFragmentView = inflater.inflate(R.layout.fragment_imij, container, false);
        int i = getArguments().getInt(ARG_IMIJ_NUMBER);
        mImijFeatureTitle = getResources().getStringArray(R.array.imij_array)[i];

        mImageView = (ImageView) mFragmentView.findViewById(R.id.image);
        isProcessing = false;

        process(i);
        getActivity().setTitle(mImijFeatureTitle);

        return mFragmentView;
    }

    private void process(final int filterNumber) {
        int imageId = getResources().getIdentifier("painting",
                "drawable", getActivity().getPackageName());

        mImageView.setImageResource(imageId);
        final MainActivity activity = (MainActivity) getActivity();
        activity.showProgressDialog(mImijFeatureTitle);

        if (filterNumber > 0) {
            final BitmapDrawable image = (BitmapDrawable) mImageView.getDrawable();
            final Imij imijContext = activity.getImijContext();

            final Bitmap bmp = image.getBitmap();
            final Bitmap bmpOut = bmp.copy(Bitmap.Config.ARGB_8888, true);
            final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());


            new Thread(new Runnable() {

                @Override
                public void run() {
                    if (!isProcessing) {
                        isProcessing = true;
                        final long start = System.currentTimeMillis();

                        switch (filterNumber) {
                            case 1:
                                imijContext.grayscale(bmp, bmpOut);
                                break;

                            case 2:
                                imijContext.gaussianBlur(bmp, bmpOut, Integer.parseInt(prefs.getString(getString(R.string.pref_gaussianBlurRadius), "15")));
                                break;

                            case 3:
                                imijContext.meanBlur(bmp, bmpOut, Integer.parseInt(prefs.getString(getString(R.string.pref_meanBlurRadius), "15")));
                                break;

                            case 4:
                                imijContext.grayscale(bmp, bmpOut);
                                imijContext.constantThreshold(bmpOut, bmpOut, Integer.parseInt(prefs.getString(getString(R.string.pref_thresholdValue), "127")), 255);
                                break;

                            case 5:
                                imijContext.grayscale(bmp, bmpOut);
                                imijContext.adaptiveThreshold(bmpOut, bmpOut, Integer.parseInt(prefs.getString(getString(R.string.pref_adaptiveThresholdRadius), "15")), 255);
                                break;

                            case 6:
                                int dim = Integer.parseInt(prefs.getString(getString(R.string.pref_resizeDimensions), "100"));
                                imijContext.resize(bmp, bmpOut, dim, dim);
                                break;

                            case 7:
                                imijContext.grayscale(bmp, bmpOut);
                                imijContext.sobel(bmpOut, bmpOut);
                                break;
                        }

                        activity.runOnUiThread(new Runnable() {

                            @Override
                            public void run() {
                                activity.dismissProgressDialog();
                                Toast.makeText(activity, "Run time: " + (System.currentTimeMillis() - start) + " ms", Toast.LENGTH_SHORT).show();
                                mImageView.setImageBitmap(bmpOut);
                                mFragmentView.invalidate();
                                isProcessing = false;
                            }
                        });

                    }
                }
            }).start();
        } else {
            activity.dismissProgressDialog();
        }
    }

    public void refresh() {
        int i = getArguments().getInt(ARG_IMIJ_NUMBER);
        process(i);
    }
}
