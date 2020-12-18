/*
 * Copyright (C) 2019 University of Washington Ubicomp Lab
 * All rights reserved.
 *
 * This software may be modified and distributed under the terms
 * of a BSD-style license that can be found in the LICENSE file.
 */

package edu.washington.cs.ubicomplab.rdt_reader.activities;

import android.app.AlertDialog;
//import android.arch.persistence.room.Room;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources;
import android.database.sqlite.SQLiteConstraintException;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.AsyncTask;
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

import org.opencv.android.Utils;
import org.opencv.core.Mat;
import org.opencv.core.MatOfByte;
import org.opencv.core.Point;
import org.opencv.core.Scalar;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.Array;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

import edu.washington.cs.ubicomplab.rdt_reader.R;
import edu.washington.cs.ubicomplab.rdt_reader.core.RDTCaptureResult;
import edu.washington.cs.ubicomplab.rdt_reader.db.DatabaseClient;
import edu.washington.cs.ubicomplab.rdt_reader.fragments.SettingsDialogFragment;
import edu.washington.cs.ubicomplab.rdt_reader.interfaces.SettingsDialogListener;
import edu.washington.cs.ubicomplab.rdt_reader.core.Constants;
import edu.washington.cs.ubicomplab.rdt_reader.model.SampleID;

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
    byte[] capturedByteArray, windowByteArray,hiresByteArray;
    boolean isImageSaved = false;
    Bitmap resultimageBitMap;
    Bitmap windowimageBitMap;
    Bitmap originalWindowBitmap;
    String resultString;
    Bitmap hiresBitMap;


    // Capture time variable
    long timeTaken = 0;

    //SampleID input
    TextInputEditText inputSampleID;

    /**
     * {@link android.app.Activity} onCreate()
     * @param savedInstanceState: the bundle object in case this is launched from an intent
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_result);

        // pull in high resolution image from internal file
        try{
            FileInputStream hiresFile=this.openFileInput("hires");
            hiresBitMap=BitmapFactory.decodeStream(hiresFile);
            hiresFile.close();
            //delete this to make sure file changes on next capture
            deleteFile("hires");
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            hiresBitMap = BitmapFactory.decodeResource(getResources(), R.mipmap.defaultbitmap);
        } catch (IOException e) {
            e.printStackTrace();
        }

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
        // peaksarray extracted
        ArrayList<double[]> peaks = (ArrayList<double[]>) args.getSerializable("peaksArray");

        double[] avgIntensities = (double[]) args.getSerializable("avgIntensities");

        String channel=intent.hasExtra("signalChannel")?intent.getStringExtra("signalChannel"):"Unknown";

        resultString ="Channel - " + channel+" : " + (peaks.size() > 0 && peaks.get(0) != null ? String.format("%.1f", peaks.get(0)[3]) : "-1");
        resultString += ":" + (peaks.size() > 1 ? String.format("%.1f", peaks.get(1)[3]) : "-1")+System.lineSeparator();


        // Captured image
        ImageView resultImageView = findViewById(R.id.RDTImageView);
        if (intent.hasExtra("captured")) {
            capturedByteArray = intent.getExtras().getByteArray("captured");
            mBitmapToSave = BitmapFactory.decodeByteArray(capturedByteArray, 0, capturedByteArray.length);


            resultimageBitMap=BitmapFactory.decodeByteArray(capturedByteArray, 0, capturedByteArray.length);
            resultImageView.setImageBitmap(resultimageBitMap);
        }

        // Enhanced image
        if (intent.hasExtra("window")) {
            windowByteArray = intent.getExtras().getByteArray("window");
            mBitmapToSave = BitmapFactory.decodeByteArray(windowByteArray, 0, windowByteArray.length);
            ImageView windowImageView = findViewById(R.id.WindowImageView);

            originalWindowBitmap = BitmapFactory.decodeByteArray(windowByteArray,0,windowByteArray.length);

            if(peaks.size() > 0) {

                Mat windowMat = new Mat();
                Utils.bitmapToMat(originalWindowBitmap, windowMat);//Imgcodecs.imdecode(new MatOfByte(windowByteArray), Imgcodecs.CV_LOAD_IMAGE_ANYCOLOR);
//                Imgproc.cvtColor(windowMat,windowMat,Imgproc.COLOR_BGR2RGB);
//                Bitmap temp = Bitmap.createBitmap(windowMat.cols(), windowMat.rows(), Bitmap.Config.ARGB_8888);
//                Utils.matToBitmap(windowMat, temp);
                if(peaks.get(0) != null) {
                    Point pt1_control = new Point(peaks.get(0)[0], 0);
                    Point pt2_control = new Point(peaks.get(0)[0], 4);
//                    Imgproc.line(windowMat, pt1_control, pt2_control, new Scalar(255, 166, 0), 1);
                    Imgproc.line(windowMat, pt1_control, pt2_control, new Scalar(0, 0, 255), 1);
                }
                if(peaks.size() > 1) {
                    Point pt1_test=new Point(peaks.get(1)[0],0);
                    Point pt2_test=new Point(peaks.get(1)[0],4);
                    Imgproc.line(windowMat,pt1_test,pt2_test,new Scalar(72,255,0),1);
                }
                Bitmap windowBitmap = Bitmap.createBitmap(windowMat.cols(), windowMat.rows(), Bitmap.Config.ARGB_8888);
                Utils.matToBitmap(windowMat, windowBitmap);
                windowimageBitMap = windowBitmap;
            } else {
                windowimageBitMap=BitmapFactory.decodeByteArray(windowByteArray,0,windowByteArray.length);
            }
            Matrix matrix = new Matrix();
            matrix.postRotate(90);
            windowimageBitMap = Bitmap.createBitmap(windowimageBitMap, 0, 0, windowimageBitMap.getWidth(), windowimageBitMap.getHeight(), matrix, true);
            windowImageView.setImageBitmap(windowimageBitMap);

            Matrix matrix_0 = new Matrix();
            matrix_0.postRotate(90);
            originalWindowBitmap = Bitmap.createBitmap(originalWindowBitmap, 0, 0, originalWindowBitmap.getWidth(), originalWindowBitmap.getHeight(), matrix_0, true);
//            windowImageView.setImageBitmap(originalWindowBitmap);
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
            topLineTextView.setTextColor(Color.rgb(0, 0, 255));
            topLineTextView.setText(peaks.size() > 0 && peaks.get(0) != null ? String.format("%.1f",(peaks.get(0)[3])) : "no control line");
            //topLineTextView.setText(String.format("%s", topLine ? "True" : "False"));
        }
        if (intent.hasExtra("topLineName")) {
            String topLineName = intent.getStringExtra("topLineName");
            TextView topLineNameTextView = findViewById(R.id.topLineNameTextView);
            topLineNameTextView.setTextColor(Color.rgb(0, 0, 255));
            topLineNameTextView.setText(topLineName);
        }

        // Middle line
        if (intent.hasExtra("middleLine")) {
            boolean middleLine = intent.getBooleanExtra("middleLine", false);
            TextView middleLineTextView = findViewById(R.id.middleLineTextView);
            middleLineTextView.setText(peaks.size() > 1 ? String.format("%.1f",(peaks.get(1)[3])) : "no test line");
            middleLineTextView.setTextColor(Color.rgb(51,153,0));
            //middleLineTextView.setText(String.format("%s", middleLine ? "True" : "False"));
        }
        if (intent.hasExtra("middleLineName")) {
            String middleLineName = intent.getStringExtra("middleLineName");
            TextView middleLineNameTextView = findViewById(R.id.middleLineNameTextView);
            middleLineNameTextView.setTextColor(Color.rgb(51,153,0));
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
        inputSampleID= findViewById(R.id.sampleID_input);
    }

    /**
     * {@link android.app.Activity} onBackPressed()
     */
    @Override
    public void onBackPressed() {
        super.onBackPressed();
        // clear sampleID text box - wwang
        inputSampleID.setText("");
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

            // moving this line to initViews()
//            inputSampleID= findViewById(R.id.sampleID_input);
            String sampleID=inputSampleID.getText().toString().trim();
            searchSampleID(sampleID);

        } else if (view.getId() == R.id.doneButton) {
//            Intent data = new Intent();
//            data.putExtra("RDTCaptureByteArray", capturedByteArray);
//            setResult(RESULT_OK, data);
//            finish();
            // clear sampleID text box - wwang
            inputSampleID.setText("");

            Intent intent = new Intent(this, MainActivity.class);
            startActivity(intent);
        }
    }
    /*
    * refactored function to save imagefile
    * @param sampleID the sampleID user typed in
     */
    public void saveImageFile(String sampleID) {

//        // Skip if the image is already saved
//        if (isImageSaved) {
//            Toast.makeText(this,"Image is already saved.", Toast.LENGTH_LONG).show();
//            return;
//        }

        // Create storage directories if they don't already exist
        File sdIconStorageDir = new File(Constants.RDT_IMAGE_DIR);
        sdIconStorageDir.mkdirs();

        // Get the current time to use as part of the filename
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH-mm-ss-SSS");


                ByteArrayOutputStream windowimagestream=new ByteArrayOutputStream();
                originalWindowBitmap.compress(Bitmap.CompressFormat.JPEG,100,windowimagestream);

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

//            windowimagestream=new ByteArrayOutputStream();
//            windowimageBitMap.compress(Bitmap.CompressFormat.JPEG,100,windowimagestream);


            fileOutputStream = new FileOutputStream(filePath);
            fileOutputStream.write(windowimagestream.toByteArray());
            //fileOutputStream.write(windowByteArray);
            fileOutputStream.flush();
            fileOutputStream.close();

            //Save highres image
            String filePathhires = sdIconStorageDir.toString() +
                    String.format("/%s-%s_hires.jpg", sampleID,sdf.format(new Date()));
            ByteArrayOutputStream hiresimagestream=new ByteArrayOutputStream();
            hiresBitMap.compress(Bitmap.CompressFormat.JPEG,100,hiresimagestream);
            fileOutputStream = new FileOutputStream(filePathhires);
            fileOutputStream.write(hiresimagestream.toByteArray());
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
//            isImageSaved = true;

        } catch (Exception e) {
            Log.w("TAG", "Error saving image file: " + Log.getStackTraceString(e));
        }
    }

    /*
     * new function to perform unique sample ID check
     * @param sampleID the sampleID user typed in
     */
    public void searchSampleID(final String SampleID) {
        //Test whether input is empty
        if (SampleID.trim().equals("")) {
            inputSampleID.setError("Sample ID is required!");
            inputSampleID.setHint("Sample ID can not be empty. Input a sample ID");
            Toast.makeText(this,"A sample ID is required.", Toast.LENGTH_SHORT).show();
            return;
        }

        class SearchSampleID extends AsyncTask<String,Void,Boolean> {
            @Override
            protected void onPostExecute(Boolean aBoolean) {
                super.onPostExecute(aBoolean);
                if (aBoolean) {
                    saveImageFile(SampleID);
                }
                else {
                    AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(ImageResultActivity.this);
                    alertDialogBuilder.setMessage("Sample ID already exists. Proceed with saving additional image?");
                    alertDialogBuilder.setPositiveButton("yes",
                            new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface arg0, int arg1) {
//                                    Toast.makeText(ImageResultActivity.this,"Image saved successfully",Toast.LENGTH_LONG).show();
                                    saveImageFile(SampleID);
                                }
                            });

                    alertDialogBuilder.setNegativeButton("No",new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            Toast.makeText(ImageResultActivity.this,"Image is not saved!",Toast.LENGTH_LONG).show();
                            return;
                        }
                    });

                    AlertDialog alertDialog = alertDialogBuilder.create();
                    alertDialog.show();
                }
            }

            @Override
            protected Boolean doInBackground(String... str) {

                SampleID sampleID=new SampleID();
                sampleID.setID(str[0]);
                try {
                    DatabaseClient.getInstance(getApplicationContext())
                            .getAppDatabase()
                            .getSampleIDDao()
                            .insert(sampleID);
                }
                catch (SQLiteConstraintException e) {
                    return false;
                }
                return true;
            }


        }

        SearchSampleID srid=new SearchSampleID();
        srid.execute(SampleID);
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
