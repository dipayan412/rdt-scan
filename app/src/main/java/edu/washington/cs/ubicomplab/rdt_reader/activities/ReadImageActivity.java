package edu.washington.cs.ubicomplab.rdt_reader.activities;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.Image;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Environment;
import android.support.annotation.RequiresApi;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

import org.opencv.android.Utils;
import org.opencv.core.Mat;

import java.io.File;

import edu.washington.cs.ubicomplab.rdt_reader.R;
import edu.washington.cs.ubicomplab.rdt_reader.core.ImageProcessor;
import edu.washington.cs.ubicomplab.rdt_reader.core.RDTCaptureResult;
import edu.washington.cs.ubicomplab.rdt_reader.core.RDTInterpretationResult;
import edu.washington.cs.ubicomplab.rdt_reader.utils.ImageUtil;
import edu.washington.cs.ubicomplab.rdt_reader.views.ImageQualityView;

import static org.opencv.android.OpenCVLoader.initDebug;

public class ReadImageActivity extends AppCompatActivity {

    Context context;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_read_image);
        context = this;
        new ImageProcessAsyncTask().execute();
    }

    private class ImageProcessAsyncTask extends AsyncTask<Void, Void, Void> {

        @RequiresApi(api = Build.VERSION_CODES.N)
        @Override
        protected Void doInBackground(Void... voids) {
            File file = new File(Environment.getExternalStorageDirectory() + File.separator + "hires_input");
            File[] imageFileList = file.listFiles();
            initDebug();
            ImageProcessor processor = ImageProcessor.getInstance((Activity) context, "covid19-ghl");

            String fullResultStr = "";
            for(int i = 0; i < 4; i++) {
                int sample = 0;
                for(File imageFile: imageFileList) {
                    Bitmap bitmap = BitmapFactory.decodeFile(imageFile.getAbsolutePath());
                    Mat rgbaMat = new Mat();
                    Utils.bitmapToMat(bitmap, rgbaMat);
                    RDTCaptureResult captureResult = processor.assessImage(rgbaMat, true);
                    RDTInterpretationResult interpretationResult = processor.interpretRDT(captureResult.resultMat,
                            captureResult.boundary);
                    String resultString = "" + (interpretationResult.peaks.size() > 0 && interpretationResult.peaks.get(0) != null ? String.format("%.1f", interpretationResult.peaks.get(0)[3]) : "-1");
                    resultString += "," + (interpretationResult.peaks.size() > 1 ? String.format("%.1f", interpretationResult.peaks.get(1)[3]) : "-1");
                    resultString += "," + (interpretationResult.redPeaks.size() > 0 && interpretationResult.redPeaks.get(0) != null ? String.format("%.1f", interpretationResult.redPeaks.get(0)[3]) : "-1");
                    resultString += "," + (interpretationResult.redPeaks.size() > 1 ? String.format("%.1f", interpretationResult.redPeaks.get(1)[3]) : "-1");
                    Log.d(getLocalClassName(), imageFile.getAbsolutePath() + "\t" + resultString);

                    bitmap.recycle();
                    rgbaMat.release();
                    captureResult.resultMat.release();
                    interpretationResult.resultMat.release();

                    fullResultStr += sample + "," + imageFile.getName() + "," + resultString + "\n";

                    sample++;
                }

                fullResultStr = "";
            }
            Log.d("fullResultStr", fullResultStr);
            return null;
        }
    }
}