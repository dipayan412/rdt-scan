package edu.washington.cs.ubicomplab.rdt_reader.activities;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.media.Image;
import android.media.ImageReader;
import android.os.Bundle;
import android.util.Log;

import org.opencv.android.Utils;
import org.opencv.core.Mat;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.Serializable;

import edu.washington.cs.ubicomplab.rdt_reader.interfaces.ImageQualityViewListener;
import edu.washington.cs.ubicomplab.rdt_reader.views.ImageQualityView;
import edu.washington.cs.ubicomplab.rdt_reader.R;
import edu.washington.cs.ubicomplab.rdt_reader.core.RDTCaptureResult;
import edu.washington.cs.ubicomplab.rdt_reader.core.RDTInterpretationResult;
import edu.washington.cs.ubicomplab.rdt_reader.utils.ImageUtil;

import static edu.washington.cs.ubicomplab.rdt_reader.core.Constants.DEFAULT_RDT_NAME;

/**
 * The {@link android.app.Activity} for showing a real-time camera feed during image capture and
 * providing real-time feedback to the user
 * Note: In this example app, this activity is launched as an {@link Intent} from {@link MainActivity}
 * with the target RDT's name passed in the bundle to support multiple RDT designs simultaneously
 */
public class ImageQualityActivity extends Activity implements ImageQualityViewListener {
    ImageQualityView mImageQualityView;
    private String TAG="ImageQualityActivity";
    byte[] captureByteArray=new byte[0];
    byte[] windowByteArray=new byte[0];
    RDTInterpretationResult rdtinterpretresult;
    RDTCaptureResult rdtcaptureresult;
    long time;


    /**
     * {@link android.app.Activity} onCreate()
     * @param savedInstanceState: the bundle object in case this is launched from an intent
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Bundle b = getIntent().getExtras();
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_quality);

        // Prepare ImageQualityView
        mImageQualityView = findViewById(R.id.imageQualityView);
        mImageQualityView.setImageQualityViewListener(this);

        // Extract the target RDT's name
        if (b != null && b.containsKey("rdt_name")) {
            String rdtName = b.getString("rdt_name");
            mImageQualityView.setRDTName(rdtName);
        } else {
            mImageQualityView.setRDTName(DEFAULT_RDT_NAME);
        }
    }


    /**
     * {@link android.app.Activity} onResume()
     */
    @Override
    public void onResume() {
        super.onResume();
        mImageQualityView.onResume();
    }

    /**
     * {@link android.app.Activity} onPause()
     */
    @Override
    public void onPause() {
        super.onPause();
        mImageQualityView.onPause();
    }

    /**
     * {@link android.app.Activity} onBackPressed()
     * Launches the MainActivity as an Intent if that's how this Activity
     * was created in the first place
     */
    @Override
    public void onBackPressed() {
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
    }

    /**
     * {@link ImageQualityViewListener} onRDTCameraReady()
     */
    @Override
    public void onRDTCameraReady() {
    }

