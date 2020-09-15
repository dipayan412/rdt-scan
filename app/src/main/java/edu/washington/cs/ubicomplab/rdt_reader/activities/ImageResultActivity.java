/*
 * Copyright (C) 2019 University of Washington Ubicomp Lab
 * All rights reserved.
 *
 * This software may be modified and distributed under the terms
 * of a BSD-style license that can be found in the LICENSE file.
 */

package edu.washington.cs.ubicomplab.rdt_reader.activities;

import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.ExifInterface;
import android.net.Uri;
import android.support.design.widget.TextInputEditText;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.lang.reflect.Array;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

import edu.washington.cs.ubicomplab.rdt_reader.R;
import edu.washington.cs.ubicomplab.rdt_reader.fragments.SettingsDialogFragment;
import edu.washington.cs.ubicomplab.rdt_reader.interfaces.SettingsDialogListener;
import edu.washington.cs.ubicomplab.rdt_reader.core.Constants;

import static java.text.DateFormat.getDateTimeInstance;

/**
 * The {@link android.app.Activity} for showing the results of RDT image post-processing and
 * automatic analysis
 * Note: In this example app, this activity is launched as an {@link Intent} from {@link MainActivity}
 * with the target RDT's name passed in the bundle to support multiple RDT designs simultaneously
 */
public class ImageResultActivity extends AppCompatActivity implements View.OnClickListener, SettingsDialogListener {
    // Image saving variables
    Bitmap mBitmapToSave;
    byte[] capturedByteArray, windowByteArray;
    boolean isImageSaved = false;
    Bitmap resultimageBitMap;
    Bitmap windowimageBitMap;
    String resultString;

    // Capture time variable
    long timeTaken = 0;

    /**
     * {@link android.app.Activity} onCreate()
     * @param savedInstanceState: the bundle object in case this is launched from an intent
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_result);

        // Initialize UI elements
        initViews();
    }

    //  size = 3

    /**
     * Initializes UI elements based on that data that was passed through the intent
     */
    private void initViews() {
        Intent intent = getIntent();

        Bundle args = intent.getBundleExtra("BUNDLE");
        ArrayList<double[]> peaks = (ArrayList<double[]>) args.getSerializable("ARRAYLIST");
        double[] avgIntensities = (double[]) args.getSerializable("avgIntensities");

        resultString = "" + (peaks.size() > 0 && peaks.get(0) != null ? "Control " + String.format("%.1f", peaks.get(1)[3]) + ", ": "");
        resultString += "" + (peaks.size() > 1 ? "Test " + String.format("%.1f", peaks.get(1)[3]) : "");
        // Captured image
        if (intent.hasExtra("captured")) {
            capturedByteArray = intent.getExtras().getByteArray("captured");
            mBitmapToSave = BitmapFactory.decodeByteArray(capturedByteArray, 0, capturedByteArray.length);

            ImageView resultImageView = findViewById(R.id.RDTImageView);
            resultimageBitMap=BitmapFactory.decodeByteArray(capturedByteArray, 0, capturedByteArray.length);
            resultImageView.setImageBitmap(resultimageBitMap);
        }

        // Enhanced image
        if (intent.hasExtra("window")) {
            windowByteArray = intent.getExtras().getByteArray("window");
            mBitmapToSave = BitmapFactory.decodeByteArray(windowByteArray, 0, windowByteArray.length);

            ImageView windowImageView = findViewById(R.id.WindowImageView);
            windowimageBitMap=BitmapFactory.decodeByteArray(windowByteArray,0,windowByteArray.length);
            windowImageView.setImageBitmap(windowimageBitMap);

        }

//        // Capture time
//        if (intent.hasExtra("timeTaken")) {
//            timeTaken = intent.getLongExtra("timeTaken", 0);
//            TextView timeTextView = findViewById(R.id.TimeTextView);
//            timeTextView.setText(String.format("%.2f seconds", timeTaken/1000.0));
//        }

        //Number of lines
        int numberOfLines = 2;
        if (intent.hasExtra("numberOfLines")) {
            numberOfLines = intent.getIntExtra("numberOfLines", 2);
        }

        // Top line
        if (intent.hasExtra("topLine")) {
            boolean topLine = intent.getBooleanExtra("topLine", false);
            TextView topLineTextView = findViewById(R.id.topLineTextView);
            topLineTextView.setText(peaks.size() > 0 && peaks.get(0) != null ? String.format("%.1f",(peaks.get(0)[3])) : "");
            //topLineTextView.setText(String.format("%s", topLine ? "True" : "False"));
        }
        if (intent.hasExtra("topLineName")) {
            String topLineName = intent.getStringExtra("topLineName");
            TextView topLineNameTextView = findViewById(R.id.topLineNameTextView);
            topLineNameTextView.setText(topLineName);
        }

        // Middle line
        if (intent.hasExtra("middleLine")) {
            boolean middleLine = intent.getBooleanExtra("middleLine", false);
            TextView middleLineTextView = findViewById(R.id.middleLineTextView);
            middleLineTextView.setText(peaks.size() > 1 ? String.format("%.1f",(peaks.get(1)[3])) : "");
            //middleLineTextView.setText(String.format("%s", middleLine ? "True" : "False"));
        }
        if (intent.hasExtra("middleLineName")) {
            String middleLineName = intent.getStringExtra("middleLineName");
            TextView middleLineNameTextView = findViewById(R.id.middleLineNameTextView);
            middleLineNameTextView.setText(middleLineName);
        }

        // Bottom line
        if (numberOfLines > 2 && intent.hasExtra("bottomLine")) {
            boolean bottomLine = intent.getBooleanExtra("bottomLine", false);
            TextView bottomLineTextView = findViewById(R.id.bottomLineTextView);
            bottomLineTextView.setVisibility(View.VISIBLE);
            bottomLineTextView.setText("");
            //bottomLineTextView.setText(String.format("%s", bottomLine ? "True" : "False"));
        }
        if (numberOfLines > 2 &&  intent.hasExtra("bottomLineName")) {
            String bottomLineName = intent.getStringExtra("bottomLineName");
            TextView bottomLineNameTextView = findViewById(R.id.bottomLineNameTextView);
            bottomLineNameTextView.setVisibility(View.VISIBLE);
            bottomLineNameTextView.setText(bottomLineName);
        }

        if (intent.hasExtra("hasTooMuchBlood")) {
            boolean hasTooMuchBlood = intent.getBooleanExtra("hasTooMuchBlood", false);
            TextView warningView = findViewById(R.id.WarningView);
            if (hasTooMuchBlood) {
                warningView.setText(getString(R.string.too_much_blood_warning));
            } else {
                warningView.setText("");
            }
        }

        // Buttons
        Button saveImageButton = findViewById(R.id.saveButton);
        saveImageButton.setOnClickListener(this);
        Button sendImageButton = findViewById(R.id.doneButton);
        sendImageButton.setOnClickListener(this);
    }

