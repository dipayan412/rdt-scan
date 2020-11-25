package edu.washington.cs.ubicomplab.rdt_reader.core;

import android.app.ListActivity;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.opencv.android.Utils;
import org.opencv.core.Core;
import org.opencv.core.KeyPoint;
import org.opencv.core.Mat;
import org.opencv.core.MatOfKeyPoint;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Size;
import org.opencv.features2d.BFMatcher;
import org.opencv.imgproc.Imgproc;
import org.opencv.xfeatures2d.SIFT;

import java.io.InputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import edu.washington.cs.ubicomplab.rdt_reader.utils.AppSingleton;

import static android.content.Context.MODE_PRIVATE;
import static edu.washington.cs.ubicomplab.rdt_reader.core.Constants.SHARPNESS_GAUSSIAN_BLUR_WINDOW;
import static org.opencv.imgproc.Imgproc.cvtColor;

/**
 * Object for holding all of the RDT-specific variables, including those provided in config.json
 */
public class RDT {
    // Template image variables
    public int refImageID;
    public String rdtName;

    // UI variables
    public double viewFinderScaleH, viewFinderScaleW;

    // Result window variables
    public double topLinePosition, middleLinePosition, bottomLinePosition;
    public String topLineName, middleLineName, bottomLineName;
    public int lineIntensity;
    public int lineSearchWidth;
    public ArrayList<double[]> topLineHueRange, middleLineHueRange, bottomLineHueRange;
    public int numberOfLines;

    // Fiducial variables
    public double distanctFromFiducialToResultWindow;
    public Rect resultWindowRect;
    public JSONArray fiducials;
    public ArrayList<Rect> fiducialRects;
    public boolean hasFiducial;
    // Feature matching variables
    public Mat refImg;
    public double refImgSharpness;
    public Mat refDescriptor;
    public MatOfKeyPoint refKeypoints;
    public SIFT detector;
    public BFMatcher matcher;
    //Glare check variables
    public boolean checkGlare;

    public boolean rotated = false;

