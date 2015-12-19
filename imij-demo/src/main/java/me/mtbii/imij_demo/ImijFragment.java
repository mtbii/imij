package me.mtbii.imij_demo;

import android.app.Fragment;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
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

    private ImageView imageView;

//    public ImijFragment() {
//        // Empty constructor required for fragment subclasses
//    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_imij, container, false);
        int i = getArguments().getInt(ARG_IMIJ_NUMBER);
        String imijFeature = getResources().getStringArray(R.array.imij_array)[i];

        int imageId = getResources().getIdentifier("painting",
                "drawable", getActivity().getPackageName());

        imageView = (ImageView) rootView.findViewById(R.id.image);
        imageView.setImageResource(imageId);

        if(i > 0) {
            BitmapDrawable image = (BitmapDrawable) imageView.getDrawable();

            Bitmap bmp = image.getBitmap();
            Bitmap bmpOut = bmp.copy(Bitmap.Config.ARGB_8888, true);

            MainActivity activity = (MainActivity) getActivity();
            Imij imijContext = activity.getImijContext();

            long start = System.currentTimeMillis();

            switch(i) {
                case 1:
                    imijContext.grayscale(bmp, bmpOut);
                    break;

                case 2:
                    imijContext.gaussianBlur(bmp, bmpOut, 15);
                    break;

                case 3:
                    imijContext.meanBlur(bmp, bmpOut, 15);
                    break;

                case 4:
                    imijContext.grayscale(bmp, bmpOut);
                    imijContext.constantThreshold(bmpOut, bmpOut, 127, 255);
                    break;

                case 5:
                    imijContext.grayscale(bmp, bmpOut);
                    imijContext.adaptiveThreshold(bmpOut, bmpOut, 15, 255);
                    break;

                case 6:
                    imijContext.resize(bmp, bmpOut, 100, 100);
                    break;

                case 7:
                    imijContext.grayscale(bmp, bmpOut);
                    imijContext.sobel(bmpOut, bmpOut);
                    break;
            }

            Toast.makeText(activity, "Run time: " + (System.currentTimeMillis() - start) + " ms", Toast.LENGTH_SHORT).show();

            imageView.setImageBitmap(bmpOut);
        }

        getActivity().setTitle(imijFeature);

        return rootView;
    }
}