    /**
     * {@link android.app.Activity} onBackPressed()
     */
    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        View view = getCurrentFocus();
        boolean ret = super.dispatchTouchEvent(ev);

        if (view instanceof EditText) {
            View w = getCurrentFocus();
            int [] scrcoords = new int[2];
            w.getLocationOnScreen(scrcoords);
            float x = ev.getRawX() + w.getLeft() - scrcoords[0];
            float y = ev.getRawY() + w.getTop() - scrcoords[1];

            if (ev.getAction() == MotionEvent.ACTION_UP
                    && (x < w.getLeft() || x >= w.getRight()
                    || y < w.getTop() || y > w.getBottom()) ) {
                InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(getWindow().getCurrentFocus().getWindowToken(), 0);
            }
        }
        return ret;
    }

    /**
     * The listener for all of the Activity's buttons
     * @param view the button that was selected
     */
    @Override
    public void onClick(View view) {
        // Save the photo locally on the user's device
        if (view.getId() == R.id.saveButton) {

            // Test whether the inputSampleID field is empty. - wwang
            TextInputEditText inputSampleID = findViewById(R.id.sampleID_input);
            String sampleID=inputSampleID.getText().toString();

            if (sampleID.trim().equals("")) {
                inputSampleID.setError("Sample ID is required!");
                inputSampleID.setHint("Sample ID can not be empty. Input a sample ID");
                return;
            }

            // Skip if the image is already saved
            if (isImageSaved) {
                Toast.makeText(this,"Image is already saved.", Toast.LENGTH_LONG).show();
                return;
            }

            // Create storage directories if they don't already exist
            File sdIconStorageDir = new File(Constants.RDT_IMAGE_DIR);
            sdIconStorageDir.mkdirs();

            // Get the current time to use as part of the filename
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH-mm-ss-SSS");

            // Save both the full image and the enhanced image
            try {
                // Save the full image
                // removed timetaken from filename, added sampleID at beginning of filename wwang
                String filePath = sdIconStorageDir.toString() +
                        String.format("/%s-%s_full.jpg",sampleID, sdf.format(new Date()));
                FileOutputStream fileOutputStream = new FileOutputStream(filePath);
                fileOutputStream.write(capturedByteArray);
                fileOutputStream.flush();
                fileOutputStream.close();

                // Save the enhanced image
                filePath = sdIconStorageDir.toString() +
                        String.format("/%s-%s_cropped.jpg", sampleID,sdf.format(new Date()));

                ByteArrayOutputStream windowimagestream=new ByteArrayOutputStream();
                windowimageBitMap.compress(Bitmap.CompressFormat.JPEG,100,windowimagestream);


                fileOutputStream = new FileOutputStream(filePath);
                fileOutputStream.write(windowimagestream.toByteArray());
                //fileOutputStream.write(windowByteArray);
                fileOutputStream.flush();
                fileOutputStream.close();

                ExifInterface windowExif=new ExifInterface(filePath);


                // save sample metadata to image file - wwang Note this might not work with SDK <24
                windowExif.setAttribute(ExifInterface.TAG_IMAGE_DESCRIPTION, sampleID);
                windowExif.setAttribute(ExifInterface.TAG_USER_COMMENT,resultString);
                windowExif.saveAttributes();

                Log.d("ImageResultActivity",windowExif.getAttribute(ExifInterface.TAG_IMAGE_DESCRIPTION));


                // Send broadcast to OS so that the files appear immediately in the file system
                sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.parse("file://" + filePath)));

                // Notify the user that the image has been saved
                Toast.makeText(this,"Image is successfully saved!", Toast.LENGTH_SHORT).show();
                isImageSaved = true;

                // clear sampleID text box - wwang
                inputSampleID.setText("");

            } catch (Exception e) {
                Log.w("TAG", "Error saving image file: " + Log.getStackTraceString(e));
            }
        } else if (view.getId() == R.id.doneButton) {
            Intent data = new Intent();
            data.putExtra("RDTCaptureByteArray", capturedByteArray);
            setResult(RESULT_OK, data);
            finish();
        }
    }

    /**
     * {@link SettingsDialogFragment} onClickPositiveButton()
     */
    @Override
    public void onClickPositiveButton() {
        Resources res = getResources();
        DisplayMetrics dm = res.getDisplayMetrics();
        android.content.res.Configuration conf = res.getConfiguration();
        conf.setLocale(new Locale(Constants.LANGUAGE));
        res.updateConfiguration(conf, dm);

        setContentView(R.layout.activity_image_quality);
        initViews();
    }
}