    public RDT(Context context, String rdtName) {
        try {
            // Read config.json
            InputStream is = context.getAssets().open(Constants.CONFIG_FILE_NAME);
            int size = is.available();
            byte[] buffer = new byte[size];
            is.read(buffer);
            is.close();
            JSONObject obj = new JSONObject(new String(buffer, "UTF-8")).getJSONObject(rdtName);

            // Load the template image
            refImageID = context.getResources().getIdentifier(obj.getString("REF_IMG"),
                    "drawable", context.getPackageName());
            refImg = new Mat();
            Bitmap bitmap = BitmapFactory.decodeResource(context.getResources(), refImageID);
            Utils.bitmapToMat(bitmap, refImg);

            if(refImg.height() > refImg.width()) {
                Core.rotate(refImg, refImg, Core.ROTATE_90_COUNTERCLOCKWISE);
                rotated = true;
            }

            cvtColor(refImg, refImg, Imgproc.COLOR_RGB2GRAY);
            this.rdtName = rdtName;

            // Pull data related to UI
            viewFinderScaleH = obj.getDouble("VIEW_FINDER_SCALE");
            viewFinderScaleW = (viewFinderScaleH * (double)refImg.height()/(double)refImg.width())+Constants.VIEW_FINDER_SCALE_W_PADDING;
            //viewFinderScaleW = obj.getDouble("VIEW_FINDER_SCALE_W");
            JSONArray rectTL = obj.getJSONArray("RESULT_WINDOW_TOP_LEFT");
            JSONArray rectBR = obj.getJSONArray("RESULT_WINDOW_BOTTOM_RIGHT");
            resultWindowRect = rotated ? new Rect(new Point(rectTL.getDouble(1), rectTL.getDouble(0)),
                    new Point(rectBR.getDouble(1), rectBR.getDouble(0))) :
                    new Rect(new Point(rectTL.getDouble(0), rectTL.getDouble(1)),
                            new Point(rectBR.getDouble(0), rectBR.getDouble(1)));

            // Pull data related to the result window
            topLinePosition = rotated ? obj.getJSONArray("TOP_LINE_POSITION").getDouble(1) - resultWindowRect.x : obj.getJSONArray("TOP_LINE_POSITION").getDouble(0) - resultWindowRect.x;
            middleLinePosition = rotated ? obj.getJSONArray("MIDDLE_LINE_POSITION").getDouble(1) - resultWindowRect.x: obj.getJSONArray("MIDDLE_LINE_POSITION").getDouble(0) - resultWindowRect.x;
            if (obj.has("BOTTOM_LINE_POSITION")) {
                bottomLinePosition = rotated ? obj.getJSONArray("BOTTOM_LINE_POSITION").getDouble(1) - resultWindowRect.x: obj.getJSONArray("BOTTOM_LINE_POSITION").getDouble(0) - resultWindowRect.x;
                numberOfLines = 3;
            } else {
                bottomLinePosition = 0;
                numberOfLines = 2;
            }
            topLineHueRange = obj.has("TOP_LINE_HUE_RANGE") ? new ArrayList<double[]>((Collection<? extends double[]>) obj.getJSONArray("TOP_LINE_HUE_RANGE")) : new ArrayList<double[]>();
            middleLineHueRange = obj.has("MIDDLE_LINE_HUE_RANGE") ? new ArrayList<double[]>((Collection<? extends double[]>) obj.getJSONArray("MIDDLE_LINE_HUE_RANGE")) : new ArrayList<double[]>();
            bottomLineHueRange = obj.has("BOTTOM_LINE_HUE_RANGE") ? new ArrayList<double[]>((Collection<? extends double[]>) obj.getJSONArray("BOTTOM_LINE_HUE_RANGE")) : new ArrayList<double[]>();
            topLineName = obj.getString("TOP_LINE_NAME");
            middleLineName = obj.getString("MIDDLE_LINE_NAME");
            if (numberOfLines > 2 && obj.has("BOTTOM_LINE_NAME")) {
                bottomLineName = obj.getString("BOTTOM_LINE_NAME");
            }
            lineIntensity = obj.getInt("LINE_INTENSITY");
            if (numberOfLines > 2 && obj.has("BOTTOM_LINE_POSITION")) {
                lineSearchWidth = obj.has("LINE_SEARCH_WIDTH") ? obj.getInt("LINE_SEARCH_WIDTH") :
                        Math.max((int) ((middleLinePosition - topLinePosition) / 2.0), (int) ((bottomLinePosition - middleLinePosition) / 2.0));
            } else {
                lineSearchWidth = obj.has("LINE_SEARCH_WIDTH") ? obj.getInt("LINE_SEARCH_WIDTH") :
                        (int) ((middleLinePosition - topLinePosition) / 2.0);
            }

            checkGlare = obj.has("CHECK_GLARE") ? obj.getBoolean("CHECK_GLARE") : false;

            // Pull data related to fiducials
            fiducials = obj.has("FIDUCIALS") ? obj.getJSONArray("FIDUCIALS") : new JSONArray();
            hasFiducial = fiducials.length() > 0;
            distanctFromFiducialToResultWindow = 0;

            if (hasFiducial && fiducials.length() == 2) {
                JSONArray trueFiducial1 = fiducials.getJSONArray(0);
                Point trueFiducialTL1 = rotated
                        ? new Point(trueFiducial1.getJSONArray(0).getDouble(1), trueFiducial1.getJSONArray(0).getDouble(0))
                        : new Point(trueFiducial1.getJSONArray(0).getDouble(0), trueFiducial1.getJSONArray(0).getDouble(1));
                Point trueFiducialBR1 = rotated
                        ? new Point(trueFiducial1.getJSONArray(1).getDouble(1), trueFiducial1.getJSONArray(1).getDouble(0))
                        : new Point(trueFiducial1.getJSONArray(1).getDouble(0), trueFiducial1.getJSONArray(1).getDouble(1));

                JSONArray trueFiducial2 = fiducials.getJSONArray(1);
                Point trueFiducialTL2 = rotated
                        ? new Point(trueFiducial2.getJSONArray(0).getDouble(1), trueFiducial2.getJSONArray(0).getDouble(0))
                        : new Point(trueFiducial2.getJSONArray(0).getDouble(0), trueFiducial2.getJSONArray(0).getDouble(1));
                Point trueFiducialBR2 = rotated
                        ? new Point(trueFiducial2.getJSONArray(1).getDouble(1), trueFiducial2.getJSONArray(1).getDouble(0))
                        : new Point(trueFiducial2.getJSONArray(1).getDouble(0), trueFiducial2.getJSONArray(1).getDouble(1));

                fiducialRects = new ArrayList<>();

                fiducialRects.add(new Rect(trueFiducialTL1, trueFiducialBR1));
                fiducialRects.add(new Rect(trueFiducialTL2, trueFiducialBR2));

                distanctFromFiducialToResultWindow = resultWindowRect.x - (trueFiducialBR2.x + trueFiducialBR1.x)/2.0;
            }

            // Store the reference's sharpness
            Size kernel = new Size(SHARPNESS_GAUSSIAN_BLUR_WINDOW,
                    SHARPNESS_GAUSSIAN_BLUR_WINDOW);
            Imgproc.GaussianBlur(refImg, refImg, kernel, 0, 0);

//            refImg = new Mat(refImg, new Rect(100, 0, refImg.width() - 200, refImg.height()));
//            Bitmap bitmap = Bitmap.createBitmap(temp.width(), temp.height(), Bitmap.Config.ARGB_8888);
//            Utils.matToBitmap(temp, bitmap);

            // Load the reference image's features

            detector = SIFT.create();
            matcher = BFMatcher.create(BFMatcher.BRUTEFORCE, false);

            long startTime = System.currentTimeMillis();
            long startTime_0 = System.currentTimeMillis();
            long startTime_00 = System.currentTimeMillis();
            if(AppSingleton.getInstance().getRefKeypoints() == null) {
                SharedPreferences pref = context.getSharedPreferences("rdt-scan", MODE_PRIVATE);
                SharedPreferences.Editor editor = pref.edit();
                if(pref.getString("refKeypoints", null) != null) {
                    startTime = System.currentTimeMillis();
                    String json = pref.getString("refKeypoints", null);
                    refKeypoints = new MatOfKeyPoint();
                    JsonObject JsonObject = JsonParser.parseString(json).getAsJsonObject();

                    int rows = JsonObject.get("rows").getAsInt();
                    int cols = JsonObject.get("cols").getAsInt();
                    int type = JsonObject.get("type").getAsInt();
                    String data = JsonObject.get("data").getAsString();
                    refKeypoints.create(rows, cols, type); //from db
//                    refKeypoints.put(0, 0, data); // from db

                    JSONArray jsonArray = new JSONArray(data);
                    float[] dataArr = new float[jsonArray.length()];
                    for (int i = 0; i < jsonArray.length(); i++) {
//                        dataList.add((float)jsonArray.getDouble(i));
                        dataArr[i] = (float) jsonArray.getDouble(i);
                    }
                    refKeypoints.put(0, 0, dataArr);
                    refDescriptor = new Mat();
                    if(pref.getString("refDescriptor", null) != null) {
                        String json_1 = pref.getString("refDescriptor", null);
                        JsonObject JsonObject_1 = JsonParser.parseString(json_1).getAsJsonObject();

                        int rows_1 = JsonObject_1.get("rows").getAsInt();
                        int cols_1 = JsonObject_1.get("cols").getAsInt();
                        int type_1 = JsonObject_1.get("type").getAsInt();

                        String dataString = JsonObject_1.get("data").getAsString();
                        byte[] data_1 = Base64.decode(dataString.getBytes(), Base64.DEFAULT);

                        refDescriptor = new Mat(rows_1, cols_1, type_1);
                        refDescriptor.put(0, 0, data_1);
                    } else {
                        detector.compute(refImg, refKeypoints, refDescriptor);
                        AppSingleton.getInstance().setRefKeypoints(refKeypoints);
                        AppSingleton.getInstance().setRefDescriptor(refDescriptor);
                    }

                    Log.d("SharedPreferences", "" + (System.currentTimeMillis() - startTime_0));
                } else {
                    refDescriptor = new Mat();
                    refKeypoints = new MatOfKeyPoint();
                    MatOfKeyPoint goodkpMat = new MatOfKeyPoint();
                    detector.detectAndCompute(refImg, new Mat(), refKeypoints, refDescriptor);

                    ArrayList<KeyPoint> goodkpsList= new ArrayList<KeyPoint> (refKeypoints.toList());
                    int i=0;
                    Iterator<KeyPoint> iter=goodkpsList.listIterator();
                    while (iter.hasNext()){
                        KeyPoint kp=iter.next();
                        Log.d("RDT","response "+String.valueOf(kp.response));
                        if(kp.response < 0.04){
                            i++;
                            iter.remove();
//                            Log.d("RDT","goodkpsList size"+goodkpsList.size());
                        }
                    }
                    goodkpMat.fromList(goodkpsList);
                    startTime = System.currentTimeMillis();
                    detector.compute(refImg, goodkpMat, refDescriptor);
                    Log.d("Compute", "" + (System.currentTimeMillis() - startTime));
                    refKeypoints = goodkpMat;

                    AppSingleton.getInstance().setRefKeypoints(goodkpMat);
//                AppSingleton.getInstance().setRefKeypoints(refKeypoints);
                    AppSingleton.getInstance().setRefDescriptor(refDescriptor);

                    Gson gson = new Gson();

                    JsonObject obj1 = new JsonObject();
                    int cols = goodkpMat.cols();
                    int rows = goodkpMat.rows();
                    int elemSize = (int) goodkpMat.elemSize();
                    float[] data = new float[rows * 7];
                    goodkpMat.get(0, 0, data);
                    obj1.addProperty("rows", goodkpMat.rows());
                    obj1.addProperty("cols", goodkpMat.cols());
                    obj1.addProperty("type", goodkpMat.type());

                    // We cannot set binary data to a json object, so:
                    // Encoding data byte array to Base64.
                    String dataString = gson.toJson(data);//new String(Base64.encode(data, Base64.DEFAULT));

                    obj1.addProperty("data", dataString);
                    String json1 = gson.toJson(obj1);
                    editor.putString("refKeypoints", json1);
                    editor.apply();

                    JsonObject obj_1 = new JsonObject();
                    int cols_1 = refDescriptor.cols();
                    int rows_1 = refDescriptor.rows();
                    int elemSize_1 = (int) refDescriptor.elemSize();

                    byte[] data_1 = new byte[cols_1 * rows_1 * elemSize_1];

                    refDescriptor.get(0, 0, data_1);

                    obj_1.addProperty("rows", refDescriptor.rows());
                    obj_1.addProperty("cols", refDescriptor.cols());
                    obj_1.addProperty("type", refDescriptor.type());

                    // We cannot set binary data to a json object, so:
                    // Encoding data byte array to Base64.
                    String dataString_1 = new String(Base64.encode(data_1, Base64.DEFAULT));

                    obj_1.addProperty("data", dataString_1);
                    String json_1 = gson.toJson(obj_1);
                    editor.putString("refDescriptor", json_1);
                    editor.apply();
                }

//
//                JsonObject obj2 = new JsonObject();
//                cols = goodkpMat.cols();
//                rows = goodkpMat.rows();
//                elemSize = (int) goodkpMat.elemSize();
//                byte[] data2 = new byte[cols * rows * elemSize];
//                refDescriptor.get(0, 0, data);
//                obj2.addProperty("rows", refDescriptor.rows());
//                obj2.addProperty("cols", refDescriptor.cols());
//                obj2.addProperty("type", refDescriptor.type());
//
//                // We cannot set binary data to a json object, so:
//                // Encoding data byte array to Base64.
//                dataString = new String(Base64.encode(data2, Base64.DEFAULT));
//
//                obj2.addProperty("data", dataString);
//                String json2 = gson.toJson(obj2);
//                Log.d("HELLO", "HELLO");
            } else {
                refDescriptor = AppSingleton.getInstance().getRefDescriptor();
                refKeypoints = AppSingleton.getInstance().getRefKeypoints();
            }

            Log.d("RefDetectAndCompute", "" + (System.currentTimeMillis() - startTime_00));
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}