    /**
     * {@link ImageQualityViewListener} onRDTDetected()
     * Launches the {@link ImageResultActivity} if the candidate video frame is high quality
     * @param rdtCaptureResult: the current {@link RDTCaptureResult}
     * @param rdtInterpretationResult: the current {@link RDTInterpretationResult}
     * @param timeTaken: the time it took for the RDT to be detected
     * @return whether the app should continue letting the user capture an image
     */
    @Override
    public ImageQualityView.RDTDetectedResult onRDTDetected(
            final RDTCaptureResult rdtCaptureResult,
            final RDTInterpretationResult rdtInterpretationResult,
            final long timeTaken) {
        // The RDT was detected, but the candidate video frame
        // did not pass all of the quality checks
        if (!rdtCaptureResult.allChecksPassed || rdtInterpretationResult == null)
            return ImageQualityView.RDTDetectedResult.CONTINUE;


        // Pass the image quality and interpretation data to the new activity
        final ImageQualityActivity self = this;
        rdtcaptureresult=rdtCaptureResult;
        rdtinterpretresult=rdtInterpretationResult;
        time=timeTaken;

        captureByteArray = ImageUtil.matToByteArray(rdtCaptureResult.resultMat);
        windowByteArray = ImageUtil.matToByteArray(rdtInterpretationResult.resultMat);

       /* runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Intent i = new Intent(self, ImageResultActivity.class);
                i.addFlags(Intent.FLAG_ACTIVITY_FORWARD_RESULT);
                i.putExtra("captured", captureByteArray);
                i.putExtra("window", windowByteArray);
                i.putExtra("topLine", rdtInterpretationResult.topLine);
                i.putExtra("middleLine", rdtInterpretationResult.middleLine);
                i.putExtra("bottomLine", rdtInterpretationResult.bottomLine);
                i.putExtra("topLineName", rdtInterpretationResult.topLineName);
                i.putExtra("middleLineName", rdtInterpretationResult.middleLineName);
                i.putExtra("bottomLineName", rdtInterpretationResult.bottomLineName);
                i.putExtra("timeTaken", timeTaken);
                i.putExtra("hasTooMuchBlood", rdtInterpretationResult.hasTooMuchBlood);
                i.putExtra("numberOfLines", rdtInterpretationResult.numberOfLines);

                Bundle args = new Bundle();
                //modified signature to peaksArray
                args.putSerializable("peaksArray",(Serializable)rdtInterpretationResult.peaks);
                //red peak array packed into intent
                args.putSerializable("RedpeaksArray", (Serializable)rdtInterpretationResult.redPeaks);
                args.putSerializable("avgIntensities",(Serializable)rdtInterpretationResult.avgIntensities);
                i.putExtra("BUNDLE",args);

                startActivity(i);
            }
        });*/
        return ImageQualityView.RDTDetectedResult.STOP;
    }

    @Override
    public void onSingleImage(Mat hiresMat) {
        Log.d(TAG,"onSingleImage callback");

        final byte[] singleImageArray=ImageUtil.matToByteArray(hiresMat);

        Bitmap hiresBitMap = Bitmap.createBitmap(hiresMat.width(), hiresMat.height(), Bitmap.Config.ARGB_8888);
        Utils.matToBitmap(hiresMat, hiresBitMap);

        try {
            ByteArrayOutputStream bytes=new ByteArrayOutputStream();
            hiresBitMap.compress(Bitmap.CompressFormat.JPEG, 100, bytes);
            FileOutputStream fo=openFileOutput("hires", Context.MODE_PRIVATE);
            fo.write(bytes.toByteArray());
            fo.close();
        }catch (Exception e){
            e.printStackTrace();
        }

        final ImageQualityActivity self = this;

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Intent i = new Intent(self, ImageResultActivity.class);
                i.addFlags(Intent.FLAG_ACTIVITY_FORWARD_RESULT);
                i.putExtra("captured", captureByteArray);
                i.putExtra("window", windowByteArray);
                i.putExtra("topLine", rdtinterpretresult.topLine);
                i.putExtra("middleLine", rdtinterpretresult.middleLine);
                i.putExtra("bottomLine", rdtinterpretresult.bottomLine);
                i.putExtra("topLineName", rdtinterpretresult.topLineName);
                i.putExtra("middleLineName", rdtinterpretresult.middleLineName);
                i.putExtra("bottomLineName", rdtinterpretresult.bottomLineName);
                i.putExtra("timeTaken", time);
                i.putExtra("hasTooMuchBlood", rdtinterpretresult.hasTooMuchBlood);
                i.putExtra("numberOfLines", rdtinterpretresult.numberOfLines);

                Bundle args = new Bundle();
                //modified signature to peaksArray
                args.putSerializable("peaksArray", (Serializable) rdtinterpretresult.peaks);
                //red peak array packed into intent
                args.putSerializable("RedpeaksArray", (Serializable) rdtinterpretresult.redPeaks);
                args.putSerializable("avgIntensities", (Serializable) rdtinterpretresult.avgIntensities);
                i.putExtra("BUNDLE", args);

                startActivity(i);
            }
        });
    }
}
